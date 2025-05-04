package com.maxeriksson.lab_02;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

@Component
public class CommandLineRunnerImpl implements CommandLineRunner {

    private WebClient webClient;
    @Autowired private CommandLineInput terminal;

    private boolean isRunning;

    @Override
    public void run(String... args) throws Exception {
        String baseUrl = "http://localhost:8080/api/products";
        webClient = WebClient.create(baseUrl);

        isRunning = true;
        while (isRunning) menu();

        terminal.close();
    }

    private void menu() {
        int choice = terminal.inputInt("Choice");
        switch (choice) {
            case 0 -> isRunning = false;
        }
    }

    private void printMenuChoices(String[] choices) {
        printMenuChoices(Arrays.asList(choices));
    }

    private void printMenuChoices(List<String> choices) {
        System.out.println();
        for (int i = 0; i < choices.size(); i++) {
            System.out.println((i + 1) + ") " + choices.get(i));
        }
        System.out.println("\n0) Exit/Main Menu");
    }
}
