package br.com.inalerta.inalerta;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Teste de integração: sobe o contexto completo, que inicializa o Firebase (FIREBASE_FILENAME)
 * e o Gemini (GEMINI_API_KEY) e agenda jobs @Scheduled. Só roda onde as credenciais existem
 * (máquina do desenvolvedor); no CI, sem credenciais, é pulado.
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "FIREBASE_FILENAME", matches = ".+")
class InalertaApplicationTests {

	@Test
	void contextLoads() {
	}

}
