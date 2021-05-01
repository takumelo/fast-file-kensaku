package jp.fastkensaku;

import javax.swing.text.html.HTML;
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
    private String doChildFormat(){
        String tmpRes = "";
        for(HitsDoc d: this.hitsDocList){
            tmpRes += HTMLUtil.wrapTag(HTMLUtil.li, d.formatPrintHTML());
        }
        String res = HTMLUtil.wrapTag(HTMLUtil.ul, tmpRes);
        return res;
    }
    public String outputHTML(){
        String header = HTMLUtil.wrapTag(HTMLUtil.div, this.totalHits + "件");
        String body = this.doChildFormat();
        String footer = "";
        String result = header + body + footer;
        return result;
    }
}
