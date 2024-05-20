package com.adaptivescale.rosetta.ddl.extensions.table;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.DDLExtensionTable;

@RosettaModule(
        name = "SQL",
        type = RosettaModuleTypes.DDL_EXTENSION_COLUMN
)
public class DDLTableChangeTest implements DDLExtensionTable {

    @Override
    public String preCreateTable(Table table, Object action) {
        return "preCreateTable";
    }

    @Override
    public String postCreateTable(Table table, Object action) {
        return "postCreateTable";
    }

    @Override
    public String preDropTable(Table actual, Object action) {
        return "preDropTable";
    }

    @Override
    public String postDropTable(Table actual, Object action) {
        return "postDropTable";
    }

    @Override
    public String preAlterTable(Table expected, Object action) {
        return "preAlterTable";
    }

    @Override
    public String postAlterTable(Table expected, Object action) {
        return "postAlterTable";
    }
}