package com.example.demo;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.List;

@Component
public class FileReader {
    private static final Logger logger = Logger.getLogger(FileReader.class.getName());

    private final VectorStore vectorStore;
    private final FileParserService fileParserService;
    private final ResourcePatternResolver resourcePatternResolver;

    @Value("classpath:/data/*.*") 
    private String resourcesPattern;

    public FileReader(VectorStore vectorStore, FileParserService fileParserService) {
        this.vectorStore = vectorStore;
        this.fileParserService = fileParserService;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver();
    }

    @PostConstruct
    public void init() throws IOException {
        logger.info("Initializing FileReader...");

        Resource[] resources = resourcePatternResolver.getResources(resourcesPattern);
        if (resources.length == 0) {
            logger.warning("No resources found for pattern: " + resourcesPattern);
            return;
        }

        var textSplitter = new TokenTextSplitter(1000, 150, 150, 50, false);

        for (Resource resource : resources) {
            logger.info("Processing resource: " + resource.getFilename());
            try {
                List<Document> document = fileParserService.parseFile(resource.getFile());
                vectorStore.accept(textSplitter.apply(document));
            } catch (IOException | SAXException e) {
                logger.warning("Failed to parse file: " + resource.getFilename() + ", reason: " + e.getMessage());
            }
        }

        logger.info("FileReader initialization completed.");
    }
}
