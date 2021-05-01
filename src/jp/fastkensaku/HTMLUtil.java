package jp.fastkensaku;

import java.util.ArrayList;

public class HTMLUtil {
    public static final String div = "div";
    public static final String li = "li";
    public static final String ul = "ul";
    public static String wrapTag(String tag, String text){
        String tmp = "<%1$s>%2$s</%1$s>";
        String element = String.format(tmp, tag, text);
        return element;
    }
    public static String wrapTag(String tag, ArrayList<String> texts){
        String element = "";
        for(String s: texts){
            element += wrapTag(tag, s);
        }
        return element;
    }
    public static String makeATag(String url, String text){
        String tmp = "<a href=\"file://%1$s\">%2$s</a>";
        String element = String.format(tmp, url, text);
        return element;
    }
}
