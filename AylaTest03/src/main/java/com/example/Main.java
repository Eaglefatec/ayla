package com.example;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // Instancia as nossas classes separadas
        Conexao conexao = new Conexao();
        CadastroCV cadastroService = new CadastroCV();
        ConsultaCV consultaService = new ConsultaCV();

        int opcao = 0;

        while (opcao != 3) {
            System.out.println("\n=================================");
            System.out.println("      AYLA - RH INTELIGENTE      ");
            System.out.println("=================================");
            System.out.println("1. Cadastrar Novo Currículo (PDF)");
            System.out.println("2. Consultar Candidatos (Busca Semântica)");
            System.out.println("3. Sair");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = Integer.parseInt(sc.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Por favor, digite um número válido.");
                continue;
            }

            switch (opcao) {
                case 1:
                    System.out.print("\nDigite o caminho do PDF: ");
                    String caminhoPdf = sc.nextLine();
                    // Delega a ação para a classe de cadastro
                    cadastroService.cadastrar(caminhoPdf, conexao);
                    break;

                case 2:
                    System.out.print("\nO que você procura no candidato? ");
                    String busca = sc.nextLine();
                    // Delega a ação para a classe de consulta
                    consultaService.buscar(busca, conexao);
                    break;

                case 3:
                    System.out.println("Encerrando o sistema. Até mais!");
                    break;

                default:
                    System.out.println("Opção inválida!");
                    break;
            }
        }
        sc.close();
    }
}