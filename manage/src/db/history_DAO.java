package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import model.History;
import model.Blacklist;

public class history_DAO {
    // 添加访问记录
    public void history_add(String user_name, String url, Date visit_time) {
        String sql = "INSERT INTO history (user_name, url, visit_time) VALUES (?, ?, ?)";  // ?用于防止SQL注入攻击

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
    public List<History> getHistoryList(String userName, String urlKeyword) {
        List<History> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, user_name, url, visit_time FROM history WHERE 1=1");
        List<String> params = new ArrayList<>();

        if (userName != null && !userName.trim().isEmpty()) {
            sql.append(" AND user_name LIKE ?");
            params.add("%" + userName.trim() + "%");
        }
        if (urlKeyword != null && !urlKeyword.trim().isEmpty()) {
            sql.append(" AND url LIKE ?");
            params.add("%" + urlKeyword.trim() + "%");
        }
        sql.append(" ORDER BY visit_time DESC");

        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new History(
                        rs.getInt("id"),
                        rs.getString("user_name"),
                        rs.getString("url"),
                        rs.getTimestamp("visit_time")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[getHistoryList error] " + e.getMessage());
        }
        return list;
    }

    public List<Blacklist> getBlacklistAll() {
        List<Blacklist> list = new ArrayList<>();
        String sql = "SELECT id, url, add_time FROM Blacklist ORDER BY add_time DESC";

        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new Blacklist(
                    rs.getInt("id"),
                    rs.getString("url"),
                    rs.getTimestamp("add_time")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[getBlacklistAll error] " + e.getMessage());
        }
        return list;
    }

    public void blacklist_add(String url) {
        String sql = "INSERT INTO Blacklist (url) VALUES (?)";
        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, url);
            ps.executeUpdate();
            System.out.println("[blacklist_add] " + url);
        } catch (SQLException e) {
            System.err.println("[blacklist_add error] " + e.getMessage());
        }
    }

    public void blacklist_delete(int id) {
        String sql = "DELETE FROM Blacklist WHERE id = ?";
        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("[blacklist_delete] id=" + id);
            }
        } catch (SQLException e) {
            System.err.println("[blacklist_delete error] " + e.getMessage());
        }
    }
}
