package com.example;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader;
import dev.langchain4j.data.document.parser.apache.pdfbox.ApachePdfBoxDocumentParser;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.data.document.Metadata;
import java.io.File;

public class CadastroCV {

    private static final String PASTA_CURRICULOS = "src/main/resources/Curriculos/";

    public void cadastrar(String nomeArquivo, Conexao conexao) {
        if (!nomeArquivo.toLowerCase().endsWith(".pdf")) {
            nomeArquivo += ".pdf";
        }

        String caminhoCompleto = PASTA_CURRICULOS + nomeArquivo;
        File arquivo = new File(caminhoCompleto);

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

            Metadata metadados = Metadata.from("nome_arquivo", arquivo.getName());
            TextSegment segmentoTexto = TextSegment.from(documento.text(), metadados);

            var vetor = conexao.getModeloEmbedding().embed(segmentoTexto).content();

            System.out.println("Enviando dados para o PostgreSQL...");
            conexao.getBancoVetorial().add(vetor, segmentoTexto);

            System.out.println("🎉 Currículo '" + arquivo.getName() + "' cadastrado com sucesso!");

        } catch (Exception e) {
            System.out.println("❌ Erro ao processar ou salvar o PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}