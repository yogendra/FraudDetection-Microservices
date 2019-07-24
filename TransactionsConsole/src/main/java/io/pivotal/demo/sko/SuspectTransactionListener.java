package io.pivotal.demo.sko;

import static org.slf4j.LoggerFactory.getLogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.pivotal.demo.sko.util.GeodeClient;
import io.pivotal.demo.sko.util.TransactionsMap;
import java.util.Map;
import java.util.Properties;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.util.CacheListenerAdapter;
import org.apache.geode.pdx.PdxInstance;
import org.slf4j.Logger;


public class SuspectTransactionListener extends CacheListenerAdapter
    implements Declarable {

  private static final Logger logger = getLogger(SuspectTransactionListener.class);


  @Override
  public void init(Properties arg0) {
    logger.info("Suspect transaction listner created");
  }

  @Override
  public void afterCreate(EntryEvent event) {
    suspectTransactionFound(event);
    logger.info("afterCreate");
  }

  @Override
  public void afterUpdate(EntryEvent event) {
    suspectTransactionFound(event);
    logger.info("afterUpdate");
  }


  public void suspectTransactionFound(EntryEvent e) {
    Object obj = e.getNewValue();
    if (obj instanceof PdxInstance) {
      logger.info("handle pdx");
      handlePdx((PdxInstance) obj);
    } else {
      logger.info("handle json");
      handleJson(obj.toString());
    }
  }

  private void handlePdx(PdxInstance obj) {
    long transactionId = ((Number) obj.getField("id")).longValue();
    long deviceId = ((Number) obj.getField("deviceId")).longValue();
    double value = ((Number) obj.getField("value")).longValue();
    long timestamp = ((Number) obj.getField("timestamp")).longValue();

    String location = GeodeClient.getInstance()
        .getPoSLocation(deviceId);

    TransactionsMap.suspiciousTransactions.addTransaction(transactionId, value, location, true, timestamp);
    logger.info("Received a suspect txn: id:{}, value:{}, location:{}, ts:{}", transactionId, value, location, timestamp);

  }

  private void handleJson(String value) {

    // parse

    ObjectMapper mapper = new ObjectMapper();
    Map<String, Object> objects = null;

    try {
      objects = mapper.readValue(value, Map.class);


    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    long transactionId;
    long deviceId;
    double transactionValue;
    long timestamp;

    transactionId = Long.parseLong(objects.get("id")
        .toString());
    try {
      deviceId = Long.parseLong(objects.get("deviceId")
          .toString());
      transactionValue = Double.parseDouble(objects.get("value")
          .toString());
      timestamp = Long.parseLong(objects.get("timestamp")
          .toString());
      String location = GeodeClient.getInstance()
          .getPoSLocation(deviceId)
          .trim();

      TransactionsMap.suspiciousTransactions.addTransaction(transactionId, transactionValue, location, true, timestamp);
      logger.info("Received a suspect txn: id:{}, value:{}, location:{}, ts:{}", transactionId, transactionValue, location, timestamp);
    } catch (IllegalArgumentException ie) {
      // This usually means a suspect based on a transaction row not available anymore in Gem (for example, expired)
      // ignore.
    }
  }


}
