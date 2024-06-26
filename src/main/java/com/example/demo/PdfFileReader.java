package com.example.demo;

import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.logging.Logger;

@Component
public class PdfFileReader {
    private static final Logger logger = Logger.getLogger(PdfFileReader.class.getName());

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourcePatternResolver;

    @Value("classpath:/data/*.pdf")
    private String pdfResourcesPattern;

    public PdfFileReader(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
    }

    @PostConstruct
    public void init() throws IOException {
        logger.info("Initializing PdfFileReader...");

        Resource[] pdfResources = resourcePatternResolver.getResources(pdfResourcesPattern);
        if (pdfResources.length == 0) {
            logger.warning("No PDF resources found for pattern: " + pdfResourcesPattern);
            return;
        }

        var config = PdfDocumentReaderConfig.builder()
                .withPageExtractedTextFormatter(
                        new ExtractedTextFormatter.Builder()
                                .build())
                .build();

        var textSplitter = new TokenTextSplitter();

        for (Resource pdfResource : pdfResources) {
            logger.info("Processing PDF resource: " + pdfResource.getFilename());
            var pdfReader = new PagePdfDocumentReader(pdfResource, config);
            vectorStore.accept(textSplitter.apply(pdfReader.get()));
        }

        logger.info("PdfFileReader initialization completed.");
    }
}
