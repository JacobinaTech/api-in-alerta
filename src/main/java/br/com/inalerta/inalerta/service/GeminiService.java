package br.com.inalerta.inalerta.service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;



@Service
public class GeminiService {




    private final RestTemplate restTemplate;

    @Value("${GEMINI_API_KEY}")
    private  String geminiApiKey;


    private final String geminiApiUrl = "https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent";


    public GeminiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String analisarProbabilidadeDeIncendio(String dadosQueimadasJson) {

        String prompt = "Você é um analista de risco de incêndio florestal. " +
                "Com base na lista de focos de queimada detectados por satélite, " +
                "gere uma breve análise de risco em português (Brasil). " +
                "Identifique os pontos de maior preocupação (ex: múltiplos focos próximos) " +
                "e sugira possíveis riscos. Seja direto e breve (1-2 parágrafos). \n\n" +
                "Dados dos Alertas:\n" +
                dadosQueimadasJson;


        String requestBody = String.format("{\"contents\":[{\"parts\":[{\"text\": \"%s\"}]}]}", prompt.replace("\"", "\\\""));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", geminiApiKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            String respostaBrutaJson = restTemplate.postForObject(geminiApiUrl, requestEntity, String.class);
            return extrairTextoDaResposta(respostaBrutaJson);

        } catch (Exception e) {
            System.err.println("Erro ao chamar a API do Gemini (RestTemplate): " + e.getMessage());
            return "Erro na análise da IA.";
        }
    }

    private String extrairTextoDaResposta(String respostaJson) {
        try {
            return respostaJson.split("\"text\": \"")[1].split("\"")[0].replace("\\n", "\n");
        } catch (Exception e) {
            return "Erro ao processar resposta da IA: " + respostaJson;
        }
    }
}
