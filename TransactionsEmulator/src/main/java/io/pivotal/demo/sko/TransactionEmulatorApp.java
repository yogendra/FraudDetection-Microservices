package io.pivotal.demo.sko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;


@SpringBootApplication
@EnableAsync
public class TransactionEmulatorApp {

  public static void main(String[] args) throws Exception {
    SpringApplication.run(TransactionEmulatorApp.class, args);
  }
}
