package br.com.feltex.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

@Service
public class CsvProcessor {

    public List<Document> parseCsvToDocuments(InputStream csvStream) throws IOException {
        List<Document> documents = new ArrayList<>();

        try (Reader reader = new InputStreamReader(csvStream);
            final var parser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {

            for (CSVRecord record : parser) {
                // Combine all fields or process specific columns
                StringBuilder content = new StringBuilder();
                record.forEach(content::append);

                // Create metadata from headers if needed
                Map<String, Object> metadata = new HashMap<>();
                record.getParser().getHeaderNames().forEach(header ->
                    metadata.put(header, record.get(header)));
                documents.add(new Document(content.toString(), metadata));
            }
        }
        return documents;
    }
}
