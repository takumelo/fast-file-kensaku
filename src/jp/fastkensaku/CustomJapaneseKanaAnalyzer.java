package jp.fastkensaku;

import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseTokenizer;
import org.apache.lucene.analysis.ja.JapaneseReadingFormFilter;
import org.apache.lucene.analysis.ja.dict.UserDictionary;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;

public class CustomJapaneseKanaAnalyzer extends Analyzer {

    private UserDictionary jaUserDict;

    CustomJapaneseKanaAnalyzer(){
        super();
        jaUserDict = null;
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        JapaneseTokenizer src = new JapaneseTokenizer(jaUserDict, true, JapaneseTokenizer.Mode.NORMAL);
        // ascii文字のみを置換
        Pattern p = Pattern.compile("\\p{ASCII}");
        TokenStream result = new PatternReplaceFilter(src, p, "", true);

        // 置換した空白トークンと日本語のストップワードの削除
        CharArraySet stopSet = StopFilter.makeStopSet("");
        stopSet.add(JapaneseAnalyzer.getDefaultStopSet());
        result = new StopFilter(result,  stopSet);

        //http://www.mwsoft.jp/programming/lucene/lucene_filter.html
        result = new JapaneseReadingFormFilter(result);
        return new TokenStreamComponents(src, result);
    }
}