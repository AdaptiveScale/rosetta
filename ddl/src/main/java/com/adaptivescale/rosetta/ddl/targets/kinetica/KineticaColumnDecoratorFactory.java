package com.adaptivescale.rosetta.ddl.targets.kinetica;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.targets.kinetica.decorators.KineticaColumnTypeName;
import com.adaptivescale.rosetta.ddl.targets.postgres.decorators.DefaultPostgresColumnSQLDecorator;

public class KineticaColumnDecoratorFactory implements ColumnSQLDecoratorFactory {
    @Override
    public ColumnSQLDecorator decoratorFor(Column column) {
        ColumnDataTypeName columnDataTypeName = new KineticaColumnTypeName();
        return new DefaultPostgresColumnSQLDecorator(column, columnDataTypeName);
    }
}
