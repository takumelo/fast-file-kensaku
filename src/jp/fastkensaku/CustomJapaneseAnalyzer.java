package jp.fastkensaku;

import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.ja.dict.UserDictionary;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;

public class CustomJapaneseAnalyzer extends Analyzer {

    private UserDictionary jaUserDict;

    CustomJapaneseAnalyzer(){
        super();
        jaUserDict = null;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        JapaneseTokenizer src = new JapaneseTokenizer(jaUserDict, true, JapaneseTokenizer.Mode.NORMAL);
        // ascii文字のみ
        Pattern p = Pattern.compile("\\p{ASCII}*");
        TokenStream result = new PatternReplaceFilter(src, p, "", true);
        result = new StopFilter(src,  JapaneseAnalyzer.getDefaultStopSet());
        return new TokenStreamComponents(src, result);
    }
}