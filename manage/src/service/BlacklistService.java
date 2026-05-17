package service;

import db.history_DAO;
import model.Blacklist;
import java.util.List;

public class BlacklistService {
    private history_DAO dao = new history_DAO();

    public List<Blacklist> getAll() {
        return dao.getBlacklistAll();
    }

    public void add(String url) {
        if (url != null && !url.trim().isEmpty()) {
            dao.blacklist_add(url.trim());
        }
    }

    public void delete(int id) {
        dao.blacklist_delete(id);
    }
}
