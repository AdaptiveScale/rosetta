package com.adaptivescale.rosetta.ddl.change;

import com.adaptivescale.rosetta.ddl.change.model.Change;

import java.util.Comparator;

public class SnowflakeChangeComparator implements Comparator<Change<?>> {
    @Override
    public int compare(Change o1, Change o2) {
        // 1. put foreign keys changes to bottom
        // 2. put drop foreign keys above all other changes for foreign key
        if(o1.getType() == Change.Type.FOREIGN_KEY &&  o2.getType() == Change.Type.FOREIGN_KEY){
            if(o1.getStatus() == Change.Status.DROP){
                return -1;
            }

            if(o2.getStatus() == Change.Status.DROP){
                return 1;
            }
        }
        return o1.getType() == o2.getType() ? 0 : (o1.getType() != Change.Type.FOREIGN_KEY ? -1 : 1);
    }
}
