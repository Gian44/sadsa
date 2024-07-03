package com.example.demo;

import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileParserService {

    @SuppressWarnings("deprecation")
    public List<Document> parseFile(File file) throws IOException, SAXException {
        List<Document> parsedContents = new ArrayList<>();
        try (TikaInputStream stream = TikaInputStream.get(file)) {
            Parser parser = new AutoDetectParser();
            ContentHandler handler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            ParseContext context = new ParseContext();
            parser.parse(stream, handler, metadata, context);
            parsedContents.add(new Document(handler.toString()));
        } catch (TikaException e) {
            throw new IOException("Error parsing file: " + file.getName(), e);
        }
        return parsedContents;
    }
}
