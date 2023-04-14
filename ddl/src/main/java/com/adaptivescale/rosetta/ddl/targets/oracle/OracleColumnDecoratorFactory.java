package com.adaptivescale.rosetta.ddl.targets.oracle;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.targets.oracle.decorators.DefaultOracleColumnSQLDecorator;

public class OracleColumnDecoratorFactory implements ColumnSQLDecoratorFactory {

    @Override
    public ColumnSQLDecorator decoratorFor(Column column) {
        ColumnDataTypeName columnDataTypeName = new ColumnDataTypeName() {};
        return new DefaultOracleColumnSQLDecorator(column, columnDataTypeName);
    }
}
