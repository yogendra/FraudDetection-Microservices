package io.pivotal.demo.sko;


import static java.lang.String.format;
import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser.Feature;
import io.pivotal.demo.sko.entity.PoSDevice;
import io.pivotal.demo.sko.entity.Transaction;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PrimitiveIterator.OfLong;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


@Component
public class Emulator {

  private static final Logger logger = getLogger(Emulator.class);
  private final Executor executor = Executors.newFixedThreadPool(20);
  @Value("${geodeUrl}")
  private String geodeURL;
  @Value("${delayInMs}")
  private long delay;
  @Value("${numberOfAccounts}")
  private int numberOfAccounts;
  @Value("${numberOfTransactions}")
  private int numberOfTransactions;
  @Value("classpath:counties.csv")
  private Resource countiesFile;
  private ArrayList<String> counties;
  private Map<Long, Long> accountToDeviceMap = new HashMap<Long, Long>();
  private RestTemplate restTemplate = new RestTemplate();

  private void getCloudEnvProperties() {
    String vcapServices = System.getenv("VCAP_SERVICES");
    if (vcapServices == null || vcapServices.isEmpty()) {
      return;
    }
    try {
      logger.info("vcap= {}", vcapServices);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode gemfire = mapper.readTree(vcapServices)
          .path("user-provided")
          .path(0)
          .path("credentials");
      geodeURL = gemfire.path("RestEndpoint")
          .asText();

    } catch (IOException e) {
      throw new RuntimeException("Unable to parse VCAP_SERVICES: " + vcapServices, e);
    }
  }


  @EventListener
  public void onAppStarted(ContextRefreshedEvent event) {

    try {
      setup();
      postTransactions();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Async
  public void setup() throws IOException {

    getCloudEnvProperties();
    loadPoSCounties();
    runSetup();

  }


  @Async
  public void postTransactions() {

    logger.info(">>>>> RUNNING SIMULATION");
    logger.info(">>> Geode rest endpoint: {}", geodeURL);

    logger.info(">>> Posting {} transactions... ", numberOfTransactions);

    int numberOfDevices = counties.size();

    Random random = new Random();

    OfLong deviceIDs = random.longs(0, numberOfDevices)
        .iterator();
    OfLong accountIDs = random.longs(0, numberOfAccounts)
        .iterator();

    long mean = 100; // mean value for transactions
    long variance = 40; // variance

    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(2);
    String transactionUrl = geodeURL + RegionName.Transaction;

    for (int i = 0; i < numberOfTransactions; i++) {

      Transaction t = new Transaction();
      t.setId(Math.abs(UUID.randomUUID()
          .getLeastSignificantBits()));
      long accountId = accountIDs.next();
      t.setAccountId(accountId);

      // 90% of times, we'll transact this account from a single "home location"
      if (Math.random() < 0.9) {
        t.setDeviceId(getHomePoS(accountId));
      } else {
        t.setDeviceId(deviceIDs.next());
      }

      t.setTimestamp(System.currentTimeMillis());

      double value = Double.parseDouble(df.format(Math.abs(mean + random.nextGaussian() * variance)));
      t.setValue(value);
      executor.execute(() -> {

        try {

          restTemplate.postForObject(transactionUrl, t, Transaction.class);
          logger.info("Posted txn: {}", t);
        } catch (Exception e) {
          logger.error("Failed to post transaction", e);
          logger.error("Check url", e);

        }
      });
      if (i != 0 && i % 100 == 0) {
        logger.info("Scheduled txn {}", i);
      }

    }
  }

  private Long getHomePoS(Long accountId) {

    // Randomly pick a deviceId, in case there's not already one mapped to that account

    if (accountToDeviceMap.get(accountId) == null) {
      Long deviceId = new Random().longs(0, counties.size())
          .iterator()
          .next();
      accountToDeviceMap.put(accountId, deviceId);
    }
    return accountToDeviceMap.get(accountId);
  }


  /*
   * Load the counties data from file
   */
  private void loadPoSCounties() throws IOException {
    counties = new ArrayList<>();
    MappingIterator<String[]> iterator = new CsvMapper()
        .enable(Feature.WRAP_AS_ARRAY)
        .readerFor(String[].class)
        .readValues(countiesFile.getFile());

    while (iterator.hasNext()) {
      String county = iterator.next()[2];
      counties.add(county);
    }
  }

  private void runSetup() {

    int numberOfDevices = counties.size();

    logger.info(">>>>> RUNNING SETUP");
    logger.info("--------------------------------------");
    logger.info(">>> Geode rest endpoint: {}", geodeURL);
    logger.info("--------------------------------------");
    String url = format("%1$s/%2$s", geodeURL, RegionName.PoS);

    logger.info("URL: {}", url);

    logger.info(">>> Adding {} devices ...", numberOfDevices);
    // Add PoS'es
    for (int i = 0; i < numberOfDevices; i++) {
      PoSDevice device = new PoSDevice();
      device.setId(i + 1);
      device.setLocation(counties.get(i));
      device.setMerchantName("Merchant " + i);
      executor.execute(() -> {
        restTemplate.postForObject(url, device, PoSDevice.class);
        logger.info("Posted: {}", device);
      });

      if (i != 0 && i % 100 == 0) {
        logger.info("Scheduled {}", i);
      }
    }
  }
}
