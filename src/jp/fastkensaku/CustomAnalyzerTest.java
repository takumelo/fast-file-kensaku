package jp.fastkensaku;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.*;
import org.junit.Test;

import java.io.StringReader;

public class CustomAnalyzerTest {
    @Test
    public void testJapaneseAnalyzer() throws Exception {
        String text = "日本IT企業の勃興";
        CustomJapaneseAnalyzer analyzer = new CustomJapaneseAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("test", text);

        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            int startOffset = offsetAttribute.startOffset();
            int endOffset = offsetAttribute.endOffset();
            String term = charTermAttribute.toString();
            if(term.equals("")){
                System.out.println("[空白]");
            }else{
                System.out.println(term);
            }
        }
    }
    @Test
    public void testKanaJapaneseAnalyzer() throws Exception {

    }
    @Test
    public void testEnglishAnalyzer() throws Exception {
        String text = "日本IT企業の勃興";
        CustomEnglishAnalyzer analyzer = new CustomEnglishAnalyzer();
        TokenStream tokenStream = analyzer.tokenStream("test", text);

        OffsetAttribute offsetAttribute = tokenStream.addAttribute(OffsetAttribute.class);
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            int startOffset = offsetAttribute.startOffset();
            int endOffset = offsetAttribute.endOffset();
            String term = charTermAttribute.toString();
            if(term.equals("")){
                System.out.println("[空白]");
            }else{
                System.out.println(term);
            }
        }
    }
    @Test
    public void testJapaneseHighlight() throws Exception{
        CustomJapaneseAnalyzer jAnalyzer = new CustomJapaneseAnalyzer();
        Query jq = new QueryParser("jaContent", jAnalyzer).parse("IT");

        Formatter formatter = new SimpleHTMLFormatter();
        QueryScorer scorer = new QueryScorer(jq);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 2);
        highlighter.setTextFragmenter(fragmenter);
        TokenStream stream = TokenSources.getTokenStream("jaContent", null, "日本IT企業の勃興", jAnalyzer, -1);

        //Get highlighted text fragments
        String[] frags = highlighter.getBestFragments(stream, "日本IT企業の勃興", 3);
        for(String i: frags){
            System.out.println(i);
        }
    }
    @Test
    public void testKanaJapaneseHighlight() throws Exception {
        CustomJapaneseKanaAnalyzer jAnalyzer = new CustomJapaneseKanaAnalyzer();
        Query jq = new QueryParser("jaKanaContent", jAnalyzer).parse("にっぽん");

        Formatter formatter = new SimpleHTMLFormatter();
        QueryScorer scorer = new QueryScorer(jq);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 2);
        highlighter.setTextFragmenter(fragmenter);
        TokenStream stream = TokenSources.getTokenStream("jaKanaContent", null, "日本IT企業の勃興", jAnalyzer, -1);

        //Get highlighted text fragments
        String[] frags = highlighter.getBestFragments(stream, "日本IT企業の勃興", 3);
        for(String i: frags){
            System.out.println(i);
        }
    }
    @Test
    public void testEnglishHighlight() throws Exception {
        CustomEnglishAnalyzer eAnalyzer = new CustomEnglishAnalyzer();
        Query jq = new QueryParser("EngContent", eAnalyzer).parse("日本");

        Formatter formatter = new SimpleHTMLFormatter();
        QueryScorer scorer = new QueryScorer(jq);
        Highlighter highlighter = new Highlighter(formatter, scorer);
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 2);
        highlighter.setTextFragmenter(fragmenter);
        TokenStream stream = TokenSources.getTokenStream("EngContent", null, "日本IT企業の勃興", eAnalyzer, -1);

        //Get highlighted text fragments
        String[] frags = highlighter.getBestFragments(stream, "日本IT企業の勃興", 3);
        for(String i: frags){
            System.out.println(i);
        }
    }
}
