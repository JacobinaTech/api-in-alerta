package br.com.inalerta.inalerta.service;

import br.com.inalerta.inalerta.repositories.FirebaseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AnaliseIncendioService {

    private final FirebaseRepository firebaseRepository;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    public AnaliseIncendioService(FirebaseRepository firebaseRepository,
                                  GeminiService geminiService,
                                  ObjectMapper objectMapper) {
        this.firebaseRepository = firebaseRepository;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
    }

    // Roda a cada 10 minutos
    @Scheduled(fixedRate = 600000, initialDelay = 60000)
    public void analisarRiscoDeIncendio() {
        System.out.println("--- Tarefa Agendada: Analisando os 100 alertas MAIS RECENTES ---");

        try {

            List<Map<String, Object>> alertas = firebaseRepository.buscarAlertasMaisRecentes();

            if (alertas.isEmpty()) {
                System.out.println("Nenhum alerta encontrado no banco de dados para analisar.");
                return;
            }

            System.out.println("Encontrados " + alertas.size() + " alertas mais recentes para analisar.");
            String dadosJson = objectMapper.writeValueAsString(alertas);


            String analiseDaIA = geminiService.analisarProbabilidadeDeIncendio(dadosJson);


            System.out.println("--- Resposta da Análise da IA ---");
            System.out.println(analiseDaIA);
            System.out.println("---------------------------------");



        } catch (Exception e) {
            System.err.println("Erro durante a tarefa agendada de análise: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
