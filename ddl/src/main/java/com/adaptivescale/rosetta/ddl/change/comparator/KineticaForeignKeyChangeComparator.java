package com.adaptivescale.rosetta.ddl.change.comparator;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.change.model.Change;

import java.util.Comparator;

@RosettaModule(
        name = "kinetica",
        type = RosettaModuleTypes.CHANGE_COMPARATOR
)
public class KineticaForeignKeyChangeComparator implements Comparator<Change<?>> {

    /**
     * ForeignKeys with status drop put at first
     * ForeignKeys with status alter or add put at bottom
     *
     * @param changeA firstChange
     * @param changeB second Change
     * @return -1 0 1
     */
    @Override
    public int compare(Change changeA, Change changeB) {
        return 0;
    }
}
