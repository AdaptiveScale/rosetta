package com.adaptivescale.rosetta.ddl;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Table;


public interface DDLExtensionColumn {

    String preCreateColumn(Column table, Object action);
    String postCreateColumn(Column table, Object action);

    String preDropColumn(Column table, Object action);
    String postDropColumn(Column table, Object action);

    String preAlterColumn(Column table, Object action);
    String postAlterColumn(Column table, Object action);

}
