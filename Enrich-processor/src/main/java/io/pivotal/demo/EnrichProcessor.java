package io.pivotal.demo;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.demo.util.Util;
import java.text.DecimalFormat;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.config.SpelExpressionConverterConfiguration;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.integration.annotation.ServiceActivator;

@EnableBinding(Processor.class)
@EnableConfigurationProperties(EnrichProcessorProperties.class)
@Import(SpelExpressionConverterConfiguration.class)
public class EnrichProcessor {

  private static final Logger logger = getLogger(EnrichProcessor.class);


  @Autowired
  private StringRedisTemplate redis;

  @Autowired
  private EnrichProcessorProperties properties;

  @Autowired
  private ObjectMapper objectMapper;

  // {"id":5136908346173344474,"deviceId":854,"value":90.23,"accountId":15,"timestamp":1560442258823}
  public static final class Transaction {

    long id;
    long deviceId;
    double value;
    long accountId;
    double distance;
    String homeLocation;

    public long getId() {
      return id;
    }

    public void setId(long id) {
      this.id = id;
    }

    public long getDeviceId() {
      return deviceId;
    }

    public void setDeviceId(long deviceId) {
      this.deviceId = deviceId;
    }

    public double getValue() {
      return value;
    }

    public void setValue(double value) {
      this.value = value;
    }

    public long getAccountId() {
      return accountId;
    }

    public void setAccountId(long accountId) {
      this.accountId = accountId;
    }

    public double getDistance() {
      return distance;
    }

    public void setDistance(double distance) {
      this.distance = distance;
    }

    public String getHomeLocation() {
      return homeLocation;
    }

    public void setHomeLocation(String homeLocation) {
      this.homeLocation = homeLocation;
    }
  }

  @ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
  public Transaction process(Transaction txn) throws Exception {
//    Transaction txn = message.getPayload();

    logger.info("Received transaction with value [{}} and account [{}]", txn.value, txn.accountId);
    String location = redis.opsForValue()
        .get("device::" + txn.deviceId);
    String home = redis.opsForValue()
        .get("home::" + txn.accountId);

    txn.homeLocation = home;
    logger.info("location:{}, home:{}", location, home);

    DecimalFormat df = new DecimalFormat();
    df.setMaximumFractionDigits(2);
    df.setGroupingUsed(false);

    if (home != null && location != null) {

      String[] homeSplit = home.split(":");
      String[] locSplit = location.split(":");
      double homeLat = Double.parseDouble(homeSplit[0].trim());
      double homeLong = Double.parseDouble(homeSplit[1].trim());
      double locLat = Double.parseDouble(locSplit[0].trim());
      double locLong = Double.parseDouble(locSplit[1].trim());

      txn.distance = Util.calculateDistanceInKm(homeLat, homeLong, locLat, locLong);
      logger.info("Txn distance : {}", txn.distance);
    }

//    return new GenericMessage<>(txn, message.getHeaders());
    return txn;
  }

}	

