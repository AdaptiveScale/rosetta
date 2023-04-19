package com.adaptivescale.rosetta.ddl.targets.db2;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.targets.db2.decorators.DB2ColumnTypeName;
import com.adaptivescale.rosetta.ddl.targets.db2.decorators.DefaultDB2ColumnSQLDecorator;

public class DB2ColumnDecoratorFactory implements ColumnSQLDecoratorFactory {

    @Override
    public ColumnSQLDecorator decoratorFor(Column column) {
        ColumnDataTypeName columnDataTypeName = new DB2ColumnTypeName();
        return new DefaultDB2ColumnSQLDecorator(column, columnDataTypeName);
    }
}
