package br.com.pucminas.aed3;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.com.pucminas.aed3.view.Tela;
import lombok.extern.java.Log;

@SpringBootApplication
@Log
public class Aed3Application implements CommandLineRunner {

	@Autowired
	private Tela tela;

	public static void main(String[] args) {
		SpringApplication.run(Aed3Application.class, args);
	}

	@Override
	public void run(String... args) {
		try {
			tela.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
