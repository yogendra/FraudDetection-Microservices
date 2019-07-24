package io.pivotal.demo.sko;

import static org.slf4j.LoggerFactory.getLogger;

import io.pivotal.demo.sko.util.GeodeClient;
import io.pivotal.demo.sko.util.TransactionsMap;
import java.util.Properties;
import org.apache.geode.cache.Declarable;
import org.apache.geode.cache.EntryEvent;
import org.apache.geode.cache.util.CacheListenerAdapter;
import org.apache.geode.pdx.PdxInstance;
import org.slf4j.Logger;


public class TransactionListener extends CacheListenerAdapter
    implements Declarable {

  private static final Logger logger = getLogger(TransactionListener.class);


  @Override
  public void init(Properties arg0) {
    System.out.println("starting listener");
  }

  @Override
  public void afterCreate(EntryEvent event) {
    logger.info("afterCreate");
    transactionArrived(event);
  }

  @Override
  public void afterUpdate(EntryEvent event) {
    logger.info("afterUpdate");
    transactionArrived(event);
  }


  public void transactionArrived(EntryEvent e) {
    logger.info("in ttransactionArrived");
    Object obj = e.getNewValue();

    long transactionId;
    long deviceId;
    double value;
    long timestamp;
    if (obj instanceof PdxInstance) {

      transactionId = ((Number) ((PdxInstance) obj).getField("id")).longValue();
      deviceId = ((Number) ((PdxInstance) obj).getField("deviceId")).longValue();
      value = ((Number) ((PdxInstance) obj).getField("value")).longValue();
      timestamp = ((Number) ((PdxInstance) obj).getField("timestamp")).longValue();

      String location = GeodeClient.getInstance()
          .getPoSLocation(deviceId);

      TransactionsMap.latestTransactions.addTransaction(transactionId, value, location, timestamp);
      logger.info("recorder: id:{}, value:{}, location:{}, timestamp:{}", transactionId, value, location, timestamp);
    } else {
      throw new RuntimeException("new object is not PDX Instance.. it came as " + obj.getClass());
    }


  }


}
