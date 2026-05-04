package db;

import java.sql.*;
import config.ConfigLoader;

public class database_utility {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    static {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC driver load failed: " + e.getMessage());
        }

        URL = ConfigLoader.get("db.url",
                "jdbc:sqlserver://localhost:1433;databaseName=WebTrackDB;encrypt=false;trustServerCertificate=true;");
        USER = ConfigLoader.get("db.user", "sa");
        PASSWORD = ConfigLoader.get("db.password", "123456");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // 关闭数据库资源
    public static void close(Connection conn, PreparedStatement ps, ResultSet rs) {
        try {
            if (rs != null)
                rs.close();
        } catch (SQLException e) {
        }
        try {
            if (ps != null)
                ps.close();
        } catch (SQLException e) {
        }
        try {
            if (conn != null)
                conn.close();
        } catch (SQLException e) {
        }
    }
}
