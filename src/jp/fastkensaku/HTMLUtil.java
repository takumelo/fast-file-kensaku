package jp.fastkensaku;

import java.util.ArrayList;

public class HTMLUtil {
    public static final String div = "div";
    public static final String li = "li";
    public static final String ul = "ul";
    public static final String clsHighlightWrap = "highlightWrap";
    public static final String clsFileName = "fileName";
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
    public static String wrapTagWithCls(String tag, String text, String cls){
        String tmp = "<%1$s class=\"%3$s\">%2$s</%1$s>";
        String element = String.format(tmp, tag, text, cls);
        return element;
    }
    public static String wrapTagWithCls(String tag, ArrayList<String> texts, String cls){
        String element = "";
        for(String s: texts){
            element += wrapTagWithCls(tag, s, cls);
        }
        return element;
    }
    public static String makeATag(String url, String text){
        String tmp = "<a href=\"file://%1$s\">%2$s</a>";
        String element = String.format(tmp, url, text);
        return element;
    }
    public static String makeATagWithCls(String url, String text, String cls){
        String tmp = "<a class=\"%3$s\" href=\"file://%1$s\">%2$s</a>";
        String element = String.format(tmp, url, text, cls);
        return element;
    }
    private static String liStyle(Integer padding, Integer margin){
        String tmpStyle = "li{list-style:none;padding:%1$dpx;margin:%1$dpx; border: solid;;}";
        String style = String.format(tmpStyle, padding, margin);
        return style;
    }
    private static String highlightWrapCls(Integer padding, Integer margin, String cls){
        String tmpStyle = ".%3$s {list-style:none;padding:%1$dpx;margin:%1$dpx; border: solid;margin-left: 20px;}";
        String style = String.format(tmpStyle, padding, margin, cls);
        return style;
    }
    private static String aFileNameStyle(Integer textSize){
        String tmpStyle = ".%1$s { font-size: %2$d;color: #0645ad; }";
        String style = String.format(tmpStyle, clsFileName, textSize);
        return style;
    }
    private static String aStyle(){
        String style = "a{text-decoration: underline;}";
        return style;
    }
    public static String applyStyle(){
        String css = "";
        css += aStyle();
        css += liStyle(8, 8);
        css += aFileNameStyle(20);
        css += highlightWrapCls(2, 2, clsHighlightWrap);
        return css;
    }
}
