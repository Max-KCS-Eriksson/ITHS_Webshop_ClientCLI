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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                    "Add new Product", "Show Products by Categories", "Change Product price",
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
            case 3 -> {
                List<Product> products = new ArrayList<>();
                while (products.size() == 0) {
                    products = searchProductByName();
                }
                Optional<Product> product;
                if (products.size() == 1) {
                    product = Optional.of(products.getFirst());
                    System.out.println();
                    printProductInfo(product.get());
                } else {
                    product = selectFromList(products);
                    if (product.isEmpty()) return; // Aborted
                }
                updateProductPrice(product.get());
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
            printProductInfo(product);
        }
    }

    private List<Product> searchProductByName() {
        String productName = terminal.inputString("Name");

        Flux<Product> productsfFlux =
                webClient
                        .get()
                        .uri(
                                uriBuilder ->
                                        uriBuilder
                                                .path("/search")
                                                .replaceQueryParam("name", productName)
                                                .build())
                        .retrieve()
                        .bodyToFlux(Product.class);

        List<Product> products = productsfFlux.collectList().block();
        return products;
    }

    private Optional<Product> selectFromList(List<Product> products) {
        printMenuChoices(products);
        int choice = terminal.inputInt("Choice") - 1;
        Product product;
        try {
            product = products.get(choice);
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
        return Optional.of(product);
    }

    private void updateProductPrice(Product product) {
        int price = terminal.inputInt("New Price");
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("price", "" + price);

        Mono<Product> productmMono =
                webClient
                        .patch()
                        .uri(
                                uriBuilder ->
                                        uriBuilder.path("/{productName}").build(product.getName()))
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Product.class);

        productmMono.subscribe();
    }

    private <T> void printMenuChoices(T[] choices) {
        printMenuChoices(Arrays.asList(choices));
    }

    private <T> void printMenuChoices(List<T> choices) {
        System.out.println();
        for (int i = 0; i < choices.size(); i++) {
            System.out.println((i + 1) + ") " + choices.get(i));
        }
        System.out.println("\n0) Exit/Main Menu");
    }

    private void printProductInfo(Product product) {
        System.out.println(product.getName());
        System.out.println("  " + product.getCategory().getName());
        System.out.println("  " + product.getPrice() + " SEK");
    }
}
