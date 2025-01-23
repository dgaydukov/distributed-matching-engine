package com.exchange.zd.matching.waitstrategy;

public class SleepWaitStrategy implements WaitStrategy {

  @Override
  public void idle() {
    idle(1);
  }

  @Override
  public void idle(int ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }
}
