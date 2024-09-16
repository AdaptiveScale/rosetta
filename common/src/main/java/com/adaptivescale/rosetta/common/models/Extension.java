package com.adaptivescale.rosetta.common.models;


import com.adaptivescale.rosetta.common.models.enums.ExtensionTypesEnum;

import java.util.Map;

public class Extension {
  private String name;
  private ExtensionTypesEnum type;
  private Map<String,Object> actions;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public ExtensionTypesEnum getType() {
    return type;
  }

  public void setType(ExtensionTypesEnum type) {
    this.type = type;
  }

  public Map<String, Object> getActions() {
    return actions;
  }

  public void setActions(Map<String, Object> actions) {
    this.actions = actions;
  }
}
