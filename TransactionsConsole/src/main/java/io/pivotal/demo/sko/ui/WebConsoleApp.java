package io.pivotal.demo.sko.ui;

import io.pivotal.demo.sko.util.GeodeClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebConsoleApp {


  public static void main(String[] args) {
    GeodeClient.getInstance();
    SpringApplication.run(WebConsoleApp.class, args);
  }
}
