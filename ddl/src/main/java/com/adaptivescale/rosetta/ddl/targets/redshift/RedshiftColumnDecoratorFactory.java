package com.adaptivescale.rosetta.ddl.targets.redshift;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.targets.redshift.decorators.DefaultRedshiftColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.redshift.decorators.RedshiftColumnTypeName;

public class RedshiftColumnDecoratorFactory implements ColumnSQLDecoratorFactory {

    @Override
    public ColumnSQLDecorator decoratorFor(Column column) {
        ColumnDataTypeName columnDataTypeName = new RedshiftColumnTypeName();
        return new DefaultRedshiftColumnSQLDecorator(column, columnDataTypeName);
    }

}
