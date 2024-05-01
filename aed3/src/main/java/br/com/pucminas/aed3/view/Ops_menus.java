package br.com.pucminas.aed3.view;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import br.com.pucminas.aed3.model.AnimeIMDB;

//Menu de opcoes do Usuario
public class Ops_menus {
    public static String loadCSVtoDB(String filePath) {
        String filePathD_db = "";
        // Implementar Logica Luan
        return filePathD_db;
    }

    // Obtem entradas para um objeto tipo AnimeIMDB
    public static void addEntry(Scanner scanner) {
        AnimeIMDB x = new AnimeIMDB();

        // Obtem ID
        Screen.print("Digite o ID: ");
        x.setId(scanner.nextInt());
        scanner.nextLine();
        // Obtem Titulo
        Screen.print("Digite o Titulo: ");
        x.setTitulo(scanner.nextLine());
        // Obtem Elenco
        Screen.print("Digitos os Elenco do anime (Separado por virgula): ");
        String generosInput = scanner.nextLine();
        List<String> generosList = Arrays.asList(generosInput.split(","));
        x.setGeneros(generosList);
        // Obtem Avaliacao
        Screen.print("Digite a avaliacao: ");
        x.setAvaliacao(scanner.nextFloat());
        scanner.nextLine();
        // Obtem numero de votos
        Screen.print("Digite o numero de votos: ");
        x.setVotos(scanner.nextInt());
        scanner.nextLine();
        // Obtem data lancamento
        Screen.print("Digite o ano de lancamento: ");
        x.setAnoLancamento((scanner.nextInt()));
        scanner.nextLine();
        // Obtem data encerramento
        Screen.print("Digite o ano de encerramento:");
        x.setAnoEncerramento((scanner.nextInt()));
        scanner.nextLine();
        // Obtem Resumo
        Screen.print("Digite o resumo: ");
        x.setResumo(scanner.nextLine());
        // Obtem Elenco
        Screen.print("Digitos os nome do elenco do anime (Separado por virgula): ");
        String ElencoInput = scanner.nextLine();
        List<String> ElencoList = Arrays.asList(ElencoInput.split(","));
        x.setElenco(ElencoList);
    }

    public static void readEntry(Scanner scanner) {
        // Implementar Logica Luan
    }

    public static void updateEntry(Scanner scanner) {
        // Implementar Logica Luan
    }

    public static void deleteEntry(Scanner scanner) {
        // Implementar Logica Luan
    }

    public static void rearrangeFile() {
        // Implementar Logica Luan
    }

    public static void saveAndExit(String filePath) {
        // Implementar Logica Luan
    }
}