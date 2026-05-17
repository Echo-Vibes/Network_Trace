package service;

import db.admin_DAO;
import model.Admin;

public class AdminService {
    private admin_DAO dao = new admin_DAO();

    // 登录验证，成功返回 Admin 对象，失败返回 null
    public Admin login(String username, String password) {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return null;
        }
        return dao.login(username.trim(), password.trim());
    }

    public Admin getAdmin() {
        return dao.getAdmin();
    }

    // 修改用户名
    public void updateUsername(String newUsername) {
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            dao.updateUsername(newUsername.trim());
        }
    }

    // 修改密码（需验证旧密码）
    public String updatePassword(String oldPassword, String newPassword) {
        Admin admin = dao.getAdmin();
        if (admin == null) {
            return "系统错误";
        }
        if (!admin.getPassword().equals(oldPassword)) {
            return "旧密码错误";
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return "新密码不能为空";
        }
        dao.updatePassword(newPassword.trim());
        return "密码修改成功";
    }
}