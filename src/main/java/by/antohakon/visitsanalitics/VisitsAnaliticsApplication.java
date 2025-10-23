package by.antohakon.visitsanalitics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class VisitsAnaliticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(VisitsAnaliticsApplication.class, args);
    }

}
