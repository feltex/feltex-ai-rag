package br.com.feltex.csv;

import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final CsvService csvService;

    public RagController(CsvService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/upload-csv")
    public ResponseEntity<String> uploadCsv(@RequestParam("file") MultipartFile file) {
        try {
            csvService.loadCsvData(file.getInputStream());
            return ResponseEntity.ok("CSV data loaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error processing CSV file");
        }
    }

    @GetMapping("/query")
    public ResponseEntity<String> query(@RequestParam String question) {
        String answer = csvService.queryWithRag(question);
        return ResponseEntity.ok(answer);
    }
}
