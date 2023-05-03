package com.adaptivescale.rosetta.ddl.targets.oracle;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.targets.oracle.decorators.DefaultOracleColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.oracle.decorators.OracleColumnTypeName;

public class OracleColumnDecoratorFactory implements ColumnSQLDecoratorFactory {

    @Override
    public ColumnSQLDecorator decoratorFor(Column column) {
        ColumnDataTypeName columnDataTypeName = new OracleColumnTypeName();
        return new DefaultOracleColumnSQLDecorator(column, columnDataTypeName);
    }
}
