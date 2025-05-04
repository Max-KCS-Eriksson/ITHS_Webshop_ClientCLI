package com.maxeriksson.lab_02;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CommandLineRunnerImpl implements CommandLineRunner {

    private WebClient webClient;
    @Autowired private CommandLineInput terminal;

    @Override
    public void run(String... args) throws Exception {
        String baseUrl = "http://localhost:8080/api/products";
        webClient = WebClient.create(baseUrl);
    }
}
