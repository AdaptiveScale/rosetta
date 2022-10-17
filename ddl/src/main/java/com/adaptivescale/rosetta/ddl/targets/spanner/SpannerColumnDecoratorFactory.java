package com.adaptivescale.rosetta.ddl.targets.spanner;

import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.ddl.targets.ColumnDataTypeName;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecorator;
import com.adaptivescale.rosetta.ddl.targets.ColumnSQLDecoratorFactory;
import com.adaptivescale.rosetta.ddl.targets.spanner.decorators.DefaultSpannerColumnDecorator;
import com.adaptivescale.rosetta.ddl.targets.spanner.decorators.SpannerColumnTypeName;

public class SpannerColumnDecoratorFactory implements ColumnSQLDecoratorFactory {
    @Override
    public ColumnSQLDecorator decoratorFor(Column column) {
        ColumnDataTypeName columnDataTypeName = new SpannerColumnTypeName();
        return new DefaultSpannerColumnDecorator(column, columnDataTypeName);
    }
}
