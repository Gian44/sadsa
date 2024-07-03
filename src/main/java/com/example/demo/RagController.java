package com.example.demo;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RagController {

    private final ChatClient aiClient;
    private final VectorStore vectorStore;

    public RagController(ChatClient.Builder aiClientBuilder, VectorStore vectorStore) {
        this.aiClient = aiClientBuilder.build();
        this.vectorStore = vectorStore;
    }

    @SuppressWarnings("removal")
    @GetMapping("/rag")
    public ResponseEntity<String> generateAnswer(@RequestParam String query) {

        System.out.println("Query: " + query);
        //SearchRequest q = query(query).withSimilarityThreshold(0.5).withTopK(3);
        List<Document> similarDocuments = vectorStore.similaritySearch(query);
        System.out.println();
        String information = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));

        var systemPromptTemplate = new SystemPromptTemplate(
                """
                            You are a helpful assistant.
                            Use only the following information to answer the question.
                            Do not use any other information. If you don't know, say you don't know.

                            {information}
                        """);
        var systemMessage = systemPromptTemplate.createMessage(Map.of("information", information));
        
        System.out.println("System message created: " + systemMessage.getContent());

        var userPromptTemplate = new PromptTemplate("{query}");
        var userMessage = userPromptTemplate.createMessage(Map.of("query", query));

        var prompt = new Prompt(List.of(systemMessage, userMessage));
        var response = aiClient.call(prompt).getResult().getOutput().getContent();

        String outputString = String.format(
            "Query: %s%nInformation: %s%n%nAI response: %s",
            query, information, response
        );

        return ResponseEntity.ok(outputString);
    }
}
