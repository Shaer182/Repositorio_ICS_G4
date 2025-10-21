package Grupo4.EcoHarmonyParkBack;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;


@SpringBootApplication
public class EcoHarmonyParkBackApplication {
    @Resource
    private Environment env;

	public static void main(String[] args) {
		SpringApplication.run(EcoHarmonyParkBackApplication.class, args);
	}

    @PostConstruct
    public void init() {
        // Show the Swagger UI URL after the app starts
        String swaggerPath = "/swagger-ui/index.html";
        String newLines = "\n".repeat(3);

        System.out.printf("%s", newLines);
        System.out.printf("Local url: http://localhost:%s%s\n", env.getProperty("server.port"), swaggerPath);
        System.out.printf("%s", newLines);
    }
}
