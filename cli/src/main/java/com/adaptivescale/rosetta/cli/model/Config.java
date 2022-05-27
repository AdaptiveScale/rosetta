package com.adaptivescale.rosetta.cli.model;

import com.adaptivescale.rosetta.common.models.input.Target;

import java.util.List;
import java.util.Optional;


public class Config {
    private List<Target> targets;

    public List<Target> getTargets() {
        return targets;
    }

    public void setTargets(List<Target> target) {
        this.targets = target;
    }

    public Optional<Target> getTarget(String name) {
        return targets.stream().filter(target -> target.getTargetName().equals(name)).findFirst();
    }
}
