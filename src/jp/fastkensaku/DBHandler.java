package jp.fastkensaku;

import java.io.IOException;
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
import java.time.LocalDateTime;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Stream;

public class DBHandler {
    private static String dbPath = "jdbc:sqlite:fastkensaku.db";
    private static String tblName = "dirSetting";

    /**
     * なければディレクトリ設定テーブルを作成、DB自体も作成
     */
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
    /**
     * ディレクトリ設定テーブルにディレクトリを追加する
     *
     * @param dirPath ディレクトリパス名
     * @return 成功なら0
     */
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
    /**
     * ディレクトリ設定テーブルにから全件取得
     *
     * @return ディレクトリ設定テーブルの全件データの2次元配列
     */
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
    /**
     * ディレクトリ設定テーブルからコンボボックス用に値を取得
     *
     * @return ディレクトリ値の配列データ
     */
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
    /**
     * ディレクトリ設定テーブルのデータから各ディレクトリテーブルを全件削除
     *
     * @return 成功なら0
     */
    private int deleteAllDirTbl(){
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

        String tmpDpSql = """
            DROP TABLE IF EXISTS "%s";
        """;
        for(String p: paths){
            String dpSql = String.format(tmpDpSql, p);
            try(Connection conn = DriverManager.getConnection(dbPath)){
                Statement stmt  = conn.createStatement();
                stmt.executeQuery(dpSql);
            }catch (SQLException e){
                System.out.println(e.getMessage());
                return -1;
            }
        }

        return 0;
    }
    /**
     * ディレクトリ設定テーブルのデータを全件削除
     *
     * @return 成功なら0
     */
    public int deleteAllDir(){
        deleteAllDirTbl();
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
    /**
     * ディレクトリテーブルを作成
     *
     * @return 成功なら0
     */
    public int createDirTbl(String dir){
        String tmpSql = """
            CREATE TABLE IF NOT EXISTS "%s"(
                'dir' text PRIMARY KEY,
                'hash' text,
                'updated' integer,
                'lucenized' integer
                check(lucenized = 0 or lucenized = 1)
            );
        """;
        String sql = String.format(tmpSql, dir);
        try (Connection conn = DriverManager.getConnection(dbPath)) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        return 0;
    }
    /**
     * ディレクトリ設定テーブルにディレクトリを追加する
     *
     * @param tblName テーブル名
     * @param path 追加するディレクトリ名
     * @param updateTime 更新時間
     * @return 成功なら0
     */
    private int insertFiles(String tblName, Path path, int updateTime){
        String tmpSql = """
                INSERT INTO "%s"('dir', 'updated') VALUES(?, ?)
                """;
        String sql = String.format(tmpSql, tblName);

        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, path.toString());
            pstmt.setInt(2, updateTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
        return 0;
    }
    /**
     * ディレクトリテーブルに特定ディレクトリ直下のファイルをすべて追加
     *
     * @param path 追加するディレクトリ名
     * @return 成功なら0
     */
    public int insertFilesRecur(String path){
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formatDateTime = now.format(formatter);
        int intFormatDataTime = Integer.parseInt(formatDateTime);
        try(Stream<Path> stream = Files.walk(Paths.get(path))){
            Stream<Path> ps = stream.filter(Files::isRegularFile);
            ps.forEach(e -> insertFiles(path, e, intFormatDataTime));
        }catch(IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
