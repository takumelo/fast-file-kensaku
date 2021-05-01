package jp.fastkensaku;

import java.util.ArrayList;
import java.nio.file.Path;

public class HitsDocs {
    private int totalHits;
    private ArrayList<HitsDoc> hitsDocList;
    public HitsDocs(){
        hitsDocList = new ArrayList<HitsDoc>();
    }
    public void add(int rank, Path p, String[] highlightFrag){
        HitsDoc doc = new HitsDoc(rank, p, highlightFrag);
        hitsDocList.add(doc);
    }
    public void setTotalHits(int num){
        this.totalHits = num;
    }
    private ArrayList<String> doChildFormat(){
        ArrayList<String> res = new ArrayList<String>();
        res.add("<ul>");
        for(HitsDoc d: this.hitsDocList){
            res.add("<li>");
            res.add(d.formatPrintHTML());
            res.add("</li>");
        }
        res.add("</ul>");
        return res;
    }
    public String outputHTML(){
        String header = "<div>" + this.totalHits + "件" + "</div>";
        ArrayList<String> bodyList = this.doChildFormat();
        String body = String.join("", bodyList);
        String footer = "";
        String result = header + body + footer;
        return result;
    }
}
