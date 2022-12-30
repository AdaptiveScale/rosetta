package com.adaptivescale.rosetta.common.types;

public enum DriverClassName {
  MYSQL("com.mysql.jdbc.Driver"),
  POSTGRES("org.postgresql.Driver");

  private String value;

  DriverClassName(final String value){
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return this.getValue();
  }
}
