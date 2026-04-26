package db;

import java.sql.*;
import java.util.Date;

public class history_DAO {
    // 添加访问记录
    public void history_add(String user_name, String url, Date visit_time) {
        String sql = "INSERT INTO history (user_name, url, visit_time) VALUES (?, ?, ?)"; // ?用于防止SQL注入攻击

        try (Connection conn = database_utility.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)
        // 预编译语句
        ) {
            // 补全SQL语句并执行
            ps.setString(1, user_name);
            ps.setString(2, url);
            ps.setTimestamp(3, new Timestamp(visit_time.getTime()));
            ps.executeUpdate();

            System.out.println("[添加访问记录成功]" + "用户： " + user_name + " URL: " + url + " 时间: " + visit_time);
        } catch (SQLException e) {
            System.err.println("[添加访问记录失败]" + e.getMessage());
        }
    }

    // 黑名单检测
    public boolean is_blacklisted(String url) {
        String sql = "SELECT COUNT(*) FROM Blacklist WHERE ? LIKE '%' + url + '%'";
        try (Connection conn = database_utility.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, url);

            // 结果集存储，第一行如果有数据就说明存在于黑名单中
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[黑名单检测失败]" + e.getMessage());
        }
        return false;
    }

    // 查询最近某个/所有用户n条访问记录
    public ResultSet show_history(int n, String user_name) throws SQLException {
        // 使用了conn这些会抛出异常的方法，且ResultSet要返回调用方，需要throws SQLException来声明异常
        String sql;
        PreparedStatement ps;
        Connection conn = database_utility.getConnection();

        if (user_name == null || user_name.isEmpty()) {
            sql = "SELECT TOP " + n + " * FROM WebHistory ORDER BY visit_time DESC";
            // TOP不支持 ?占位符
            ps = conn.prepareStatement(sql);
        } else {
            sql = "SELECT TOP " + n + " * FROM WebHistory WHERE user_name = ? ORDER BY visit_time DESC";
            ps = conn.prepareStatement(sql);
            ps.setString(1, user_name);
        }

        return ps.executeQuery();
    }
}
