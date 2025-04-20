package br.com.feltex.csv;

import static org.springframework.jdbc.core.JdbcOperationsExtensionsKt.query;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.ai.vectorstore.SearchRequest;

@Service
public class CsvService {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;

    public CsvService(VectorStore vectorStore, ChatClient.Builder builder) {
        this.vectorStore = vectorStore;
        this.chatClient = builder.build();
    }

    public void loadCsvData(InputStream csvStream) throws IOException {
        final var processor = new CsvProcessor();
        List<Document> documents = processor.parseCsvToDocuments(csvStream);

        // For large CSV files, consider batching the inserts
        vectorStore.add(documents);
    }

    public String queryWithRag(String question) {
        // Retrieve relevant documents with similarity threshold
        List<Document> similarDocuments = vectorStore.similaritySearch(
            SearchRequest.builder()
                .query(question)
                .similarityThreshold(0.7) // Minimum similarity score
                .topK(5) // Number of documents to retrieve
                .build()
        );

        if (similarDocuments.isEmpty()) {
            return "No relevant information found in the documents.";
        }

        // Build context from documents
        String context = similarDocuments.stream()
            .map(Document::getFormattedContent)
            .collect(Collectors.joining("\n\n"));

        // Create prompt with context
        PromptTemplate promptTemplate = new PromptTemplate("""
            Answer the question based only on the following context:
            {context}
            
            Question: {question}
            Answer in a clear and concise manner:""");

        Map<String, Object> promptParameters = new HashMap<>();
        promptParameters.put("context", context);
        promptParameters.put("question", question);

        Prompt prompt = promptTemplate.create(promptParameters);

        // Get AI response
        return chatClient
            .prompt(prompt)
            .call()
            .content();

    }
}