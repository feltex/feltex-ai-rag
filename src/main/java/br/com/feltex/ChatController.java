package br.com.feltex;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ChatController {

    final ChatClient chatClient;


    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/")
    public String chat(@RequestParam(value = "query",
        defaultValue = "Resuma a ultima ata do COPOM de 2025") String query) {
        return chatClient.prompt()
            .user(query)
            .call()
            .content();
    }




}
