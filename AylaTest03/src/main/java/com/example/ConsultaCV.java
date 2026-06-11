package com.example;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import java.util.List;

public class ConsultaCV {

    public void buscar(String buscaDoRecrutador, Conexao conexao) {
        System.out.println("Pesquisando candidatos compatíveis no banco...");
        var vetorDaBusca = conexao.getModeloEmbedding().embed(buscaDoRecrutador).content();

        // Busca os 3 melhores candidatos no Postgres
        List<EmbeddingMatch<TextSegment>> resultados = conexao.getBancoVetorial().findRelevant(vetorDaBusca, 3, 0.6);

        if (resultados.isEmpty()) {
            System.out.println("Nenhum currículo compatível encontrado para esta busca.");
        } else {
            System.out.println("\n--- Candidato(s) Encontrado(s)! Gerando Resumo Prático ---");

            for (EmbeddingMatch<TextSegment> match : resultados) {
                System.out.println("==================================================");
                System.out.printf("Grau de Afinidade Semântica: %.2f%%\n", match.score() * 100);
                System.out.println("==================================================");

                String textoBrutoDoCurriculo = match.embedded().text();

                // ⚠️ ESSA PARTE CRIA O PROMPT PARA O GRANITE
                String prompt = """
                        Você é um assistente de RH sênior. Baseado estritamente no currículo fornecido abaixo, crie um resumo prático e direto para o recrutador seguindo este formato:
                        - Nome do Candidato:
                        - Principais Tecnologias/Skills:
                        - Tempo aproximado de experiência na área:
                        - Pontos Fortes (Por que ele combina com a busca '%s'):
                        
                        Currículo:
                        %s
                        """.formatted(buscaDoRecrutador, textoBrutoDoCurriculo);

                System.out.println("A Ayla está analisando o perfil... (Aguarde alguns segundos)");

                // 🚀 CHAMA O GRANITE PARA ENVIAR O RESUMO
                String resumoDaIA = conexao.getModeloChat().generate(prompt);

                // IMPRIME APENAS O RESUMO DA IA (Substitua qualquer System.out antigo por este)
                System.out.println("\n" + resumoDaIA);
                System.out.println("==================================================\n");
            }
        }
    }
}