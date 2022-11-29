package com.adaptivescale.rosetta.common.models.test;

import com.adaptivescale.rosetta.common.models.AssertTest;

public class AssertionResult {
  private long startTime;
  private String sqlExecuted;
  private String result;
  private boolean pass = false;
  AssertTest assertTest;

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public String getSqlExecuted() {
    return sqlExecuted;
  }

  public void setSqlExecuted(String sqlExecuted) {
    this.sqlExecuted = sqlExecuted;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public boolean isPass() {
    return pass;
  }

  public void setPass(boolean pass) {
    this.pass = pass;
  }

  public AssertTest getAssertTest() {
    return assertTest;
  }

  public void setAssertTest(AssertTest assertTest) {
    this.assertTest = assertTest;
  }
}
