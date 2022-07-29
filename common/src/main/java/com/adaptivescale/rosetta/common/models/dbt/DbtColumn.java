package com.adaptivescale.rosetta.common.models.dbt;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DbtColumn {
  private String name;
  private String description;
  private Collection<String> tests;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Collection<String> getTests() {
    return tests;
  }

  public void setTests(Collection<String> tests) {
    this.tests = tests;
  }
}