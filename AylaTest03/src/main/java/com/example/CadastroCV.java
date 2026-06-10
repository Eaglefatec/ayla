package com.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import java.io.File;

public class CadastroCV {

    // Definimos a pasta padrão como uma constante
    private static final String PASTA_CURRICULOS = "src/main/resources/Curriculos/";

    public void cadastrar(String nomeArquivo, Conexao conexao) {
        // Se você esquecer de digitar o ".pdf" no final, o código adiciona sozinho
        if (!nomeArquivo.toLowerCase().endsWith(".pdf")) {
            nomeArquivo += ".pdf";
        }

        // Junta a pasta padrão com o nome do arquivo digitado
        String caminhoCompleto = PASTA_CURRICULOS + nomeArquivo;
        File arquivo = new File(caminhoCompleto);

        // Se mesmo assim não achar, tenta com o prefixo do projeto que você usava antes
        if (!arquivo.exists()) {
            caminhoCompleto = "AylaTest03/" + PASTA_CURRICULOS + nomeArquivo;
            arquivo = new File(caminhoCompleto);
        }

        if (!arquivo.exists()) {
            System.out.println("❌ Erro: Não encontrei nenhum arquivo com o nome '" + nomeArquivo + "' dentro da pasta " + PASTA_CURRICULOS);
            return;
        }

        System.out.println("Encontrado: " + arquivo.getName());
        System.out.println("Lendo PDF e gerando vetor... Aguarde.");

        try {
            Document documento = FileSystemDocumentLoader.loadDocument(arquivo.toPath(), new ApachePdfBoxDocumentParser());
            TextSegment segmentoTexto = TextSegment.from(documento.text());

            var vetor = conexao.getModeloEmbedding().embed(segmentoTexto).content();
            conexao.getBancoVetorial().add(vetor, segmentoTexto);

            System.out.println("🎉 Currículo cadastrado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao processar o PDF: " + e.getMessage());
        }
    }
}