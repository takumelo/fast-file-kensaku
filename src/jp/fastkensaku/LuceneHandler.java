package jp.fastkensaku;

import java.io.IOException;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;

public class LuceneHandler {
    private Path path;
    private String indexRoot;
    private CustomEnglishAnalyzer engAnalyzer;
    private CustomJapaneseAnalyzer jpnAnalyzer;
    private CustomJapaneseKanaAnalyzer jpnKanaAnalyzer;
    private PerFieldAnalyzerWrapper aWrapper;
    public LuceneHandler(){
        indexRoot = "index";
        path = Paths.get(indexRoot);
        engAnalyzer = new CustomEnglishAnalyzer();
        jpnAnalyzer = new CustomJapaneseAnalyzer();
        jpnKanaAnalyzer = new CustomJapaneseKanaAnalyzer();

        // 日本語用アナライザ
        Map<String,Analyzer> analyzerPerField = new HashMap<>();
        analyzerPerField.put("jaContent", jpnAnalyzer);
        analyzerPerField.put("jaFileName", jpnAnalyzer);
        analyzerPerField.put("jaKanaFileName", jpnKanaAnalyzer);
        analyzerPerField.put("jaKanaContent", jpnKanaAnalyzer);
        // 上記以外は英語アナライザ
        aWrapper = new PerFieldAnalyzerWrapper(engAnalyzer, analyzerPerField);
    }
    public int index(Path rootDir , Path p, String meta, String extName, String content) throws IOException, ParseException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5_result = md5.digest(rootDir.toString().getBytes());
        String hashDir = String.format("%020x", new BigInteger(1, md5_result));

        Directory index = FSDirectory.open(Paths.get(indexRoot, hashDir));

        IndexWriterConfig config = new IndexWriterConfig(aWrapper);

        IndexWriter w = new IndexWriter(index, config);
        long updatedAt = p.toFile().lastModified();
        addDoc(w, p, meta, extName, content, updatedAt);
        w.close();
        return 0;
    }
    public int update(Path rootDir , Path p, String meta, String extName, String content) throws IOException, ParseException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5_result = md5.digest(rootDir.toString().getBytes());
        String hashDir = String.format("%020x", new BigInteger(1, md5_result));
        Directory index = FSDirectory.open(Paths.get(indexRoot, hashDir));
        IndexWriterConfig config = new IndexWriterConfig(aWrapper);
        IndexWriter w = new IndexWriter(index, config);

        // 削除
        w.deleteDocuments(new Term("filePath", p.toString()));
        w.commit();

        // 追加
        long updatedAt = p.toFile().lastModified();
        addDoc(w, p, meta, extName, content, updatedAt);
        w.commit();
        w.close();
        return 0;
    }
    public int deletes(String dir, String[] outdatedFilePaths) throws NoSuchAlgorithmException, IOException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5_result = md5.digest(dir.getBytes());
        String hashDir = String.format("%020x", new BigInteger(1, md5_result));
        Directory index = FSDirectory.open(Paths.get(indexRoot, hashDir));
        IndexWriterConfig config = new IndexWriterConfig(aWrapper);
        IndexWriter w = new IndexWriter(index, config);
        for(String filePath: outdatedFilePaths){
            w.deleteDocuments(new Term("filePath", filePath));
        }
        w.commit();
        w.close();
        return 0;
    }
    public HitsDocs search(String queryStr, String searchDir) throws IOException, ParseException, InvalidTokenOffsetsException, NoSuchAlgorithmException {
        System.out.println(searchDir);
        // アナライザ準備
        Query fjq = new QueryParser("jaFileName", jpnAnalyzer).parse(queryStr);
        Query cjq = new QueryParser("jaContent", jpnAnalyzer).parse(queryStr);
        Query kjq = new QueryParser("jaKanaContent", jpnKanaAnalyzer).parse(queryStr);

        Query feq = new QueryParser("enFileName", engAnalyzer).parse(queryStr);
        Query ceq = new QueryParser("enContent", engAnalyzer).parse(queryStr);

        BooleanQuery bq = new BooleanQuery.Builder()
                .add(fjq, BooleanClause.Occur.SHOULD)
                .add(cjq, BooleanClause.Occur.SHOULD)
                .add(kjq, BooleanClause.Occur.SHOULD)
                .add(feq, BooleanClause.Occur.SHOULD)
                .add(ceq, BooleanClause.Occur.SHOULD)
                .build();


        // 3. search
        int hitsPerPage = 10;
        String hashDir = HashUtil.getMD5(searchDir);
        Directory index = FSDirectory.open(Paths.get(indexRoot, hashDir));
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs docs = searcher.search(bq, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;

        HitsDocs hitsDocs = makeHitsDocs(reader, searcher, bq, hits);

        reader.close();
        return hitsDocs;
    }
    private static void addDoc(IndexWriter w, Path p, String meta, String extName, String content, long updatedAt) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("enFileName", p.getFileName().toString(), Field.Store.YES));
        doc.add(new TextField("jaFileName", p.getFileName().toString(), Field.Store.YES));
        doc.add(new TextField("enContent", content, Field.Store.YES));
        doc.add(new TextField("jaContent", content, Field.Store.YES));
        doc.add(new TextField("jaKanaContent", content, Field.Store.YES));
        doc.add(new TextField("jaKanaFileName", p.getFileName().toString(), Field.Store.YES));

        // use a string field for isbn because we don't want it tokenized
        doc.add(new StringField("filePath", p.toString(), Field.Store.YES));
        doc.add(new StringField("meta", meta, Field.Store.YES));
        doc.add(new StringField("extName", extName, Field.Store.YES));
        doc.add(new NumericDocValuesField("updated", updatedAt));
        w.addDocument(doc);
    }
    private HitsDocs makeHitsDocs(IndexReader reader, IndexSearcher searcher, Query query, ScoreDoc[] hits) throws IOException, InvalidTokenOffsetsException {
        //Uses HTML &lt;B&gt;&lt;/B&gt; tag to highlight the searched terms
        Formatter formatter = new SimpleHTMLFormatter();
        //It scores text fragments by the number of unique query terms found
        //Basically the matching score in layman terms
        QueryScorer scorer = new QueryScorer(query);
        //used to markup highlighted terms found in the best sections of a text
        Highlighter highlighter = new Highlighter(formatter, scorer);
        //It breaks text up into same-size texts but does not split up spans
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 30);
        //breaks text up into same-size fragments with no concerns over spotting sentence boundaries.
        //Fragmenter fragmenter = new SimpleFragmenter(10);
        //set fragmenter to highlighter
        highlighter.setTextFragmenter(fragmenter);

        // 4. display results
        // https://northcoder.com/post/lucene-83-basic-search-examples/

        HitsDocs hitsDocs = new HitsDocs();
        hitsDocs.setTotalHits(hits.length);

        int maxNumFragments = 3;
        Map<String, Analyzer> analyzerPerField = new LinkedHashMap<>();
        analyzerPerField.put("jaContent", jpnAnalyzer);
        analyzerPerField.put("jaFileName", jpnAnalyzer);
        analyzerPerField.put("jaKanaFileName", jpnKanaAnalyzer);
        analyzerPerField.put("jaKanaContent", jpnKanaAnalyzer);
        analyzerPerField.put("enContent", engAnalyzer);
        analyzerPerField.put("enFileName", engAnalyzer);


        for(int i=0;i<hits.length;++i) {
            String[] resultFrags = new String[0];
            int docId = hits[i].doc;
            Document d = searcher.doc(docId);
            Fields f = reader.getTermVectors(docId);

            for (Map.Entry<String, Analyzer> kv : analyzerPerField.entrySet()) {
                String fieldName = kv.getKey();
                String text = d.get(fieldName);
                Analyzer analyzer = kv.getValue();
                TokenStream stream = TokenSources.getTokenStream(fieldName, f, text, analyzer, -1);
                if(resultFrags.length == 0){
                    resultFrags = highlighter.getBestFragments(stream, text, maxNumFragments);
                }
            }


            Path p = Paths.get(d.get("filePath"));
            hitsDocs.add(i, p, resultFrags);

        }
        return hitsDocs;
    }
}
