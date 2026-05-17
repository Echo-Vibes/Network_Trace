package model;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class Blacklist {
    private int id;
    private String url;
    private String addTime;

    public Blacklist(int id, String url, Timestamp addTime) {
        this.id = id;
        this.url = url;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.addTime = sdf.format(addTime);
    }

    public int getId() { return id; }
    public String getUrl() { return url; }
    public String getAddTime() { return addTime; }
}
