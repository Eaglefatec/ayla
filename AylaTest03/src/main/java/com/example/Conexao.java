package com.example;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

public class Conexao {
    private final EmbeddingModel modeloEmbedding;
    private final EmbeddingStore<TextSegment> bancoVetorial;
    private final ChatLanguageModel modeloChat;

    public Conexao() {
        // Mantém o Nomic para a busca vetorial (Postgres)
        this.modeloEmbedding = OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("nomic-embed-text")
                .build();

        // 🚀 ATUALIZADO: Agora usando o Granite da IBM para gerar o resumo prático
        this.modeloChat = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("granite3.1-dense:2b") // Ajuste o nome exato conforme baixou no Ollama
                .temperature(0.2)                 // Temperatura baixa deixa o Granite bem preciso
                .build();

        // Configuração do Postgres Local
        this.bancoVetorial = PgVectorEmbeddingStore.builder()
                .host("localhost")
                .port(5432)
                .database("postgres")
                .user("postgres")
                .password("139499")
                .table("curriculos_teste")
                .dimension(768)
                .build();
    }

    public EmbeddingModel getModeloEmbedding() { return modeloEmbedding; }
    public EmbeddingStore<TextSegment> getBancoVetorial() { return bancoVetorial; }
    public ChatLanguageModel getModeloChat() { return modeloChat; }
}