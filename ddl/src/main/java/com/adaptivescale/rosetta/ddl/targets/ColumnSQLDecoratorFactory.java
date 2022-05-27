package com.adaptivescale.rosetta.ddl.targets;

import com.adaptivescale.rosetta.common.models.Column;

public interface ColumnSQLDecoratorFactory {
     ColumnSQLDecorator decoratorFor(Column column);
}
