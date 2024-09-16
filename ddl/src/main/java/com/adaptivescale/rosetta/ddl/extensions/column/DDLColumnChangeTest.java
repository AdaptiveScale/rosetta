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
    public String preCreateColumn(Column column, Object action) {
        return action.toString();
    }

    @Override
    public String postCreateColumn(Column column, Object action) {
        return action.toString();
    }

    @Override
    public String preDropColumn(Column column, Object action) {
        return action.toString();
    }

    @Override
    public String postDropColumn(Column column, Object action) {
        return action.toString();
    }

    @Override
    public String preAlterColumn(Column column, Object action) {
        return action.toString();
    }

    @Override
    public String postAlterColumn(Column column, Object action) {
        return action.toString();
    }
}
