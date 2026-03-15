package com.group1.catalogservice;

import org.springframework.boot.SpringApplication;

public class TestCatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication.from(CatalogServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
