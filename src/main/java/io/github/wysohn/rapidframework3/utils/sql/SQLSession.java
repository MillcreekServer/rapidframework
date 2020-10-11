package io.github.wysohn.rapidframework3.utils.sql;

import io.github.wysohn.rapidframework3.utils.Validation;

import java.io.File;
import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SQLSession {
    private final String url;
    private final Properties properties;
    private Connection connection;
    private boolean autoCommit;

    private SQLSession(String url, Properties properties) throws SQLException {
        this.url = url;
        this.properties = properties;
        reconnect();
    }

    private void reconnect() throws SQLException {
        connection = DriverManager.getConnection(url, properties);
        connection.setAutoCommit(autoCommit);
    }

    public void execute(String sql, Consumer<PreparedStatement> fn, Consumer<Long> fnResult) {
        execute(sql, fn, fnResult, Statement.RETURN_GENERATED_KEYS);
    }

    public void execute(String sql, Consumer<PreparedStatement> fn, Consumer<Long> fnResult, int autoGeneratedKeys) {
        try (PreparedStatement stmt = connection.prepareStatement(sql, autoGeneratedKeys)) {
            fn.accept(stmt);
            long result = stmt.executeUpdate();
            if (result < 1) {
                fnResult.accept(0L);
                return;
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next())
                    fnResult.accept(rs.getLong(1));
                else
                    fnResult.accept(result);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (SQLException ex) {
            if (ex.getSQLState() == null) {
                ex.printStackTrace();
                return;
            }

            switch (ex.getSQLState()) {
                case "08003":
                case "08006":
                    try {
                        reconnect();
                        execute(sql, fn, fnResult);
                    } catch (SQLException ex2) {
                        ex2.printStackTrace();
                    }
                    break;
                default:
                    ex.printStackTrace();
            }
        }
    }

    public void execute(String sql) throws SQLException {
        execute(sql, pstmt -> {
        }, id -> {
        });
    }

    public void commit() throws SQLException {
        if (!autoCommit)
            connection.commit();
    }

    public void rollback() throws SQLException {
        if (!autoCommit)
            connection.rollback();
    }

    public Savepoint saveState() throws SQLException {
        if (autoCommit)
            throw new RuntimeException("autoCommit is on");

        return connection.setSavepoint();
    }

    public void restoreState(Savepoint state) throws SQLException {
        if (autoCommit)
            throw new RuntimeException("autoCommit is on");

        connection.rollback(state);
    }

    public void query(String sql, Consumer<PreparedStatement> fn, Consumer<ResultSet> fnResult) {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            fn.accept(stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                fnResult.accept(rs);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                reconnect();
                query(sql, fn, fnResult);
            } catch (SQLException ex2) {
                ex2.printStackTrace();
            }
        }
    }

    public static class Builder {
        private static final Function<Attribute, String> commonConverter = attribute -> {
            switch (attribute) {
                case NOT_NULL:
                    return "NOT NULL";
                case PRIMARY_KEY:
                    return "PRIMARY KEY";
                default:
                    throw new RuntimeException("Undefined attribute " + attribute);
            }
        };

        private final String url;
        private final Properties properties = new Properties();
        private final Function<Attribute, String> converter;
        private final List<TableInitializer> tableInitializers = new LinkedList<>();

        private boolean autoCommit = false;

        private Builder(String url,
                        Function<Attribute, String> converter) {
            this.url = url;
            this.converter = converter;
        }

        public static Builder sqlite(File dbFile) {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return new Builder(String.format("jdbc:sqlite:%s", dbFile.getAbsolutePath()), attribute -> {
                switch (attribute) {
                    case AUTO_INCREMENT:
                        return "AUTOINCREMENT";
                    default:
                        return commonConverter.apply(attribute);
                }
            });
        }

        public static Builder mysql(String host, String databaseName) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            return new Builder(String.format("jdbc:mysql://%s/%s", host, databaseName), attribute -> {
                switch (attribute) {
                    case AUTO_INCREMENT:
                        return "AUTO_INCREMENT";
                    default:
                        return commonConverter.apply(attribute);
                }
            });
        }

        public Builder user(String userName) {
            properties.put("user", userName);
            return this;
        }

        public Builder password(String password) {
            properties.put("password", password);
            return this;
        }

        public Builder autoCommit() {
            this.autoCommit = true;
            return this;
        }

        public Builder createTable(String tableName, Consumer<TableInitializer> consumer) {
            TableInitializer initializer = new TableInitializer(tableName);
            consumer.accept(initializer);
            tableInitializers.add(initializer);
            return this;
        }

        public SQLSession build() throws SQLException {
            SQLSession sqlSession = new SQLSession(url, properties);
            sqlSession.autoCommit = autoCommit;
            tableInitializers.forEach(initializer -> initializer.execute(sqlSession.connection));
            return sqlSession;
        }

        public class TableInitializer {
            private final String tableName;

            private boolean ifNotExist;
            private List<String> fields = new LinkedList<>();

            public TableInitializer(String tableName) {
                this.tableName = tableName;
            }

            public TableInitializer ifNotExist() {
                this.ifNotExist = true;
                return this;
            }

            public TableInitializer field(String fieldName, String type, Attribute... others) {
                fields.add(fieldName + " " + type + " " + Arrays.stream(others)
                        .map(converter)
                        .collect(Collectors.joining(" ")));
                return this;
            }

            public TableInitializer field(String plainText) {
                fields.add(plainText);
                return this;
            }

            private void execute(Connection conn) {
                Validation.validate(fields.size(), val -> val > 0, "at least one field is required.");

                String sql = "CREATE TABLE";
                if (ifNotExist)
                    sql += " IF NOT EXISTS";
                sql += " " + tableName + "(";
                sql += String.join(",", fields);
                sql += ");";

                try (PreparedStatement newTableStmt = conn.prepareStatement(sql)) {
                    newTableStmt.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();

                }
            }
        }
    }

    public enum Attribute {
        AUTO_INCREMENT, NOT_NULL, PRIMARY_KEY
    }
}
