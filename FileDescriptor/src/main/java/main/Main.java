package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication

public class Main {
    public static void main(String[] args) {
        File theDir = new File(".FILES");
        theDir.mkdir();
        SpringApplication.run(Main.class, args);
    }
}
