package jp.fastkensaku;

import java.util.ArrayList;
import java.nio.file.Path;

public class HitsDoc {
    private int rank;
    private String filePath;
    private String fileName;
    private String fileDir;
    private String[] highlightTexts;
    private Path path;
    public HitsDoc(int rank, Path p, String[] highlightFrag){
        this.rank = rank;
        this.path = p;
        this.filePath = p.toString();
        this.fileName = p.getFileName().toString();
        int len = highlightFrag.length;
        highlightTexts = new String[len];
        for(int i = 0; i < len; i++){
            // 改行を削除
            highlightTexts[i] = highlightFrag[i].replaceAll("\\R", "");
        }
    }
    public String formatPrintHTML(){
        String path = "<div><a href=\"file://" + this.path.getParent().toString() + "\">" + this.path.getParent().toString() + "</a></div>";
        String fileName = "<div><a href=\"file://" + this.filePath + "\">" + this.fileName + "</a></div>";
        String highlight = "<div>";
        for(String ht: highlightTexts){
            highlight += "<div>" + ht + "</div>";
        }
        highlight += "</div>";

        String res = path + fileName + highlight;
        return res;
    }
}