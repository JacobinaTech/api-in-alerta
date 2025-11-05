package br.com.inalerta.inalerta.repositories;

import br.com.inalerta.inalerta.entity.Alerta;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class FirebaseRepository {

    private final Firestore firestore;



    public void salvarAlerta(Alerta alerta) throws ExecutionException, InterruptedException {
        String id = UUID.randomUUID().toString();
        Map<String, Object> dadosParaSalvar = new HashMap<>();
        dadosParaSalvar.put("latitude", alerta.getLatitude());
        dadosParaSalvar.put("longitude", alerta.getLongitude());
        dadosParaSalvar.put("satelite", alerta.getSatelite());
        dadosParaSalvar.put("data", alerta.getData().toString());

        System.out.println("--- REPOSITORY-DEBUG: Tentando salvar doc ID: " + id + " na coleção 'alertas'");
        WriteResult result = firestore.collection("alertas").document(id).set(dadosParaSalvar).get();
        System.out.println("--- REPOSITORY-DEBUG: SUCESSO! Documento salvo em: " + result.getUpdateTime());
    }

    public void salvarAlertas(List<Alerta> alertas) throws ExecutionException, InterruptedException {
        for (Alerta alerta : alertas) {
            salvarAlerta(alerta);
        }
    }

    /**
     * Busca o nome do último arquivo salvo na coleção 'status'.
     */
    public String getUltimoArquivoProcessado() throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("status").document("inpeTracker");
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) return document.getString("ultimoArquivo");
        return null;
    }


     //Salva o nome do último arquivo salvo na coleção 'status'.

    public void setUltimoArquivoProcessado(String nomeArquivo) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("status").document("inpeTracker");
        Map<String, Object> data = Map.of(
                "ultimoArquivo", nomeArquivo,
                "dataAtualizacao", new Date()
        );
        docRef.set(data, SetOptions.merge()).get();
    }


    public List<Map<String, Object>> buscarAlertasMaisRecentes() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection("alertas")
                .orderBy("data", Query.Direction.DESCENDING)
                .limit(20)
                .get();

        List<Map<String, Object>> alertas = new ArrayList<>();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        for (DocumentSnapshot document : documents) {
            alertas.add(document.getData());
        }
        return alertas;
    }
}
