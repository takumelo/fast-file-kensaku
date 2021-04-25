package jp.fastkensaku;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class TikaHandler {

    private String content;
    private String meta;
    private String extention;
    private String fileName;

    public TikaHandler(){
        content = null;
        meta = null;
        extention = null;
    }

    public int parse(File file) throws TikaException, IOException, SAXException {
        this.fileName = file.getName();
        //Parser method parameters
        Parser parser = new AutoDetectParser();
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(file);
        ParseContext context = new ParseContext();

        parser.parse(inputstream, handler, metadata, context);
        this.content = handler.toString();


        //getting the list of all meta data elements
        String[] metadataNames = metadata.names();

        this.meta = String.join(",", metadataNames);

        this.extention = getFileExtension(file);

        return 0;
    }

    public String getContent(){
        return this.content;
    }

    public String getMeta(){
        return this.meta;
    }

    public String getExtention(){
        return this.extention;
    }

    public String getFileName(){
        return this.fileName;
    }

    private static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf + 1);
    }
}
