import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaEmbeddingModel;

import java.io.File;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        String caminhoPdf = "src/main/resources/Curriculos/CV Julliana Martins Costa.pdf";

        System.out.println("--- Passou 1: Lendo o arquivo PDF ---");
        File arquivo = new File(caminhoPdf);
        if (!arquivo.exists()) {
            System.out.println("Erro: Coloque um arquivo PDF válido em: " + arquivo.getAbsolutePath());
            return;
        }

        // O LangChain4j usa o PDFBox por trás deste Parser para extrair o texto bruto
        Document documento = FileSystemDocumentLoader.loadDocument(arquivo.toPath(), new ApachePdfBoxDocumentParser());

        System.out.println("\nTexto extraído com sucesso! Prévia do conteúdo:\n");
        // Mostra os primeiros 600 caracteres do currículo lido
        String textoBruto = documento.text();
        System.out.println(textoBruto.substring(0, Math.min(textoBruto.length(), 600)) + "...");

        System.out.println("\n--- Passo 2: Conectando ao Ollama e Gerando Embedding ---");

        // Configura o cliente para conversar com o Ollama local
        EmbeddingModel modeloEmbedding = OllamaEmbeddingModel.builder()
                .baseUrl("http://localhost:11434")
                .modelName("nomic-embed-text")
                .build();

        System.out.println("Enviando texto para o modelo 'nomic-embed-text'...");
        // Envia o texto do currículo e recebe a lista de números (vetor)
        Embedding embedding = modeloEmbedding.embed(textoBruto).content();

        System.out.println("\n--- Sucesso! Vetor gerado pelo Ollama ---");
        System.out.println("Tamanho do vetor (Dimensões): " + embedding.dimension());
        System.out.println("Prévia dos primeiros 10 números do vetor: ");

        // O vetor é um array de floats gigantesco que representa o significado do currículo
        float[] valoresDoVetor = embedding.vector();
        System.out.println(Arrays.toString(Arrays.copyOfRange(valoresDoVetor, 0, 10)) + " ... e mais " + (valoresDoVetor.length - 10) + " números.");
    }
}