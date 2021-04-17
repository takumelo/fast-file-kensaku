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

import java.util.ArrayList;

public class DBHandler {
    private static String dbPath = "jdbc:sqlite:fastkensaku.db";
    private static String tblName = "dirSetting";
    public DBHandler(){
        Path filePath = Paths.get("fastkensaku.db");
        boolean existsDB = Files.exists(filePath);
        String tmpSql = """
            CREATE TABLE IF NOT EXISTS %s(
                'dir' text PRIMARY KEY
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
    public int addNewDir(String dirPath){
        String tmpSql = "INSERT INTO %s('dir') VALUES(?)";
        String sql = String.format(tmpSql, tblName);

        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dirPath);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        return 0;
    }
    public Object[][] getAllDir(){
        String tmpSql = "SELECT dir FROM %s";
        String sql = String.format(tmpSql, tblName);

        ArrayList<ArrayList<String>> paths = new ArrayList<ArrayList<String>>();
        try (Connection conn = DriverManager.getConnection(dbPath);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            while (rs.next()) {
                ArrayList<String> row = new ArrayList<String>();
                row.add(rs.getString("dir"));
                paths.add(row);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        Object[][] data = new Object[paths.size()][];
        for (int i = 0; i < paths.size(); i++) {
            ArrayList<String> row = paths.get(i);
            data[i] = row.toArray(new String[row.size()]);
        }
        return data;
    }
    public Object[] getAllDirForCmb(){
        String tmpSql = "SELECT dir FROM %s";
        String sql = String.format(tmpSql, tblName);

        ArrayList<String> paths = new ArrayList<String>();
        try (Connection conn = DriverManager.getConnection(dbPath);
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)){

            while (rs.next()) {
                paths.add(rs.getString("dir"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return paths.toArray();
    }
    public int deleteAllDir(){
        String tmpSql = "DELETE FROM %s";
        String sql = String.format(tmpSql, tblName);

        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        return 0;
    }
}
