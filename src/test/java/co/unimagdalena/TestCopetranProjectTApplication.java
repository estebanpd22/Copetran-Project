package co.unimagdalena;

import org.springframework.boot.SpringApplication;

public class TestCopetranProjectTApplication {

    public static void main(String[] args) {
        SpringApplication.from(CopetranProjectApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}