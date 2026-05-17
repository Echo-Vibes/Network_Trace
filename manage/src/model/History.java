package model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class History {
    private int id;
    private String userName;
    private String url;
    private String visitTime;

    public History(int id, String userName, String url, Timestamp visitTime) {
        this.id = id;
        this.userName = userName;
        this.url = url;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.visitTime = sdf.format(visitTime);
    }

    public int getId() { return id; }
    public String getUserName() { return userName; }
    public String getUrl() { return url; }
    public String getVisitTime() { return visitTime; }
}
