package com.maxeriksson.lab_02;

import com.maxeriksson.lab_02.model.Category;
import com.maxeriksson.lab_02.model.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        printMenuChoices(
                new String[] {
                    "Add new Product", "Show Products by Categories",
                });

        int choice = terminal.inputInt("Choice");
        switch (choice) {
            case 1 -> {
                Product product = createProduct();
                addNewProduct(product);
            }
            case 2 -> {
                List<Category> categories = showCategories();
                showProductsByCategory(categories);
            }
            case 0 -> isRunning = false;
        }
    }

    private Product createProduct() {
        System.out.println();

        String name = terminal.inputString("Name");
        int price = terminal.inputInt("Price");
        String categoryName = terminal.inputString("Category");
        Category category = new Category(categoryName);

        return new Product(name, price, category);
    }

    private void addNewProduct(Product product) {
        Mono<Product> productmMono =
                webClient.post().bodyValue(product).retrieve().bodyToMono(Product.class);

        productmMono.subscribe();
    }

    private List<Category> showCategories() {
        Mono<List<Category>> categoriesMono =
                webClient
                        .get()
                        .uri("/categories")
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<List<Category>>() {});

        List<Category> categories = categoriesMono.block();
        List<String> categoryNames =
                categories.stream()
                        .map(category -> category.getName())
                        .collect(Collectors.toList());
        printMenuChoices(categoryNames);

        return categories;
    }

    private void showProductsByCategory(List<Category> categories) {
        int choice = terminal.inputInt("Choice") - 1;
        if (choice < 0) return;

        Category category = categories.get(choice);
        Flux<Product> productfFlux =
                webClient
                        .get()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path("/category/{category}")
                                                .build(category.getName()))
                        .retrieve()
                        .bodyToFlux(Product.class);

        List<Product> products = productfFlux.collectList().block();
        System.out.println();
        for (Product product : products) {
            System.out.println(product.getName());
            System.out.println("  " + product.getCategory().getName());
            System.out.println("  " + product.getPrice() + " SEK");
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
