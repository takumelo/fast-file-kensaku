package jp.fastkensaku;

import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.CapitalizationFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;


public class CustomEnglishAnalyzer extends Analyzer {
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        // https://stackoverflow.com/questions/61075242/is-there-a-way-to-remove-all-special-characters-using-lucene-filters
        // TODO: WhitespaceTokenizer()のほうがいい？
        StandardTokenizer src = new StandardTokenizer();
        // ascii文字以外
        Pattern p = Pattern.compile("[^\\p{ASCII}]");
        TokenStream result = new PatternReplaceFilter(src, p, " ", true);
        result = new LowerCaseFilter(src);
        result = new StopFilter(result,  EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        result = new PorterStemFilter(result);
        result = new CapitalizationFilter(result);
        return new TokenStreamComponents(src, result);
    }
}