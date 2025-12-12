package com.oryzem.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.cloud.storage.Storage;

import java.util.HashMap;
import java.util.Map;

@RestController  // Anotação para indicar que esta classe é um Controller REST
@RequestMapping("/api/debug")  // Todos os endpoints aqui começam com /api/debug
public class DebugController {

    @Autowired(required = false)  // 'required = false' evita erro se Storage não estiver configurado
    private Storage storage;

    @Value("${gcp.bucket-name:}")  // Valor padrão vazio se não configurado
    private String bucketName;

    @GetMapping("/config")  // Endpoint: GET /api/debug/config
    public Map<String, String> checkConfig() {
        Map<String, String> config = new HashMap<>();

        // Verifica variáveis de ambiente (sem mostrar valores sensíveis)
        String projectId = System.getenv("GCP_PROJECT_ID");
        String credentials = System.getenv("GCP_CREDENTIALS_JSON");

        config.put("projectId_set", projectId != null ? "YES" : "NO");
        config.put("bucketName", bucketName != null ? bucketName : "Não configurado");
        config.put("credentials_set", credentials != null ? "YES" : "NO");

        if (storage != null && bucketName != null && !bucketName.isEmpty()) {
            try {
                // Tenta acessar o bucket
                boolean bucketExists = storage.get(bucketName) != null;
                config.put("bucket_accessible", bucketExists ? "YES" : "NO");
                config.put("status", "✅ Google Cloud Storage conectado!");
            } catch (Exception e) {
                config.put("bucket_accessible", "ERROR: " + e.getMessage());
                config.put("status", "❌ Erro na conexão com GCP");
            }
        } else {
            config.put("bucket_accessible", "Storage não configurado");
            config.put("status", "⚠️ GCP Storage não configurado");
        }

        // Informações da aplicação
        config.put("app_name", "Oryzem Backend");
        config.put("api_version", "1.0");

        return config;
    }
}