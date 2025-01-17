/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2023 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ext.mysql.model;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPEvaluationContext;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableForeignKey;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSEntityAssociation;
import org.jkiss.dbeaver.model.struct.DBSEntityAttributeRef;
import org.jkiss.dbeaver.model.struct.DBSEntityReferrer;
import org.jkiss.dbeaver.model.struct.rdb.DBSForeignKeyModifyRule;
import org.jkiss.dbeaver.model.struct.rdb.DBSTableForeignKeyColumn;

import java.util.ArrayList;
import java.util.List;

/**
 * GenericForeignKey
 */
public class MySQLTableForeignKey extends JDBCTableForeignKey<MySQLTable, MySQLTableForeignKeyColumn, MySQLTableConstraint>
{
    private List<MySQLTableForeignKeyColumn> columns;

    public MySQLTableForeignKey(
        MySQLTable table,
        String name,
        String remarks,
        MySQLTableConstraint referencedKey,
        DBSForeignKeyModifyRule deleteRule,
        DBSForeignKeyModifyRule updateRule,
        boolean persisted)
    {
        super(table, name, remarks, referencedKey, deleteRule, updateRule, persisted);
    }

    // Copy constructor
    public MySQLTableForeignKey(DBRProgressMonitor monitor, MySQLTable table, DBSEntityAssociation source) throws DBException {
        super(
            monitor,
            table,
            source,
            false);
        if (source instanceof DBSEntityReferrer) {
            List<? extends DBSEntityAttributeRef> columns = ((DBSEntityReferrer) source).getAttributeReferences(monitor);
            if (columns != null) {
                this.columns = new ArrayList<>(columns.size());
                for (DBSEntityAttributeRef srcCol : columns) {
                    if (srcCol instanceof DBSTableForeignKeyColumn) {
                        DBSTableForeignKeyColumn fkCol = (DBSTableForeignKeyColumn) srcCol;
                        this.columns.add(new MySQLTableForeignKeyColumn(
                            this,
                            table.getAttribute(monitor, fkCol.getName()),
                            this.columns.size(),
                            table.getAttribute(monitor, fkCol.getReferencedColumn().getName())));
                    }
                }
            }
        }
    }

    @Override
    public List<MySQLTableForeignKeyColumn> getAttributeReferences(DBRProgressMonitor monitor)
    {
        return columns;
    }

    @Override
    public void setAttributeReferences(List<MySQLTableForeignKeyColumn> columns) throws DBException {
        this.columns = columns;
    }

    @NotNull
    @Override
    @Property(viewable = true, editable = true, updatable = true, listProvider = ConstraintModifyRuleListProvider.class, order = 5)
    public DBSForeignKeyModifyRule getDeleteRule() {
        return super.getDeleteRule();
    }

    @NotNull
    @Override
    @Property(viewable = true, editable = true, updatable = true, listProvider = ConstraintModifyRuleListProvider.class, order = 6)
    public DBSForeignKeyModifyRule getUpdateRule() {
        return super.getUpdateRule();
    }

    public void addColumn(MySQLTableForeignKeyColumn column)
    {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        columns.add(column);
    }

    public boolean hasColumn(MySQLTableForeignKeyColumn column) {
        if (columns != null) {
            String columnName = column.getName();
            String refName = column.getReferencedColumn().getName();
            for (MySQLTableForeignKeyColumn col : columns) {
                if (columnName.equals(col.getName()) &&
                    refName.equals(col.getReferencedColumn().getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @NotNull
    @Override
    public String getFullyQualifiedName(DBPEvaluationContext context)
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getTable().getContainer(),
            getTable(),
            this);
    }

    @NotNull
    @Override
    public MySQLDataSource getDataSource()
    {
        return getTable().getDataSource();
    }
}
