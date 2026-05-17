package db;

import java.sql.*;
import model.Admin;

public class admin_DAO {

    // 根据用户名和密码查找管理员，登录验证
    public Admin login(String username, String password) {
        String sql = "SELECT id, username, password FROM admin WHERE username = ? AND password = ?";
        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Admin(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
                }
            }
        } catch (SQLException e) {
            System.err.println("[admin_DAO login] " + e.getMessage());
        }
        return null;
    }

    // 获取当前管理员信息（只有一条记录）
    public Admin getAdmin() {
        String sql = "SELECT id, username, password FROM admin WHERE id = 1";
        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return new Admin(rs.getInt("id"), rs.getString("username"), rs.getString("password"));
            }
        } catch (SQLException e) {
            System.err.println("[admin_DAO getAdmin] " + e.getMessage());
        }
        return null;
    }

    // 修改用户名
    public void updateUsername(String newUsername) {
        String sql = "UPDATE admin SET username = ? WHERE id = 1";
        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newUsername);
            ps.executeUpdate();
            System.out.println("[admin_DAO] 用户名已更新为: " + newUsername);
        } catch (SQLException e) {
            System.err.println("[admin_DAO updateUsername] " + e.getMessage());
        }
    }

    // 修改密码
    public void updatePassword(String newPassword) {
        String sql = "UPDATE admin SET password = ? WHERE id = 1";
        try (Connection conn = database_utility.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.executeUpdate();
            System.out.println("[admin_DAO] 密码已更新");
        } catch (SQLException e) {
            System.err.println("[admin_DAO updatePassword] " + e.getMessage());
        }
    }
}