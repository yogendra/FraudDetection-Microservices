package io.pivotal.demo.sko.entity;

import lombok.Data;

@Data
public class Suspect {

  private long transactionId;
  private long deviceId;
  private long markedSuspectMillis;
  private String reason;


}
