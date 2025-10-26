package com.example.oniria;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class N8NWebhookService {

    private static final String TAG = "N8NWebhookService";
    private final OkHttpClient client;

    public interface WebhookCallback {
        void onSuccess();
        void onError(String error);
    }

    public N8NWebhookService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Envía notificación a n8n con la URL del recibo subido
     */
    public void sendReceiptNotification(String imageUrl, WebhookCallback callback) {
        // Obtener URL del webhook desde BuildConfig
        String webhookUrl = BuildConfig.N8N_WEBHOOK_URL;

        if (webhookUrl == null || webhookUrl.isEmpty() || webhookUrl.contains("tu-n8n-instance")) {
            Log.w(TAG, "URL del webhook n8n no configurada. Saltando notificación.");
            callback.onError("Webhook no configurado");
            return;
        }

        try {
            // Crear JSON con los datos del recibo
            JSONObject jsonData = new JSONObject();
            jsonData.put("receiptUrl", imageUrl);
            jsonData.put("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()).format(new Date()));
            jsonData.put("source", "oniria_android_app");
            jsonData.put("type", "receipt_upload");

            // Crear el request body
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(jsonData.toString(), JSON);

            // Crear el request
            Request request = new Request.Builder()
                    .url(webhookUrl)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            // Ejecutar request de forma asíncrona
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Error al enviar notificación a n8n", e);
                    callback.onError("Error de conexión: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "Notificación enviada a n8n exitosamente");
                        callback.onSuccess();
                    } else {
                        String errorMsg = "Error HTTP " + response.code();
                        Log.e(TAG, errorMsg);
                        callback.onError(errorMsg);
                    }
                    response.close();
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error al crear JSON", e);
            callback.onError("Error JSON: " + e.getMessage());
        }
    }
}

