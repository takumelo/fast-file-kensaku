package jp.fastkensaku;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.ja.JapaneseAnalyzer;
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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class LuceneHandler {
    private Path path;
    public LuceneHandler(){
        Path path = Paths.get("C:\\Projects\\20210322_lucene_app_1st\\lucene_storage");
    }
    public int index() throws IOException, ParseException {
        // 0. Specify the analyzer for tokenizing text.
        //    The same analyzer should be used for indexing and searching
        Map<String,Analyzer> analyzerPerField = new HashMap<>();
        analyzerPerField.put("ja", new CustomJapaneseAnalyzer());
        analyzerPerField.put("ja_kana", new CustomJapaneseKanaAnalyzer());

        PerFieldAnalyzerWrapper aWrapper =
                new PerFieldAnalyzerWrapper(new CustomEnglishAnalyzer(), analyzerPerField);

        // 1. create the index
        Directory index = FSDirectory.open(path);

        IndexWriterConfig config = new IndexWriterConfig(aWrapper);

        IndexWriter w = new IndexWriter(index, config);
        addDoc(w, "Lucene in Action", "193398817", "今日は桜の開花日だ。");
        addDoc(w, "Lucene for Dummies", "55320055Z", "毎日仕事が退屈だよね");
        addDoc(w, "Managing Gigabytes", "55063554A", "This is my world.");
        addDoc(w, "The Art of Computer Science", "9900333X", "桜の花びらが舞っている");
        w.close();
        return 0;
    }
    public int search(String queryStr) throws IOException, ParseException {

        // the "title" arg specifies the default field to use
        // when no field is explicitly specified in the query.
        //StandardAnalyzer analyzer = new StandardAnalyzer();
        CustomEnglishAnalyzer analyzer = new CustomEnglishAnalyzer();
        Query q = new QueryParser("title", analyzer).parse(queryStr);
        CustomJapaneseAnalyzer jAnalyzer = new CustomJapaneseAnalyzer();
        Query jq = new QueryParser("ja", jAnalyzer).parse(queryStr);
        CustomJapaneseKanaAnalyzer jkanaAnalyzer = new CustomJapaneseKanaAnalyzer();
        Query jqk = new QueryParser("ja_kana", jkanaAnalyzer).parse(queryStr);

        // 3. search
        int hitsPerPage = 10;
        Directory index = FSDirectory.open(path);
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(q, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        // 4. display results
        System.out.println("Found " + hits.length + " hits.");
        for(int i=0;i<hits.length;++i) {
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("title"));
        }

        // JP search
        TopDocs jDocs = searcher.search(jq, hitsPerPage);
        ScoreDoc[] jHits = jDocs.scoreDocs;
        System.out.println("Found " + jHits.length + " hits.");
        for(int i=0;i<jHits.length;++i){
            int docId = jHits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("ja"));
        }

        TopDocs jkDocs = searcher.search(jqk, hitsPerPage);
        ScoreDoc[] jkHits = jkDocs.scoreDocs;
        System.out.println("Found " + jkHits.length + " hits.");
        for(int i=0;i<jkHits.length;++i){
            int docId = jkHits[i].doc;
            Document d = searcher.doc(docId);
            System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("ja"));
        }

        // reader can only be closed when there
        // is no need to access the documents any more.
        reader.close();
        return 0;
    }
    private static void addDoc(IndexWriter w, String title, String isbn, String jpTitle) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new TextField("ja", jpTitle, Field.Store.YES));
        doc.add(new TextField("ja_kana", jpTitle, Field.Store.YES));

        // use a string field for isbn because we don't want it tokenized
        doc.add(new StringField("isbn", isbn, Field.Store.YES));
        w.addDocument(doc);
    }
}
