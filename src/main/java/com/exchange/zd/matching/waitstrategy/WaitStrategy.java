package com.exchange.zd.matching.waitstrategy;

public interface WaitStrategy {

  void idle();

  void idle(int ms);
}
