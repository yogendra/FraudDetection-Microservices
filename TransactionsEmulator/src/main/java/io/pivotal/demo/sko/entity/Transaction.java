package io.pivotal.demo.sko.entity;


import lombok.Data;

@Data
public class Transaction {

  private long id;
  private long deviceId;
  private double value;
  private long accountId;
  private long timestamp;

}
