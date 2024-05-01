package br.com.pucminas.aed3.view;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import br.com.pucminas.aed3.controller.AnimeIMDBController;
import br.com.pucminas.aed3.model.AnimeIMDB;
import lombok.Setter;

@Component
public class TelaCRUD {

    private Menu menu;
    private static final Integer Lpad = 8;
    private AnimeIMDB anime;

    @Setter
    private Scanner scan;

    @Autowired
    AnimeIMDBController controller;

    public void run() {
        Boolean active = true;
        while (active) {
            if (Objects.isNull(menu)) {
                menu = new Menu(
                        "1. Pesquisar registro,2. Criar registro,3. Alterar registro,4. Deletar registro,5. Ordenar arquivo,6. Indexar,9. Sair"
                                .split(","));
            }
            print();

            Screen.print("Selecione a opera\u00E7\u00E3o: ");
            Integer op = scan.nextInt();
            scan.nextLine();
            switch (op) {
                case 1:
                    pesquisar();
                    break;
                case 2:
                    inserir();
                    break;
                case 3:

                    alterar();
                    break;
                case 4:
                    deletar();
                    break;
                case 5:
                    ordenar();
                    break;
                case 6:
                    gerarIndex();
                    break;
                case 9:
                    active = false;
                    break;
                default:
                    print();
                    Screen.gotoXY(Lpad, 25);
                    Screen.print("Op\u00E7\u00E3o inv\u00E1lida");
                    break;
            }
        }
    }

    private void pesquisar() {
        Integer ID = null;
        try {
            print();
            Screen.print("Informe o ID do registro a ser buscado: ");
            ID = scan.nextInt();
            anime = controller.getById(ID);
            if (Objects.isNull(anime) || anime.getId() < 0) {
                Screen.gotoXY(Lpad, 10);
                Screen.clearLine();
                Screen.print(String.format("O id %d n\u00E3o foi encontrado", ID));
                Thread.sleep(5000);
                anime = null;
            }
        } catch (Exception e) {
            print();
            Screen.print(String.format("Erro ao buscar o ID %d\n", ID));
            Screen.print(String.format("Erro causado: %s", e.getMessage()));
        }
    }

    private void alterar() {
        String aux = "";

        try {
            while (true) {
                print();
                if (Objects.isNull(anime)) {

                    System.out
                            .print("Voc\u00EA precisa buscar um registro para alterar. Digite o ID do registro que quer alterar: ");
                    anime = controller.getById(scan.nextInt());
                    scan.nextLine();
                    print();
                }
                String mensagem = "Para alterar um campo digite x:valor onde x \u00E9 o campo que pretende alterar: ";
                Screen.print(mensagem);
                Screen.gotoXY(Lpad, 25);
                Screen.print("Legenda:\n");
                Screen.print("t - T\u00EDtulo\t\tl - Lan\u00E7amento\t\tf - Encerramento\n");
                Screen.print("n - Nota\t\tv - Votos\t\tr - Resumo\n");
                Screen.print("g - G\u00EAneros\t\te - Elenco\t\ts - Salvar");
                Screen.gotoXY(Lpad + mensagem.length() + 1, 10);
                aux = scan.nextLine();
                switch (Character.toUpperCase(aux.charAt(0))) {
                    case 'T':
                        anime.setTitulo(aux.substring(2));
                        break;
                    case 'L':
                        anime.setAnoLancamento(Integer.parseInt(aux.substring(2)));
                        break;
                    case 'F':
                        anime.setAnoEncerramento(Integer.parseInt(aux.substring(2)));
                        break;
                    case 'N':
                        anime.setAvaliacao(Float.parseFloat(aux.substring(2)));
                        break;
                    case 'V':
                        anime.setVotos(Integer.parseInt(aux.substring(2)));
                        break;
                    case 'R':
                        anime.setResumo(aux.substring(2));
                        break;
                    case 'G':
                        anime.setGeneros(Arrays.asList(aux.substring(2).split(",")));
                        break;
                    case 'E':
                        anime.setElenco(Arrays.asList(aux.substring(2).split(",")));
                        break;
                    case 'S':
                        controller.salvar(anime);
                    default:
                        Screen.gotoXY(Lpad, 10);
                        Screen.print("Op\u00E7\u00E3o inv\u00E1lida: Selecione uma op\u00E7\u00E3o v\u00E1lida!: ");
                        break;
                }
                if (aux.toUpperCase().substring(0, 1).equals("S")) {
                    break;
                }
            }
        } catch (Exception e) {
            print();
            Screen.print("Ocorreu um erro ao salvar a entidade em arquivo\n");
            Screen.print("Erro: " + e.getMessage());
        }
    }

    private void inserir() {
        anime = null;
        print();
        // scan.nextLine();
        Screen.print("Informe o T\u00EDtulo do anime: ");
        anime = new AnimeIMDB();
        anime.setTitulo(scan.nextLine());
        Screen.clearLine();
        Screen.print("Informe o ano de Lan\u00E7amento: ");
        anime.setAnoLancamento(scan.nextInt());
        scan.nextLine();
        Screen.clearLine();
        Screen.print("Informe o ano de encerramento (caso n\u00E3o tenha informe 0): ");
        anime.setAnoEncerramento(scan.nextInt());
        scan.nextLine();
        Screen.clearLine();
        Screen.print("Informe a nota: ");
        anime.setAvaliacao(scan.nextFloat());
        scan.nextLine();
        Screen.clearLine();
        Screen.print("Informe o n\u00FAmero total de votos: ");
        anime.setVotos(scan.nextInt());
        scan.nextLine();
        Screen.clearLine();
        Screen.print("Informe o resumo do anime: ");
        anime.setResumo(scan.nextLine());
        Screen.clearLine();
        Screen.print("Informe os g\u00EAneros do anime (separado por v\u00EDrgulas): ");
        String aux = scan.nextLine();
        anime.setGeneros(Arrays.asList(aux.split(",")));
        Screen.clearLine();
        Screen.print("Informe o elenco do anime (separado por virgulas): ");
        aux = scan.nextLine();
        anime.setElenco(Arrays.asList(aux.split(",")));
        anime.setCadastro(new Date());
        try {
            controller.salvar(anime);
            print();
        } catch (Exception e) {
            print();
            Screen.print("Ocorreu um erro ao salvar a entidade em arquivo\n");
            Screen.print("Erro: " + e.getMessage());
        }
    }

    private void deletar() {
        print();
        try {
            if (Objects.isNull(anime)) {

                System.out
                        .print("Voc\u00EA precisa buscar um registro para excluir. Digite o ID do registro que quer excluir: ");
                anime = controller.getById(scan.nextInt());
                print();
            }
            Screen.print("Confirma a exclus\u00E3o (S/N)? ");
            String aux = scan.next();
            if (aux.toUpperCase().charAt(0) == 'S') {
                controller.deletar(anime);
            }
        } catch (Exception e) {
            print();
            Screen.print("Ocorreu um erro ao excluir\n");
            Screen.print("Erro: " + e.getMessage());
        }
    }

    private void print() {
        menu.print();
        String aux;
        Screen.gotoXY(0, 13);
        Screen.clearLine();
        Screen.gotoXY(Lpad, 12);
        Screen.print("====================REGISTRO ATIVO=====================");
        if (Objects.nonNull(anime)) {
            Screen.gotoXY(Lpad, 13);
            Screen.clearLine();
            Screen.print(String.format("ID: %d\n", (anime.getId() < 0 ? null : anime.getId())));

            Screen.gotoXY(Lpad, 14);
            Screen.clearLine();
            aux = anime.getTitulo();
            Screen.print(String.format("T\u00EDtulo: %s\n",
                    (aux.length() > 70 ? aux.substring(0, 70) + "..."
                            : aux)));

            Screen.gotoXY(Lpad, 15);
            Screen.clearLine();
            Screen.print(String.format("Dura\u00E7\u00E3o: %s - %s\n",
                    (anime.getAnoLancamento().equals(0) ? "" : anime.getAnoLancamento().toString()),
                    (anime.getAnoEncerramento().equals(0) ? "" : anime.getAnoEncerramento().toString())));

            Screen.gotoXY(Lpad, 16);
            Screen.clearLine();
            Screen.print(String.format("Nota: %f\n", anime.getAvaliacao()));

            Screen.gotoXY(Lpad, 17);
            Screen.clearLine();
            Screen.print(String.format("Votos: %d\n", anime.getVotos()));

            Screen.gotoXY(Lpad, 18);
            Screen.clearLine();
            aux = anime.getResumo();
            Screen.print(String.format("Resumo: %s\n",
                    (aux.length() > 70 ? aux.substring(0, 70) + "..."
                            : aux)));

            Screen.gotoXY(Lpad, 19);
            Screen.clearLine();
            aux = String.join(",", anime.getGeneros());
            Screen.print(String.format("G\u00EAneros: %s\n",
                    (aux.length() > 70 ? aux.substring(0, 70) + "..."
                            : aux)));

            Screen.gotoXY(Lpad, 20);
            Screen.clearLine();
            aux = String.join(",", anime.getElenco());
            Screen.print(String.format("Elenco: %s\n",
                    (aux.length() > 70 ? aux.substring(0, 70) + "..."
                            : aux)));

            Screen.gotoXY(Lpad, 21);
            Screen.clearLine();
            SimpleDateFormat sf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

            Screen.print(String.format("Data de cadastro: %s\n",
                    (Objects.nonNull(anime.getCadastro()) ? sf.format(anime.getCadastro()) : "")));
        }

        Screen.gotoXY(Lpad, 10);
    }

    private void ordenar() {
        try {
            anime = null;
            print();
            Screen.gotoXY(Lpad, 15);
            Screen.clearLine();
            Screen.print("Opera\u00E7\u00E3o de ordena\u00E7\u00E3o em andamento...");

            atualizarIndex();

            // controller.ordenacaoExterna();

            Screen.gotoXY(Lpad, 15);
            Screen.clearLine();
            Screen.print("Opera\u00E7\u00E3o de ordena\u00E7\u00E3o conclu\u00EDda");
            Thread.sleep(5000);
        } catch (Exception e) {

        }
    }

    private void gerarIndex() {
        controller.gerarIndex();
    }

    private void atualizarIndex() {
        controller.atualizarIndex();
    }
}
