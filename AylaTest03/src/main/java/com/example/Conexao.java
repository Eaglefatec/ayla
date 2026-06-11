package com.example;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pgvector.PgVectorEmbeddingStore;

import java.io.InputStream;
import java.util.Properties;

public class Conexao {
    private final EmbeddingModel modeloEmbedding;
    private final EmbeddingStore<TextSegment> bancoVetorial;
    private final ChatLanguageModel modeloChat; // Mantido como final pois agora só recebe valor UMA vez

    public Conexao() {
        // 1. Mantém o Nomic para a busca vetorial (Postgres)
        this.modeloEmbedding = OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("nomic-embed-text")
                .build();

        // 2. Inicializa o Chat de IA usando a lógica de Fallback (Llama3.2 como principal)
        this.modeloChat = inicializarModeloChatComFallback();

        // 3. Carrega as configurações do arquivo seguro
        Properties propriedades = new Properties();
        try (InputStream input = Conexao.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Erro crítico: O arquivo 'application.properties' não foi encontrado!");
            }
            propriedades.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler propriedades de conexão: " + e.getMessage());
        }

        // 4. Configuração do Postgres Local com leitura dinâmica por propriedades
        this.bancoVetorial = PgVectorEmbeddingStore.builder()
                .host(propriedades.getProperty("db.host"))
                .port(Integer.parseInt(propriedades.getProperty("db.port")))
                .database(propriedades.getProperty("db.name"))
                .user(propriedades.getProperty("db.user"))
                .password(propriedades.getProperty("db.password"))
                .table("curriculos_teste")
                .dimension(768)
                .createTable(true)
                .build();
    }

    /**
     * Tenta carregar o Llama 3.2. Se falhar por falta do modelo, carrega o Qwen automaticamente.
     */
    private ChatLanguageModel inicializarModeloChatComFallback() {
        String modeloPrincipal = "llama3.2";
        String modeloReserva = "qwen2.5:1.5b";

        System.out.println("🤖 Tentando inicializar o modelo de IA principal (" + modeloPrincipal + ")...");

        ChatLanguageModel modeloTentativa = OllamaChatModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName(modeloPrincipal)
                .temperature(0.2)
                .build();

        try {
            // Força uma interação mínima para validar se o Ollama tem o modelo carregado
            modeloTentativa.generate("Oi");
            System.out.println("✅ " + modeloPrincipal + " carregado com sucesso!");
            return modeloTentativa;

        } catch (Exception e) {
            // Verifica se a mensagem de erro do Ollama indica que o modelo não foi achado
            if (e.getMessage() != null && e.getMessage().contains("not found")) {
                System.out.println("⚠️ " + modeloPrincipal + " não foi encontrado no seu Ollama.");
                System.out.println("🔄 Ativando plano B: Carregando modelo reserva (" + modeloReserva + ")...");

                ChatLanguageModel modeloPlanoB = OllamaChatModel.builder()
                        .baseUrl("http://localhost:11434")
                        .modelName(modeloReserva)
                        .temperature(0.2)
                        .build();

                try {
                    // Testa o modelo reserva também para garantir
                    modeloPlanoB.generate("Oi");
                    System.out.println("✅ " + modeloReserva + " carregado com sucesso como alternativa!");
                    return modeloPlanoB;
                } catch (Exception ex) {
                    System.out.println("❌ Erro crítico: O modelo reserva '" + modeloReserva + "' também falhou ou não está baixado.");
                    System.out.println("Certifique-se de dar 'ollama run " + modeloReserva + "' no terminal do Linux.");
                    throw ex;
                }
            } else {
                // Se for outro erro (ex: Ollama desligado), repassa o erro original
                System.out.println("❌ Erro de conexão com o Ollama: " + e.getMessage());
                throw e;
            }
        }
    }

    public EmbeddingModel getModeloEmbedding() { return modeloEmbedding; }
    public EmbeddingStore<TextSegment> getBancoVetorial() { return bancoVetorial; }
    public ChatLanguageModel getModeloChat() { return modeloChat; }
}