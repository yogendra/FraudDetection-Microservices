package io.pivotal.demo.sko;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/emulator")
public class EmulatorController {

  private Emulator emulator;

  @Autowired
  public EmulatorController(Emulator emulator) {
    this.emulator = emulator;
  }

  @RequestMapping("/post-transactions")
  @ResponseBody
  public String postTransactions() {
    emulator.postTransactions();
    return "Posting transactions";
  }

  @RequestMapping(value="/post-transactions", params = {"count"})
  @ResponseBody
  public String postTransactions(@RequestParam("count") int count) {
    emulator.postTransactions(count);
    return "Posting transactions";
  }


  @RequestMapping("/post-suspect")
  @ResponseBody
  public String postSuspect() {
    emulator.postSuspectTransactions();
    return "Posting suspect transactions";
  }


  @RequestMapping(value="/post-suspect", params = {"count"})
  @ResponseBody
  public String postSuspect(@RequestParam("count") int count) {
    emulator.postSuspectTransactions(count);
    return "Posting suspect transactions";
  }


  @RequestMapping("/setup")
  @ResponseBody
  public String setup() throws IOException {
    emulator.setup();
    return "Completed Setup";
  }


}
