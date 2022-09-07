package com.adaptivescale.rosetta.ddl.change.comparator;

import com.adaptivescale.rosetta.ddl.change.model.Change;

import java.util.Comparator;

public class SnowflakeChangesComparator implements Comparator<Change<?>> {
    /**
     * Put foreign keys changes to bottom then put drop foreign keys above all other changes for foreign key
     * Put Column Add before Table Alter, when adding column with PK, PK of column is Table change
     *
     * @param changeA firstChange
     * @param changeB second Change
     * @return -1 0 1
     */
    @Override
    public int compare(Change changeA, Change changeB) {
        boolean isFKChangeA = changeA.getType() == Change.Type.FOREIGN_KEY;
        boolean isFKChangeB = changeB.getType() == Change.Type.FOREIGN_KEY;


        if (isFKChangeA && isFKChangeB) {
            if (changeA.getStatus() == Change.Status.DROP) {
                return -1;
            }

            if (changeB.getStatus() == Change.Status.DROP) {
                return 1;
            }
        }

        if (changeA.getType() == changeB.getType()) {
            return 0;
        }

        if (isFKChangeA) {
            return 1;
        }

        if (isFKChangeB) {
            return -1;
        }

        boolean isColumnAddChangeA = changeA.getType() == Change.Type.COLUMN && changeA.getStatus() == Change.Status.ADD;
        boolean isColumnAddChangeB = changeB.getType() == Change.Type.COLUMN && changeB.getStatus() == Change.Status.ADD;

        if(isColumnAddChangeA && isColumnAddChangeB){
            return 0;
        }

        if(isColumnAddChangeA){
            return -1;
        }

        if(isColumnAddChangeB){
            return 1;
        }


        return 0;


    }
}
