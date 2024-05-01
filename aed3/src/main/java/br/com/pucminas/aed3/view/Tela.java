package br.com.pucminas.aed3.view;

import java.util.Objects;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.pucminas.aed3.controller.AnimeIMDBController;
import lombok.Getter;
import lombok.Setter;

@Component
@Getter
@Setter
public class Tela {

    private Menu menuPrincipal;

    private final Integer Lpad = 8;

    @Autowired
    private TelaCRUD crud;

    @Autowired
    private AnimeIMDBController controller;

    public void run() {
        Scanner scan = new Scanner(System.in);
        try {
            menuPrincipal = new Menu("1. Carregar dados CSV,2. Operar sobre arquivo binario".split(","));
            Screen.clearScreen();
            while (true) {
                menuPrincipal.print();
                switch (scan.nextInt()) {
                    case 1:
                        Screen.clearScreen();
                        Screen.gotoXY(Lpad, 2);
                        Screen.print("===============INFORME O ARQUIVO PARA IMPORTAÇÃO===============");
                        Screen.gotoXY(Lpad, 3);
                        Screen.print("= Essa operação irá sobrescrever os dados atuais              =");
                        Screen.gotoXY(Lpad, 4);
                        Screen.print("===============================================================");
                        Screen.gotoXY(Lpad, 8);
                        Screen.print("Digite o endereço do aruivo: ");
                        // String caminho = scan.next();
                    String caminho = "C:\\Users\\rodri\\Desktop\\Luan v2\\aed3\\imdb_anime.csv";
                        controller.carregarCSV(caminho);
                        Screen.gotoXY(Lpad, 9);
                        Screen.print("Carga de dados completa!!!");
                        break;
                    case 2:
                        crud.setScan(scan);
                        crud.run();
                        break;
                    default:
                        Screen.gotoXY(Lpad, 0);
                }
                Screen.gotoXY(Lpad, 15);
                Screen.print("Para retornar ao menu digite \"R\". Para encerrar digite \"S\": ");
                Character c = scan.next().toUpperCase().charAt(0);
                while (!((c.equals('R')) || c.equals('S'))) {
                    Screen.gotoXY(Lpad, 14);
                    Screen.print("Opção inválida!!!");
                    Screen.gotoXY(Lpad, 15);
                    Screen.print("Para retornar ao menu digite \"R\". Para encerrar digite \"S\": ");
                    c = scan.next().toUpperCase().charAt(0);
                }
                if (c.equals('R'))
                    Screen.clearScreen();
                else
                    break;
            }
        } catch (Exception e) {
            if (Objects.nonNull(scan))
                scan.close();

            Screen.clearScreen();
            e.printStackTrace();
        }
        scan.close();
    }

}
