package jp.fastkensaku;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DBHandler {
    private static String dbPath = "jdbc:sqlite:fastkensaku.db";
    private static String tblName = "test1";
    public DBHandler(){
        Path filePath = Paths.get("fastkensaku.db");
        boolean existsDB = Files.exists(filePath);
        String tmpSql = """
            CREATE TABLE IF NOT EXISTS %s (
                id integer PRIMARY KEY,
                name text NOT NULL,
                capacity real
            );
        """;
        String sql = String.format(tmpSql, tblName);
        try (Connection conn = DriverManager.getConnection(dbPath)) {
            if (conn != null) {
                if(!existsDB){
                    DatabaseMetaData meta = conn.getMetaData();
                }
                Statement stmt = conn.createStatement();
                // create a new table
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
