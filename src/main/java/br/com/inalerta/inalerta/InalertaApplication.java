package br.com.inalerta.inalerta;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.firestore.Firestore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@SpringBootApplication
@EnableScheduling
public class InalertaApplication {

	public static void main(String[] args) {
		SpringApplication.run(InalertaApplication.class, args);
	}

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {


        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();


        factory.setConnectTimeout(10000);

        factory.setReadTimeout(60000);

        // 3. Constrói o RestTemplate usando a fábrica configurada
        return builder
                .requestFactory(() -> factory)
                .build();
    }

    // A correção para o erro do LocalDateTime
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }


}
