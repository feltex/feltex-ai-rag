package br.com.feltex;

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

    public IngestionService(VectorStore vectorStore, JdbcClient jdbcClient) {
        this.vectorStore = vectorStore;
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void run(String... args){
        try {
            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] pdfResources = resolver.getResources("classpath:/docs/*.pdf");

            if (pdfResources.length == 0) {
                log.warn("No PDF files found in docs folder.");
                return;
            }
            Arrays.stream(pdfResources).forEach(this::ingestPdfIfNotExists);
        } catch (Exception e) {
            log.error("Failed to load PDF files from docs folder", e);
        }
    }

    private void ingestPdfIfNotExists(Resource pdfResource) {
        try {
            final var filename = pdfResource.getFilename();

            var exists = jdbcClient
                .sql("SELECT COUNT(*) FROM ingested_files WHERE filename = ?")
                .params(filename)
                .query(Integer.class)
                .single();

            if (exists == 0) {
                var reader = new PagePdfDocumentReader(pdfResource);
                var textSplitter = new TokenTextSplitter();
                vectorStore.accept(textSplitter.apply(reader.get()));

                jdbcClient
                    .sql("INSERT INTO ingested_files (filename) VALUES (?)")
                    .params(filename)
                    .update();

                log.info("✅ Arquivos carregado com sucesso '{}'", filename);
            } else {
                log.info("⏭️ Pulando arquivo '{}'. Arquivos já foi carreado.", filename);
            }
        }catch (Exception e) {
            log.error("Falha ao carregar arquivo '{}'", pdfResource.getFilename(), e);
        }
    }
}
