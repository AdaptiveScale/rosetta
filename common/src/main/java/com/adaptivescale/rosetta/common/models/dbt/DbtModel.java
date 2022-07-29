package com.adaptivescale.rosetta.common.models.dbt;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DbtModel {
  private Integer version;
  private Collection<DbtSource> sources;

  public Integer getVersion() {
    return version;
  }

  public void setVersion(Integer version) {
    this.version = version;
  }

  public Collection<DbtSource> getSources() {
    return sources;
  }

  public void setSources(Collection<DbtSource> sources) {
    this.sources = sources;
  }
}