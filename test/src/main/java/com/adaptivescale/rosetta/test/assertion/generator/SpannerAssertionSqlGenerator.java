package com.adaptivescale.rosetta.test.assertion.generator;

import com.adaptivescale.rosetta.common.models.Column;

public class SpannerAssertionSqlGenerator extends DefaultAssertionSqlGenerator {
  private boolean isArray(Column column) {
    return "ARRAY".equals(column.getTypeName());
  }

  @Override
  String castValue(Object value, Column column) {
    String stringValue = value == null ? null : "\"" + value + "\"";
    if (isArray(column)) {
      return stringValue;
    }
    return " CAST(" + stringValue + " as " + column.getTypeName().split("\\(")[0] + ")";
  }
}
