package com.adaptivescale.rosetta.common.types;

public enum DriverClassName {
  MYSQL("com.mysql.jdbc.Driver"),
  POSTGRES("org.postgresql.Driver"),
  BIGQUERY("com.simba.googlebigquery.jdbc42.Driver"),
  SNOWFLAKE("net.snowflake.client.jdbc.SnowflakeDriver"),
  KINETICA("com.kinetica.jdbc.Driver"),
  SPANNER("com.google.cloud.spanner.jdbc.JdbcDriver");



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
