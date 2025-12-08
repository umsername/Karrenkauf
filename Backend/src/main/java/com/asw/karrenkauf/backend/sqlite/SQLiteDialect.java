// NOT CURRENTLY IN USE

package com.asw.karrenkauf.backend.sqlite;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.CommonFunctionFactory;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitOffsetLimitHandler;
import org.hibernate.dialect.DatabaseVersion;

public class SQLiteDialect extends Dialect {

    public SQLiteDialect() {
        super(DatabaseVersion.make(3));
    }

    // ------------------------------------------------------------
    // TYPE MAPPINGS (BOOLEAN als INTEGER)
    // ------------------------------------------------------------
    public int getDefaultSqlTypeCode(String columnTypeName) {
        return switch (columnTypeName.toLowerCase()) {
            case "integer" -> java.sql.Types.INTEGER;
            case "text" -> java.sql.Types.VARCHAR;
            case "real" -> java.sql.Types.REAL;
            case "blob" -> java.sql.Types.BLOB;
            case "boolean" -> java.sql.Types.INTEGER; // SQLite kennt kein native BOOLEAN
            default -> java.sql.Types.VARCHAR;
        };
    }

    // ------------------------------------------------------------
    // FUNCTIONS
    // ------------------------------------------------------------
    public void initializeFunctionRegistry(FunctionContributions functionContributions) {
        CommonFunctionFactory f = new CommonFunctionFactory(functionContributions);

        f.concat();
        f.substr();
        f.substring();
        f.lowerUpper();

        functionContributions.getFunctionRegistry()
                .registerPattern("now", "current_timestamp");
    }

    // ------------------------------------------------------------
    // LIMIT / OFFSET
    // ------------------------------------------------------------
    public LimitHandler getLimitHandler() {
        return LimitOffsetLimitHandler.INSTANCE;
    }

    public boolean supportsOffsetInLimit() {
        return true;
    }

    // ------------------------------------------------------------
    // FOREIGN KEYS / ALTER TABLE
    // ------------------------------------------------------------
    public boolean supportsForeignKeyConstraints() {
        return false;
    }

    public boolean hasAlterTable() {
        return false;
    }

    public boolean dropConstraints() {
        return false;
    }

    public String getAddForeignKeyConstraintString(
            String constraintName,
            String[] foreignKey,
            String referencedTable,
            String[] primaryKey,
            boolean referencesPrimaryKey) {
        return "";
    }

    public String getDropForeignKeyString() {
        return "";
    }

    public String getAddPrimaryKeyConstraintString(String constraintName) {
        return "";
    }

    public String getAlterTableString(String tableName) {
        throw new UnsupportedOperationException("SQLite does not fully support ALTER TABLE.");
    }

    // ------------------------------------------------------------
    // IDENTITY (auto-increment)
    // ------------------------------------------------------------
    public String getIdentityColumnString(int type) {
        return "integer primary key autoincrement";

    }
    
    public String getIdentitySelectString() {
        return "select last_insert_rowid()";
    }
}
