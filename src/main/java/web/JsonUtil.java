package web;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonUtil {

    private static final SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String esc(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder(s.length() + 4);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:   sb.append(c);
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static String date(Date d) {
        if (d == null) return "null";
        return "\"" + fmt.format(d) + "\"";
    }

    public static String obj(String... kvs) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < kvs.length; i += 2) {
            if (i > 0) sb.append(',');
            sb.append(esc(kvs[i])).append(':').append(kvs[i + 1]);
        }
        sb.append('}');
        return sb.toString();
    }

    public static String arr(Iterable<String> items) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (String item : items) {
            if (!first) sb.append(',');
            sb.append(item);
            first = false;
        }
        sb.append(']');
        return sb.toString();
    }
}
