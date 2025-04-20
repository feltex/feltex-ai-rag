package br.com.feltex;

import br.com.feltex.csv.CsvService;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IngestionService implements CommandLineRunner {

    final VectorStore vectorStore;
    final JdbcClient jdbcClient;
    private final CsvService csvService;

    public IngestionService(VectorStore vectorStore, JdbcClient jdbcClient, CsvService csvService) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
        this.csvService = csvService;
    }

    @Override
    public void run(String... args) {
        loadFiles("/csv/*.csv", "docs/*.pdf");
    }

    private void loadFiles(String...paths) {

        for (String path : paths) {
            try {
                var resolver = new PathMatchingResourcePatternResolver();
                Resource[] csvResources = resolver.getResources("classpath:"+path);

                if (csvResources.length == 0) {
                    log.warn("No CSV files found in docs folder.");
                    return;
                }
                Arrays.stream(csvResources).forEach(this::ingestFileIfNotExists);
            } catch (Exception e) {
                log.error("Failed to load {} files from docs folder", path, e);
            }
        }

    }


    private void ingestFileIfNotExists(Resource resource) {
        try {
            final var filename = resource.getFilename();

            if (!fileExist(filename)) {
                if (filename.endsWith(".pdf")) {
                    var reader = new PagePdfDocumentReader(resource);
                    var textSplitter = new TokenTextSplitter();
                    vectorStore.accept(textSplitter.apply(reader.get()));
                } else if (filename.endsWith(".csv")) {
                    csvService.loadCsvData(resource.getInputStream());
                } else {
                    log.warn("Arquivo não suportado '{}'", filename);
                    return;
                }

                persistFileName(filename);
                log.info("✅ Arquivos carregado com sucesso '{}'", filename);
            } else {
                log.info("⏭️ Pulando arquivo '{}'. Arquivos já foi carreado.", filename);
            }
        } catch (Exception e) {
            log.error("Falha ao carregar arquivo '{}'", resource.getFilename(), e);
        }
    }


    private boolean fileExist(String filename) {
        return jdbcClient
            .sql("SELECT COUNT(*) FROM ingested_files WHERE filename = ?")
            .params(filename)
            .query(Integer.class)
            .single() > 0;
    }

    private void persistFileName(String filename) {
        jdbcClient
            .sql("INSERT INTO ingested_files (filename) VALUES (?)")
            .params(filename)
            .update();
    }


}
