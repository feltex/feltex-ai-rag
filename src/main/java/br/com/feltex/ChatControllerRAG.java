package br.com.feltex;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="/rag")
public class ChatControllerRAG {

    final ChatClient chatClient;

    private final String prompt = """ 
                        Você é um assistente de IA que responde as dúvidas dos usuários com bases nos documentos a baixo.
                        Os documentos abaixo apresentam as fontes atualizadas e devem ser consideradas como verdade.
                        Cite a fonte quando fornecer a informação
                        """;

    public ChatControllerRAG(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
            .defaultSystem(prompt)
            .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore))
            .build();
    }

    @GetMapping("/")
    public String chat(@RequestParam(value = "query", required = false,
        defaultValue = "Resuma a ultima ata do COPOM de 2025") String query) {
        return chatClient.prompt()
            .user(query)
            .call()
            .content();
    }


}
