package br.com.inalerta.inalerta.service;

import br.com.inalerta.inalerta.entity.Alerta;
import br.com.inalerta.inalerta.repositories.FirebaseRepository;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlertaService {


    private static final String INPE_CSV_BASE_URL = "https://dataserver-coids.inpe.br/queimadas/queimadas/focos/csv/10min/";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    private final RestTemplate restTemplate;
    private final FirebaseRepository firebaseRepository;

    public List<Map<String, Object>> getUltimosFocos() {
        System.out.println("Endpoint /getUltimosFocos chamado. Buscando 20 alertas mais recentes do Firebase...");

        try {

            return firebaseRepository.buscarAlertasMaisRecentes();

        } catch (Exception e) {
            System.err.println("Erro ao buscar dados do Firebase para o front-end: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Scheduled(fixedRate = 600000, initialDelay = 10000)
    public void tarefaBuscarEsalvarFocos() {
        System.out.println("--- Tarefa Agendada: Verificando 'catch-up' de focos no INPE ---");

        try {
            // 1. Descobre qual é o último arquivo que *deveria* existir
            LocalDateTime dataArquivoMaisRecente = getTimestampDoUltimoArquivoDisponivel();
            String ultimoSalvoNome = firebaseRepository.getUltimoArquivoProcessado();

            LocalDateTime proximoProcessar;
            if (ultimoSalvoNome == null) {
                proximoProcessar = dataArquivoMaisRecente;
            } else {
                proximoProcessar = parseDataFromNomeArquivo(ultimoSalvoNome).plusMinutes(10);
            }


            // O "LOOP DE CATCH-UP" continua rodando ENQUANTO o arquivo que queremos processar
            // não for MAIS NOVO que o último arquivo que *deveria* existir.
            while (!proximoProcessar.isAfter(dataArquivoMaisRecente)) {

                String nomeArquivoLoop = formatNomeArquivo(proximoProcessar);
                String urlLoop = INPE_CSV_BASE_URL + nomeArquivoLoop;

                System.out.println("--- Processando 'catch-up': " + nomeArquivoLoop + " ---");

                int statusCode = buscarEsalvarNovosFocos(urlLoop);


                if (statusCode == 1 || statusCode == 0) {

                    firebaseRepository.setUltimoArquivoProcessado(nomeArquivoLoop);
                    if (statusCode == 1) {
                        System.out.println("--- 'Catch-up' salvo. Marcador atualizado para: " + nomeArquivoLoop + " ---");
                    } else {
                        System.out.println("--- 'Catch-up' vazio. Marcador atualizado para: " + nomeArquivoLoop + " ---");
                    }
                } else if (statusCode == -2) {

                    System.out.println("--- 'Catch-up' pulando arquivo " + nomeArquivoLoop + " (404). Marcando como processado.");
                    firebaseRepository.setUltimoArquivoProcessado(nomeArquivoLoop);
                } else if (statusCode == -1) {

                    System.out.println("--- 'Catch-up' falhou (Erro de Servidor). Marcador NÃO atualizado.");
                    break;
                }


                proximoProcessar = proximoProcessar.plusMinutes(10);
            }

            System.out.println("--- Tarefa Agendada: 'Catch-up' concluído. ---");

        } catch (Exception e) {
            System.err.println("Erro durante a tarefa agendada de 'catch-up' do INPE: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private int buscarEsalvarNovosFocos(String url) {
        List<Alerta> alertas;
        String csvData;

        try {
            csvData = restTemplate.getForObject(url, String.class);
            if (csvData == null || csvData.isEmpty()) {
                return 0;
            }
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return -2;
            }
            System.err.println("Erro (HttpClientErrorException) ao buscar dados do INPE: " + e.getMessage());
            return -1;
        } catch (Exception e) {
            System.err.println("Erro (Exception) ao buscar dados do INPE: " + e.getMessage());
            return -1;
        }

        try (StringReader reader = new StringReader(csvData)) {
            alertas = new CsvToBeanBuilder<Alerta>(reader)
                    .withType(Alerta.class)
                    .withSkipLines(1)
                    .build()
                    .parse();
        } catch (Exception e) {
            System.err.println("Erro ao processar CSV: " + e.getMessage());
            return -1;
        }

        if (alertas != null && !alertas.isEmpty()) {
            try {
                System.out.println("Salvando " + alertas.size() + " alertas no Firebase...");
                firebaseRepository.salvarAlertas(alertas);
                System.out.println("Alertas salvos com sucesso!");
                return 1;
            } catch (Exception e) {
                System.err.println("!!! ERRO AO SALVAR DADOS NO FIREBASE: " + e.getMessage());
                return -1;
            }
        }

        return 0;
    }



    private LocalDateTime getTimestampDoUltimoArquivoDisponivel() {
        LocalDateTime agoraUTC = LocalDateTime.now(ZoneId.of("UTC"));
        int minutoAtual = agoraUTC.getMinute();
        int minutoArredondado = (minutoAtual / 10) * 10;

        return agoraUTC.withMinute(minutoArredondado).withSecond(0).withNano(0).minusMinutes(10);
    }

    private LocalDateTime parseDataFromNomeArquivo(String nomeArquivo) {
        String dataString = nomeArquivo.substring(12, nomeArquivo.length() - 4);
        return LocalDateTime.parse(dataString, formatter);
    }

    private String formatNomeArquivo(LocalDateTime data) {
        return "focos_10min_" + data.format(formatter) + ".csv";
    }
}
