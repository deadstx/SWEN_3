package org.example.swen3backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Swen3BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Swen3BackendApplication.class, args);
    }

    @RestController
    public class HealthController {

        @GetMapping("/api/health")
        public String health() {
            return "Backend is running";
        }
    }

}
