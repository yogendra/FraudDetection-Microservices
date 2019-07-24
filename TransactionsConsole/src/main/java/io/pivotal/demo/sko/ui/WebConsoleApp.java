package io.pivotal.demo.sko.ui;

import static org.slf4j.LoggerFactory.getLogger;

import io.pivotal.demo.sko.util.GeodeClient;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebConsoleApp {

  private static final Logger logger = getLogger(WebConsoleApp.class);


  public static void main(String[] args) {
    logger.info("Starting application");
    SpringApplication.run(WebConsoleApp.class, args);
    logger.info("App Started");
    logger.info("Initializing Geode Client");
    GeodeClient.getInstance();
    logger.info("Finished initializing Geode Client");
  }

}
