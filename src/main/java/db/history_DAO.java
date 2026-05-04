package db;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class history_DAO {

    // 添加访问记录
    public void history_add(String user_name, String url, Date visit_time) {
        String sql = "INSERT INTO history (user_name, url, visit_time) VALUES (?, ?, ?)";
        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user_name);
            ps.setString(2, url);
            ps.setTimestamp(3, new Timestamp(visit_time.getTime()));
            ps.executeUpdate();
            System.out.println("[添加访问记录成功]用户： " + user_name + " URL: " + url + " 时间: " + visit_time);
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

    // 分页搜索历史记录
    public HistoryPage search_history(int page, int size, String user, String keyword) {
        int offset = (page - 1) * size;
        List<HistoryEntry> data = new ArrayList<>();
        int total = 0;

        try (Connection conn = database_utility.getConnection()) {
            // 查询总数
            String countSql = "SELECT COUNT(*) FROM history WHERE "
                    + "(? IS NULL OR user_name LIKE '%' + ? + '%') AND "
                    + "(? IS NULL OR url LIKE '%' + ? + '%')";
            try (PreparedStatement ps = conn.prepareStatement(countSql)) {
                ps.setString(1, user == null || user.isEmpty() ? null : user);
                ps.setString(2, user == null || user.isEmpty() ? null : user);
                ps.setString(3, keyword == null || keyword.isEmpty() ? null : keyword);
                ps.setString(4, keyword == null || keyword.isEmpty() ? null : keyword);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) total = rs.getInt(1);
                }
            }

            // 查询分页数据
            String dataSql = "SELECT * FROM history WHERE "
                    + "(? IS NULL OR user_name LIKE '%' + ? + '%') AND "
                    + "(? IS NULL OR url LIKE '%' + ? + '%') "
                    + "ORDER BY visit_time DESC "
                    + "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
            try (PreparedStatement ps = conn.prepareStatement(dataSql)) {
                ps.setString(1, user == null || user.isEmpty() ? null : user);
                ps.setString(2, user == null || user.isEmpty() ? null : user);
                ps.setString(3, keyword == null || keyword.isEmpty() ? null : keyword);
                ps.setString(4, keyword == null || keyword.isEmpty() ? null : keyword);
                ps.setInt(5, offset);
                ps.setInt(6, size);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        data.add(new HistoryEntry(
                                rs.getInt("id"),
                                rs.getString("user_name"),
                                rs.getString("url"),
                                rs.getTimestamp("visit_time")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[查询历史失败]" + e.getMessage());
        }
        return new HistoryPage(total, data);
    }

    // 获取所有黑名单
    public List<BlacklistEntry> blacklist_list() {
        List<BlacklistEntry> list = new ArrayList<>();
        String sql = "SELECT id, url, add_time FROM Blacklist ORDER BY id DESC";
        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new BlacklistEntry(
                        rs.getInt("id"),
                        rs.getString("url"),
                        rs.getTimestamp("add_time")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[查询黑名单失败]" + e.getMessage());
        }
        return list;
    }

    // 添加黑名单
    public int blacklist_add(String url) {
        String sql = "INSERT INTO Blacklist (url) VALUES (?)";
        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, url);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[添加黑名单失败]" + e.getMessage());
        }
        return -1;
    }

    // 删除黑名单
    public boolean blacklist_delete(int id) {
        String sql = "DELETE FROM Blacklist WHERE id = ?";
        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[删除黑名单失败]" + e.getMessage());
        }
        return false;
    }

    // 历史记录条目
    public static class HistoryEntry {
        public final int id;
        public final String userName;
        public final String url;
        public final Date visitTime;

        HistoryEntry(int id, String userName, String url, Date visitTime) {
            this.id = id;
            this.userName = userName;
            this.url = url;
            this.visitTime = visitTime;
        }
    }

    // 分页结果
    public static class HistoryPage {
        public final int total;
        public final List<HistoryEntry> data;

        HistoryPage(int total, List<HistoryEntry> data) {
            this.total = total;
            this.data = data;
        }
    }

    // 黑名单条目
    public static class BlacklistEntry {
        public final int id;
        public final String url;
        public final Date addTime;

        BlacklistEntry(int id, String url, Date addTime) {
            this.id = id;
            this.url = url;
            this.addTime = addTime;
        }
    }
}
