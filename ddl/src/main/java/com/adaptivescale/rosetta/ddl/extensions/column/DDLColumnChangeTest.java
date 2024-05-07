package com.adaptivescale.rosetta.ddl.extensions.column;

import com.adaptivescale.rosetta.common.annotations.RosettaModule;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.DDLExtensionColumn;

@RosettaModule(
        name = "SQL",
        type = RosettaModuleTypes.DDL_EXTENSION_COLUMN
)
public class DDLColumnChangeTest implements DDLExtensionColumn {
    @Override
    public String preCreateColumn(Column table, Object action) {
        return action.toString();
    }

    @Override
    public String postCreateColumn(Column table, Object action) {
        return action.toString();
    }

    @Override
    public String preDropColumn(Column table, Object action) {
        return action.toString();
    }

    @Override
    public String postDropColumn(Column table, Object action) {
        return action.toString();
    }

    @Override
    public String preAlterColumn(Column table, Object action) {
        return action.toString();
    }

    @Override
    public String postAlterColumn(Column table, Object action) {
        return action.toString();
    }
}
