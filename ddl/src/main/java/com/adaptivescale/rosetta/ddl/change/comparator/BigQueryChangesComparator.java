package com.adaptivescale.rosetta.ddl.change.comparator;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.change.model.Change;

import java.util.Comparator;

@RosettaModule(
        name = "bigquery",
        type = RosettaModuleTypes.CHANGE_COMPARATOR
)
public class BigQueryChangesComparator implements Comparator<Change<?>> {


    @Override
    public int compare(Change changeA, Change changeB) {
        if (changeA.getStatus() == Change.Status.DROP) {
            return -1;
        }

        return 0;
    }

}
