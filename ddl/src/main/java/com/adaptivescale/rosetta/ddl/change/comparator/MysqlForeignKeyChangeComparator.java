package com.adaptivescale.rosetta.ddl.change.comparator;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.change.model.Change;

import java.util.Comparator;

@RosettaModule(
        name = "mysql",
        type = RosettaModuleTypes.CHANGE_COMPARATOR
)
public class MysqlForeignKeyChangeComparator implements Comparator<Change<?>> {

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

        boolean isFKChangeA = changeA.getType() == Change.Type.FOREIGN_KEY;
        boolean isFKChangeB = changeB.getType() == Change.Type.FOREIGN_KEY;

        boolean isFKDropChangeA = isFKChangeA && changeA.getStatus() == Change.Status.DROP;
        boolean isFKDropChangeB = isFKChangeB && changeB.getStatus() == Change.Status.DROP;

        if (isFKDropChangeA && isFKDropChangeB) {
            return 0;
        }

        if (isFKDropChangeA) {
            return -1;
        }

        if (isFKDropChangeB) {
            return 1;
        }

        //now changes are not ForeignKey with drop status
        if (isFKChangeA && isFKChangeB) {
            return 0;
        }

        if (isFKChangeA) {
            return 1;
        }

        if (isFKChangeB) {
            return -1;
        }

        //if change is drop, put ahead of all other changes
        if (changeA.getStatus() == Change.Status.DROP) {
            return -1;
        }

        //other changes we don't care
        return 0;
    }
}
