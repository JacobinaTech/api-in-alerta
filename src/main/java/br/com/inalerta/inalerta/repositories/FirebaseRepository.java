package br.com.inalerta.inalerta.repositories;

import br.com.inalerta.inalerta.entity.Alerta;
import com.google.cloud.firestore.Firestore;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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


        firestore.collection("alertas").document(id).set(dadosParaSalvar).get();
    }


    public void salvarAlertas(List<Alerta> alertas) throws ExecutionException, InterruptedException {
        for (Alerta alerta : alertas) {
            salvarAlerta(alerta);
        }
    }
}
