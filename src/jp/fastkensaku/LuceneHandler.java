package jp.fastkensaku;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneHandler {
    private static Path path;
    public LuceneHandler(){
        path = Paths.get("C:\\Projects\\20210402_fast_kensaku\\lucene_storage");
    }
    public int index(String fileName, String meta, String extName, String content) throws IOException, ParseException {
        // 日本語用アナライザ
        Map<String,Analyzer> analyzerPerField = new HashMap<>();
        analyzerPerField.put("jaContent", new CustomJapaneseAnalyzer());
        analyzerPerField.put("jaFileName", new CustomJapaneseAnalyzer());
        analyzerPerField.put("jaKanaFileName", new CustomJapaneseKanaAnalyzer());
        analyzerPerField.put("jaKanaContent", new CustomJapaneseKanaAnalyzer());
        // 上記以外は英語アナライザ
        PerFieldAnalyzerWrapper aWrapper =
                new PerFieldAnalyzerWrapper(new CustomEnglishAnalyzer(), analyzerPerField);

        // 1. create the index
        Directory index = FSDirectory.open(path);

        IndexWriterConfig config = new IndexWriterConfig(aWrapper);

        IndexWriter w = new IndexWriter(index, config);
        addDoc(w, fileName,meta, extName, content);
        w.close();
        return 0;
    }
    public int search(String queryStr) throws IOException, ParseException {

        // アナライザ準備
        CustomEnglishAnalyzer analyzer = new CustomEnglishAnalyzer();
        Query q = new QueryParser("fileName", analyzer).parse(queryStr);
        CustomJapaneseAnalyzer jAnalyzer = new CustomJapaneseAnalyzer();
        Query jq = new QueryParser("jaContent", jAnalyzer).parse(queryStr);
        CustomJapaneseKanaAnalyzer jkanaAnalyzer = new CustomJapaneseKanaAnalyzer();
        Query jqk = new QueryParser("jaKanaContent", jkanaAnalyzer).parse(queryStr);

        BooleanQuery bq = new BooleanQuery.Builder()
                .add(q, BooleanClause.Occur.SHOULD)
                .add(jq, BooleanClause.Occur.SHOULD)
                .add(jqk, BooleanClause.Occur.SHOULD)
                .build();


        // 3. search
        int hitsPerPage = 10;
        Directory index = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(bq, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("fileName"));
        }

        reader.close();
        return 0;
    }
    private static void addDoc(IndexWriter w, String fileName, String meta, String extName, String content) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("fileName", fileName, Field.Store.YES));
        doc.add(new TextField("jaFileName", fileName, Field.Store.YES));
        doc.add(new TextField("enContent", content, Field.Store.YES));
        doc.add(new TextField("jaContent", content, Field.Store.YES));
        doc.add(new TextField("jaKanaContent", content, Field.Store.YES));

        // use a string field for isbn because we don't want it tokenized
        doc.add(new StringField("meta", meta, Field.Store.YES));
        doc.add(new StringField("extName", extName, Field.Store.YES));
        w.addDocument(doc);
    }
}
