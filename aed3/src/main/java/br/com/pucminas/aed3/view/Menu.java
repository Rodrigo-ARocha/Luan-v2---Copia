package br.com.pucminas.aed3.view;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
public class Menu {

    @Getter
    @Setter
    private String[] opcoes;

    private final Integer Lpad = 8;

    public Menu(String[] itens) {
        opcoes = itens;
    }

    public void print() {
        Screen.clearScreen();
        Screen.gotoXY(Lpad, 2);
        Screen.print("===============SELECIONE A OPÇÃO DESEJADA==============");
        for (int i = 0; i < opcoes.length; i++) {
            Screen.gotoXY(Lpad, 4 + i);
            Screen.print(opcoes[i]);
        }

        Screen.gotoXY(Lpad, 4 + opcoes.length + 4);
        Screen.print("Digite a opção selecionada: ");
    }

}