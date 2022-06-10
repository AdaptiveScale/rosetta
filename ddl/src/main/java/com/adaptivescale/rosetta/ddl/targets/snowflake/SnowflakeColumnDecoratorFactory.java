package com.adaptivescale.rosetta.ddl.targets.snowflake;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.targets.mysql.decorators.DefaultMySQLColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.mysql.decorators.MySqlVarcharColumnName;
import com.adaptivescale.rosetta.ddl.targets.snowflake.decorators.SnowflakeNumericColumnSQLDecorator;

public class SnowflakeColumnDecoratorFactory implements ColumnSQLDecoratorFactory {

    @Override
    public ColumnSQLDecorator decoratorFor(Column column) {

        ColumnDataTypeName columnDataTypeName;
        if (column.getTypeName().equals("NUMERIC")) {
            columnDataTypeName = new SnowflakeNumericColumnSQLDecorator();
        } else {
            columnDataTypeName = new ColumnDataTypeName() {
            };
        }
        return new DefaultMySQLColumnSQLDecorator(column, columnDataTypeName);
    }
}
