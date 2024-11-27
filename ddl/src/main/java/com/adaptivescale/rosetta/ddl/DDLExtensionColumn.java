package com.adaptivescale.rosetta.ddl;

import com.adaptivescale.rosetta.common.models.Column;


public interface DDLExtensionColumn {

    String preCreateColumn(Column column, Object action);
    String postCreateColumn(Column column, Object action);

    String preDropColumn(Column column, Object action);
    String postDropColumn(Column column, Object action);

    String preAlterColumn(Column column, Object action);
    String postAlterColumn(Column column, Object action);

}
