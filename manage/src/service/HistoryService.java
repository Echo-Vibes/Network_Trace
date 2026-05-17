package service;

import db.history_DAO;
import model.History;
import java.util.List;

public class HistoryService {
    private history_DAO dao = new history_DAO();

    public List<History> getHistory(String userName, String urlKeyword) {
        return dao.getHistoryList(userName, urlKeyword);
    }
}
