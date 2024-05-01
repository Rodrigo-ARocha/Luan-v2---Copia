package br.com.pucminas.aed3.view;

import org.springframework.stereotype.Component;

@Component
public class Screen {

    private static final char esc = 0x1B;

    public static void clearScreen() {
        Screen.print(String.format("%c[H%c[2J", esc, esc));
        System.out.flush();
    }

    public static void gotoXY(int x, int y) {
        System.out.print(String.format("%c[%d;%df", esc, y, x));
        System.out.flush();
    }

    public static void clearLine() {
        System.out.print(String.format("%c[2K", esc));
    }

    public static void print(String message) {
        clearLine();
        System.out.print(message);
    }
}
