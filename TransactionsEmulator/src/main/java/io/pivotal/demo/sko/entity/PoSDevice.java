package io.pivotal.demo.sko.entity;


import lombok.Data;

@Data
public class PoSDevice {

  private long id;
  private String location;
  /* location can be a ZIP code or county name, that would be mapped on D3 US MAP.
   * Good transactions would be green, possible frauds would be red.
   * Also a box in a side showing stats about frauds:  number total, per state, etc.
   * http://bl.ocks.org/mbostock/4965422
   * http://bl.ocks.org/mbostock/9943478
   * http://www.scriptscoop.net/t/ec57d05b18cd/d3.js-dynamically-adjust-bubble-radius-of-counties.html
   */
  private String merchantName;

}
