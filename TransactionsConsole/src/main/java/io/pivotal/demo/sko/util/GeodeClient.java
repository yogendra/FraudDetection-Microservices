package io.pivotal.demo.sko.util;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.demo.sko.RegionName;
import java.util.Collection;
import java.util.Properties;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.client.ClientCache;
import org.apache.geode.cache.client.ClientCacheFactory;
import org.apache.geode.cache.query.FunctionDomainException;
import org.apache.geode.cache.query.NameResolutionException;
import org.apache.geode.cache.query.Query;
import org.apache.geode.cache.query.QueryInvocationTargetException;
import org.apache.geode.cache.query.QueryService;
import org.apache.geode.cache.query.TypeMismatchException;
import org.apache.geode.pdx.PdxInstance;
import org.slf4j.Logger;

public class GeodeClient {

  private static final Logger logger = getLogger(GeodeClient.class);
  private static GeodeClient instance;
  private ClientCache cache;
  private String locatorHost = System.getProperty("locatorHost", "geode-server");
  private int locatorPort = Integer.getInteger("locatorPort", 10334);
  private String username = "";
  private String password = "";

  private QueryService queryService;

  private Region transactions;
  private Region suspect;


  private GeodeClient() {

    if (System.getenv("VCAP_SERVICES") != null) {
      logger.info("Configuring locator information from service binding");
      getCloudEnvProperties();
    }

    logger.info("Geode Locator Information: {}[ {} ]", locatorHost, locatorPort);

  }

  public static synchronized GeodeClient getInstance() {
    if (instance == null) {
      instance = new GeodeClient();
      instance.setup();
    }
    return instance;
  }

  /*
   * Parse the environment variables for services.
   */
  private void getCloudEnvProperties() {
    String vcapServices = System.getenv("VCAP_SERVICES");
    if (vcapServices == null || vcapServices.isEmpty()) {
      return;
    }

    try {
      logger.info("vcap= {}", vcapServices);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode credentials = mapper.readTree(vcapServices)
          .path("user-provided")
          .path(0)
          .path("credentials");

      locatorHost = credentials.path("locatorHost")
          .asText();

      locatorPort = credentials.path("locatorPort")
          .asInt();


    } catch (Exception e) {
      throw new RuntimeException("Unable to parse VCAP", e);
    }
  }

  private void setup() {

    Properties props = new Properties();
    if (!username.isEmpty()) {
			/*
			props.put("security-client-auth-init","templates.security.UserPasswordAuthInit.create");*/
      props.put("security-username", username);
      props.put("security-password", password);
    }

    cache = new ClientCacheFactory(props)
        .addPoolLocator(locatorHost, locatorPort)
        .setPoolSubscriptionEnabled(true)
        .set("name", "GeodeClient")
        .set("cache-xml-file", "client.xml")
        .set("mcast-port", "0")
        .create();

    queryService = cache.getQueryService();

    transactions = cache.getRegion(RegionName.Transaction.name());
    suspect = cache.getRegion(RegionName.Suspect.name());

    transactions.registerInterest("ALL_KEYS");
    suspect.registerInterest("ALL_KEYS");


  }

  public String getPoSLocation(Long deviceId) {

    Query query = queryService.newQuery("select d.location from /PoS d where d.id=$1");

    try {

      Collection result = (Collection) query.execute(new Object[]{deviceId});
      if (result.size() == 0) {
        return "";
      }
      String location = (String) result.iterator()
          .next();

      return location;

    } catch (FunctionDomainException | TypeMismatchException
        | NameResolutionException | QueryInvocationTargetException e) {
      throw new RuntimeException(e);
    }


  }

  public PdxInstance getTransaction(long id) {

    Query query = queryService.newQuery("select * from /Transaction t where t.id=$1");

    try {

      Collection result = (Collection) query.execute(new Object[]{id});
      if (result.size() == 0) {
        throw new IllegalArgumentException("Couldn't find the transaction #" + id);
      }
      return (PdxInstance) result.iterator()
          .next();

    } catch (FunctionDomainException | TypeMismatchException
        | NameResolutionException | QueryInvocationTargetException e) {
      throw new RuntimeException(e);
    }

  }


}
