import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestDBConnection {

    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=WebTrackDB;encrypt=false;trustServerCertificate=true;";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456"; // 改成你设置的 sa 密码

    public static void main(String[] args) {
        System.out.println("正在测试连接到 SQL Server...");
        System.out.println("URL: " + URL);
        System.out.println("User: " + USER);
        System.out.println();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT * FROM Blacklist")) {

            System.out.println("数据库连接成功！");
            System.out.println("\n当前 Blacklist 表中的数据：");
            System.out.println("ID | Keyword");
            System.out.println("---|---------");

            while (rs.next()) {
                int id = rs.getInt("ID");
                String keyword = rs.getString("Keyword");
                System.out.printf("%d  | %s%n", id, keyword);
            }

        } catch (Exception e) {
            System.err.println("数据库连接或查询失败！");
            System.err.println("错误信息: " + e.getMessage());
            e.printStackTrace();
        }
    }
}