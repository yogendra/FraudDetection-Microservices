package io.pivotal.demo.sko.ui;


import static org.slf4j.LoggerFactory.getLogger;

import io.pivotal.demo.sko.util.GeodeClient;
import io.pivotal.demo.sko.util.TransactionsMap;
import org.slf4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/controller")
public class Controller {

  static GeodeClient client;


  @RequestMapping(value = "/getTransactionsMap")
  public @ResponseBody
  TransactionsMap getDeviceMap() {

    TransactionsMap latestTransactions = TransactionsMap.latestTransactions;
    synchronized (latestTransactions) {
      logStats();
      TransactionsMap map = new TransactionsMap(latestTransactions.getTransactions());
      latestTransactions.clearAll();
      return map;
    }


  }


  @RequestMapping(value = "/getSuspiciousTransactionsMap")
  public @ResponseBody
  TransactionsMap getSuspeciousMap() {
    TransactionsMap suspeciousTransactions = TransactionsMap.suspiciousTransactions;
    synchronized (suspeciousTransactions) {
      logStats();
      TransactionsMap map = new TransactionsMap(suspeciousTransactions.getTransactions());
      suspeciousTransactions.clearAll();
      return map;
    }

  }

  private static final Logger logger = getLogger(Controller.class);
  private void logStats(){
      logger.info("Txn: {}, sTxn: {}", TransactionsMap.latestTransactions.getTransactions().size(), TransactionsMap.suspiciousTransactions.getTransactions().size());
  }


}