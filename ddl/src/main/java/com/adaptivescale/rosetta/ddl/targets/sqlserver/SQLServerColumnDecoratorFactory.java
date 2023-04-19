package com.adaptivescale.rosetta.ddl.targets.sqlserver;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.targets.sqlserver.decorators.DefaultSQLServerColumnSQLDecorator;

public class SQLServerColumnDecoratorFactory implements ColumnSQLDecoratorFactory {

    @Override
    public ColumnSQLDecorator decoratorFor(Column column) {
        ColumnDataTypeName columnDataTypeName = new ColumnDataTypeName() {};
        return new DefaultSQLServerColumnSQLDecorator(column, columnDataTypeName);
    }
}
