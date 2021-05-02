package jp.fastkensaku;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.charfilter.MappingCharFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.CapitalizationFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.charfilter.NormalizeCharMap;

public class CustomEnglishAnalyzer extends Analyzer {
    protected NormalizeCharMap charConvertMap;
    CustomEnglishAnalyzer(){
        super();
        NormalizeCharMap.Builder normMapBuilder = new NormalizeCharMap.Builder();
        normMapBuilder.add("農業", "");
        charConvertMap = normMapBuilder.build();
    }
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        StandardTokenizer src = new StandardTokenizer();
        TokenStream result = new LowerCaseFilter(src);
        result = new StopFilter(result,  EnglishAnalyzer.ENGLISH_STOP_WORDS_SET);
        result = new PorterStemFilter(result);
        result = new CapitalizationFilter(result);
        return new TokenStreamComponents(src, result);
    }
    @Override
    protected Reader initReader(String fieldName, Reader reader)
    {
        reader = super.initReader(fieldName, reader);
        return new MappingCharFilter(charConvertMap, reader);
    }
}