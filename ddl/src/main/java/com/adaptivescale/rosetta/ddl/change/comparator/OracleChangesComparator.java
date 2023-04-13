package com.adaptivescale.rosetta.ddl.change.comparator;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.change.model.Change;

import java.util.Comparator;

@RosettaModule(
        name = "oracle",
        type = RosettaModuleTypes.CHANGE_COMPARATOR
)
public class OracleChangesComparator implements Comparator<Change<?>> {
    @Override
    public int compare(Change changeA, Change changeB) {

        return 0;
    }
}
