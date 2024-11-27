package com.adaptivescale.rosetta.ddl.change;

import com.adaptivescale.rosetta.common.helpers.ModuleLoader;
import com.adaptivescale.rosetta.common.models.Column;
import com.adaptivescale.rosetta.common.models.Extension;
import com.adaptivescale.rosetta.common.models.Table;
import com.adaptivescale.rosetta.common.models.enums.OperationTypeEnum;
import com.adaptivescale.rosetta.common.types.RosettaModuleTypes;
import com.adaptivescale.rosetta.ddl.DDL;
import com.adaptivescale.rosetta.ddl.DDLExtensionColumn;
import com.adaptivescale.rosetta.ddl.DDLExtensionTable;
import com.adaptivescale.rosetta.ddl.change.model.*;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.adaptivescale.rosetta.common.models.enums.OperationTypeEnum.*;

@Slf4j
public class ChangeHandlerImplementation implements ChangeHandler {

    private final DDL ddl;
    private final Comparator<Change<?>> changeComparator;

    public ChangeHandlerImplementation(DDL ddl, Comparator<Change<?>> changeComparator) {
        this.ddl = ddl;
        this.changeComparator = changeComparator;
    }

    @Override
    public String createDDLForChanges(List<Change<?>> changes) {
        if (changeComparator != null) {
            changes.sort(changeComparator);
        }

        List<String> ddlStatements = new ArrayList<>();
        for (Change<?> change : changes) {
            switch (change.getType()) {
                case DATABASE:
                    ddlStatements.add(onDatabaseChange((DatabaseChange) change));
                    break;
                case TABLE_SCHEMA:
                    ddlStatements.add(onTableSchemaChange((TableSchemaChange) change));
                    break;
                case TABLE:
                    ddlStatements.add(onTableChange((TableChange) change));
                    break;
                case COLUMN:
                    ddlStatements.add(onColumnChange((ColumnChange) change));
                    break;
                case FOREIGN_KEY:
                    ddlStatements.add(onForeignKeyChange((ForeignKeyChange) change));
                    break;
                case INDEX:
                    ddlStatements.add(onIndexChange((IndexChange) change));
                case VIEW:
                    ddlStatements.add(onViewChange((ViewChange) change));
            }
        }

        return String.join("\r", ddlStatements);
    }

    @Override
    public String onDatabaseChange(DatabaseChange databaseChange) {
        switch (databaseChange.getStatus()) {
            case ADD:
                return ddl.createDatabase(databaseChange.getExpected(), false);
            case ALTER:
            case DROP:
            default:
                throw new RuntimeException("Operation " + databaseChange.getStatus() + " for database not supported");
        }
    }

    @Override
    public String onTableChange(TableChange change) {
        switch (change.getStatus()) {
            case DROP:
                StringBuilder dropQueryBuilder = new StringBuilder();
                if (change.getExpected() != null && change.getExpected().getExtensions() != null) {
                    Optional<Extension> first = change.getExpected().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(PRE_DROP.name())).findFirst();
                    first.ifPresent(extension -> dropQueryBuilder.append(executeTableExtensions(change.getExpected(), PRE_DROP, extension.getActions().get(PRE_DROP.name()))));
                }

                dropQueryBuilder.append(ddl.dropTable(change.getActual()));

                if (change.getExpected() != null && change.getExpected().getExtensions() != null) {
                    Optional<Extension> first = change.getExpected().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(POST_DROP.name())).findFirst();
                    first.ifPresent(extension -> dropQueryBuilder.append(executeTableExtensions(change.getExpected(), POST_DROP, extension.getActions().get(POST_DROP.name()))));
                }
                return dropQueryBuilder.toString();
            case ADD:
                StringBuilder addQueryBuilder = new StringBuilder();
                if (change.getExpected() != null && change.getExpected().getExtensions() != null) {
                    Optional<Extension> first = change.getExpected().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(PRE_CREATE.name())).findFirst();
                    first.ifPresent(extension -> addQueryBuilder.append(executeTableExtensions(change.getExpected(), PRE_CREATE, extension.getActions().get(PRE_CREATE.name()))));
                }

                addQueryBuilder.append(ddl.createTable(change.getExpected(), false));

                if (change.getExpected() != null && change.getExpected().getExtensions() != null) {
                    Optional<Extension> first = change.getExpected().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(POST_CREATE.name())).findFirst();
                    first.ifPresent(extension -> addQueryBuilder.append(executeTableExtensions(change.getExpected(), POST_CREATE, extension.getActions().get(POST_CREATE.name()))));
                }
                return addQueryBuilder.toString();
            case ALTER:
                StringBuilder alterQueryBuilder = new StringBuilder();
                if (change.getExpected() != null && change.getExpected().getExtensions() != null) {
                    Optional<Extension> first = change.getExpected().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(PRE_ALTER.name())).findFirst();
                    first.ifPresent(extension -> alterQueryBuilder.append(executeTableExtensions(change.getExpected(), PRE_ALTER, extension.getActions().get(PRE_ALTER.name()))));
                }

                alterQueryBuilder.append(ddl.alterTable(change.getExpected(), change.getActual()));

                if (change.getExpected() != null && change.getExpected().getExtensions() != null) {
                    Optional<Extension> first = change.getExpected().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(POST_ALTER.name())).findFirst();
                    first.ifPresent(extension -> alterQueryBuilder.append(executeTableExtensions(change.getExpected(), POST_ALTER, extension.getActions().get(POST_ALTER.name()))));
                }
                return alterQueryBuilder.toString();
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for table not supported");
        }
    }

    @Override
    public String onTableSchemaChange(TableSchemaChange change) {
        switch (change.getStatus()) {
            case ADD:
                return ddl.createTableSchema(change.getExpected());
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for table not supported");
        }
    }

    @Override
    public String onColumnChange(ColumnChange change) {
        switch (change.getStatus()) {
            case ALTER:
                StringBuilder alterQueryBuilder = new StringBuilder();
                if (change.getExpected() != null && change.getExpected().getExtensions() != null) {
                    Optional<Extension> first = change.getExpected().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(PRE_ALTER.name())).findFirst();
                    first.ifPresent(extension -> alterQueryBuilder.append(executeColumnExtensions(change.getExpected(), PRE_ALTER, extension.getActions().get(PRE_ALTER.name()))));
                }

                alterQueryBuilder.append(ddl.alterColumn(change));

                if (change.getExpected() != null && change.getExpected().getExtensions() != null) {
                    Optional<Extension> first = change.getExpected().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(POST_ALTER.name())).findFirst();
                    first.ifPresent(extension -> alterQueryBuilder.append(executeColumnExtensions(change.getExpected(), POST_ALTER, extension.getActions().get(POST_ALTER.name()))));
                }
                return alterQueryBuilder.toString();
            case DROP:
                StringBuilder dropQueryBuilder = new StringBuilder();
                if (change.getActual() != null && change.getActual().getExtensions() != null) {
                    Optional<Extension> first = change.getActual().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(PRE_DROP.name())).findFirst();
                    first.ifPresent(extension -> dropQueryBuilder.append(executeColumnExtensions(change.getActual(), PRE_DROP, extension.getActions().get(PRE_DROP.name()))));
                }
                dropQueryBuilder.append(ddl.dropColumn(change));
                if (change.getActual() != null && change.getActual().getExtensions() != null) {
                    Optional<Extension> first = change.getActual().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(POST_DROP.name())).findFirst();
                    first.ifPresent(extension -> dropQueryBuilder.append(executeColumnExtensions(change.getActual(), POST_DROP, extension.getActions().get(POST_DROP.name()))));
                }
                return dropQueryBuilder.toString();
            case ADD:
                StringBuilder addQueryBuilder = new StringBuilder();
                if (change.getExpected() != null && change.getExpected().getExtensions() != null) {
                    Optional<Extension> first = change.getExpected().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(PRE_CREATE.name())).findFirst();
                    first.ifPresent(extension -> addQueryBuilder.append(executeColumnExtensions(change.getExpected(), PRE_CREATE, extension.getActions().get(PRE_CREATE.name()))));
                }
                addQueryBuilder.append(ddl.addColumn(change));

                if (change.getExpected() != null && change.getExpected().getExtensions() != null) {
                    Optional<Extension> first = change.getExpected().getExtensions().stream()
                            .filter(extension -> extension.getActions().containsKey(POST_CREATE.name())).findFirst();
                    first.ifPresent(extension -> addQueryBuilder.append(executeColumnExtensions(change.getExpected(), POST_CREATE, extension.getActions().get(POST_CREATE.name()))));
                }
                return addQueryBuilder.toString();
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for column not supported");
        }
    }

    public String executeTableExtensions(Table table, OperationTypeEnum status, Object action) {
        Optional<Class<?>> tableExtension = ModuleLoader.loadModuleByAnnotationClassValues(String.format("%s.%s",DDLExtensionTable.class.getPackageName(),"extensions.table") , RosettaModuleTypes.DDL_EXTENSION_TABLE, "SQL");
        DDLExtensionTable ddlExtensionTable = null;
        if (tableExtension.isPresent()) {
            try {
                ddlExtensionTable = (DDLExtensionTable) tableExtension.get().getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.warn("Failed to load table ddl extension %s - skipping", tableExtension.get().getName());
            }
        } else {
            log.warn("No table ddl extension found - skipping", tableExtension.get().getName());
        }
        if (ddlExtensionTable == null) {
            return "";
        }
        switch (status) {
            case PRE_CREATE:
                return ddlExtensionTable.preCreateTable(table, action);
            case PRE_ALTER:
                return ddlExtensionTable.preAlterTable(table, action);
            case PRE_DROP:
                return ddlExtensionTable.preDropTable(table, action);
            case POST_CREATE:
                return ddlExtensionTable.postCreateTable(table, action);
            case POST_ALTER:
                return ddlExtensionTable.postAlterTable(table, action);
            case POST_DROP:
                return ddlExtensionTable.postDropTable(table, action);
            default:
                return "";
        }
    }

    public String executeColumnExtensions(Column column, OperationTypeEnum status, Object action) {
        Optional<Class<?>> columnExtension = ModuleLoader.loadModuleByAnnotationClassValues(String.format("%s.%s",DDLExtensionColumn.class.getPackageName(),"extensions.column"), RosettaModuleTypes.DDL_EXTENSION_COLUMN, "SQL");
        DDLExtensionColumn ddlExtensionColumn = null;
        if (columnExtension.isPresent()) {
            try {
                ddlExtensionColumn = (DDLExtensionColumn) columnExtension.get().getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                log.warn("Failed to load column ddl extension %s - skipping", columnExtension.get().getName());
            }
        } else {
            log.warn("No column ddl extension found - skipping", columnExtension.get().getName());
        }
        if (ddlExtensionColumn == null) {
            return "";
        }
        switch (status) {
            case PRE_CREATE:
                return ddlExtensionColumn.preCreateColumn(column, action);
            case PRE_ALTER:
                return ddlExtensionColumn.preAlterColumn(column, action);
            case PRE_DROP:
                return ddlExtensionColumn.preDropColumn(column, action);
            case POST_CREATE:
                return ddlExtensionColumn.postCreateColumn(column, action);
            case POST_ALTER:
                return ddlExtensionColumn.postAlterColumn(column, action);
            case POST_DROP:
                return ddlExtensionColumn.postDropColumn(column, action);
            default:
                return "";
        }
    }

    @Override
    public String onForeignKeyChange(ForeignKeyChange change) {
        switch (change.getStatus()) {
            case ADD:
                return ddl.createForeignKey(change.getExpected());
            case ALTER:
                return ddl.alterForeignKey(change);
            case DROP:
                return ddl.dropForeignKey(change.getActual());
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for foreign key not supported");
        }
    }

    @Override
    public String onIndexChange(IndexChange change) {
        switch (change.getStatus()) {
            case ADD:
                return ddl.createIndex(change.getExpected());
            case DROP:
                return ddl.dropIndex(change.getActual());
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for index not supported");
        }
    }

    @Override
    public String onViewChange(ViewChange change) {
        switch (change.getStatus()) {
            case DROP:
                return ddl.dropView(change.getActual());
            case ADD:
                return ddl.createView(change.getExpected(), false);
            case ALTER:
                return ddl.alterView(change.getExpected(), change.getActual());
            default:
                throw new RuntimeException("Operation " + change.getStatus() + " for view not supported");
        }
    }
}