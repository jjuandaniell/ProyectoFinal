package com.example.oniria;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
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

public class AIAssistantService {

    private static final String TAG = "AIAssistantService";
    private static final String GROQ_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private final OkHttpClient client;
    private final Context context;
    private final DatabaseHelper dbHelper;

    // Interfaz para devolver la respuesta de la IA
    public interface AIResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }

    public AIAssistantService(Context context) {
        this.context = context;
        this.dbHelper = new DatabaseHelper(context);
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public void getAIResponse(String userMessage, AIResponseCallback callback) {
        // Usar la API de Groq para respuestas reales de IA
        generateGroqResponseAsync(userMessage, callback);
        // generateLocalResponseAsync(userMessage, callback); // Solo para pruebas locales
    }

    private void generateGroqResponseAsync(String userMessage, AIResponseCallback callback) {
        String apiKey = BuildConfig.GROQ_API_KEY;
        Log.d(TAG, "API Key length: " + (apiKey != null ? apiKey.length() : "null"));

        if (apiKey == null || apiKey.isEmpty() || "tu_api_key_aqui".equals(apiKey)) {
            callback.onError("API key de Groq no configurada.");
            return;
        }

        try {
            String userContext = getUserFinancialContext();
            JSONObject requestBody = createGroqRequestBody(userMessage, userContext);

            Log.d(TAG, "Request body: " + requestBody.toString());

            RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url(GROQ_API_URL)
                    .post(body)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .build();

            Log.d(TAG, "Sending request to: " + GROQ_API_URL);

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Request failed: " + e.getMessage(), e);
                    callback.onError("Error de red: " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "No response body";
                    Log.d(TAG, "Response code: " + response.code());
                    Log.d(TAG, "Response body: " + responseBody);

                    if (!response.isSuccessful()) {
                        Log.e(TAG, "HTTP Error: " + response.code() + " " + response.message());
                        Log.e(TAG, "Response headers: " + response.headers());
                        callback.onError("Error del servidor: " + response.code() + " " + response.message() + "\nDetalle: " + responseBody);
                        return;
                    }

                    try {
                        String parsedResponse = parseGroqResponse(responseBody);
                        callback.onResponse(parsedResponse);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
                        callback.onError("Error al procesar la respuesta: " + e.getMessage());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "JSON creation error: " + e.getMessage(), e);
            callback.onError("Error al construir la solicitud: " + e.getMessage());
        }
    }

    private JSONObject createGroqRequestBody(String userMessage, String userContext) throws JSONException {
        JSONObject requestBody = new JSONObject();

        // Usar un modelo válido de Groq
        requestBody.put("model", "llama-3.1-8b-instant");

        // Configurar parámetros adicionales requeridos
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 1024);
        requestBody.put("top_p", 1.0);
        requestBody.put("stream", false);

        JSONArray messages = new JSONArray();

        // Mensaje de sistema (contexto) - más conciso para evitar límites de tokens
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        String systemContent = "Eres Oniria, un asistente financiero personal. Tu objetivo es dar consejos útiles, amigables y personalizados basados en los datos del usuario. Sé conciso y fácil de entender.\n\nDATOS DEL USUARIO:\n" +
            (userContext.length() > 2000 ? userContext.substring(0, 2000) + "..." : userContext);
        systemMessage.put("content", systemContent);
        messages.put(systemMessage);

        // Mensaje del usuario
        JSONObject userMessageObj = new JSONObject();
        userMessageObj.put("role", "user");
        userMessageObj.put("content", userMessage);
        messages.put(userMessageObj);

        requestBody.put("messages", messages);

        Log.d(TAG, "Final request body size: " + requestBody.toString().length());
        return requestBody;
    }

    private String parseGroqResponse(String responseBody) throws JSONException {
        JSONObject jsonResponse = new JSONObject(responseBody);

        if (jsonResponse.has("choices")) {
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (choices.length() > 0) {
                JSONObject firstChoice = choices.getJSONObject(0);
                if (firstChoice.has("message")) {
                    JSONObject message = firstChoice.getJSONObject("message");
                    if (message.has("content")) {
                        return message.getString("content");
                    }
                }
            }
        }

        // Verificar si hay error
        if (jsonResponse.has("error")) {
            JSONObject error = jsonResponse.getJSONObject("error");
            String errorMessage = error.optString("message", "Error desconocido");
            throw new JSONException("Error de Groq API: " + errorMessage);
        }

        throw new JSONException("No se encontró respuesta válida en Groq");
    }

    private void generateLocalResponseAsync(String userMessage, AIResponseCallback callback) {
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simular procesamiento
                String userContext = getUserFinancialContext();
                String response = generateLocalResponse(userContext, userMessage);
                callback.onResponse(response);
            } catch (Exception e) {
                callback.onError("Error generando respuesta local: " + e.getMessage());
            }
        }).start();
    }

    private String generateLocalResponse(String userContext, String userMessage) {
        String message = userMessage.toLowerCase().trim();

        // Extraer datos del contexto para respuestas personalizadas
        double sueldo = extractValue(userContext, "Sueldo mensual: Q([0-9,.]+)");
        double egresos = extractValue(userContext, "Egresos mensuales promedio: Q([0-9,.]+)");
        double ahorros = extractValue(userContext, "Ahorros disponibles: Q([0-9,.]+)");
        double balance = sueldo - egresos;

        // Respuestas inteligentes basadas en el mensaje del usuario
        if (message.contains("hola") || message.contains("hi") || message.isEmpty()) {
            return String.format("¡Hola! 👋 Soy tu asistente financiero personal de Oniria.\n\n" +
                    "He revisado tu situación financiera actual:\n" +
                    "• Sueldo mensual: Q%.2f\n" +
                    "• Gastos mensuales: Q%.2f\n" +
                    "• Ahorros disponibles: Q%.2f\n" +
                    "• Balance mensual: Q%.2f\n\n" +
                    "¡Tu situación financiera se ve muy bien! 💪\n\n" +
                    "¿En qué puedo ayudarte hoy? Puedes preguntarme sobre:\n" +
                    "• Análisis de gastos\n" +
                    "• Consejos de ahorro\n" +
                    "• Progreso hacia tus metas\n" +
                    "• Planificación financiera",
                    sueldo, egresos, ahorros, balance);
        }

        if (message.contains("gasto") || message.contains("gast") || message.contains("dinero")) {
            if (egresos > 0) {
                double porcentajeGastos = (egresos / sueldo) * 100;
                return String.format("📊 **Análisis de tus gastos:**\n\n" +
                        "Actualmente gastas Q%.2f mensuales, que representa el %.1f%% de tus ingresos.\n\n" +
                        "%s\n\n" +
                        "💡 **Recomendaciones:**\n" +
                        "• Mantén tus gastos por debajo del 70%% de tus ingresos\n" +
                        "• Revisa tus gastos en categorías como entretenimiento y compras\n" +
                        "• Considera hacer un presupuesto mensual detallado",
                        egresos, porcentajeGastos,
                        porcentajeGastos < 60 ? "¡Excelente! Tienes un control muy bueno de tus gastos. 👏" :
                        porcentajeGastos < 70 ? "Buen control de gastos, pero podrías optimizar un poco más. 👍" :
                        "Tus gastos están un poco altos. Te recomiendo revisar y reducir. ⚠️");
            }
        }

        if (message.contains("ahorr") || message.contains("meta") || message.contains("objetivo")) {
            return String.format("💰 **Tu situación de ahorro:**\n\n" +
                    "Tienes Q%.2f en ahorros disponibles. ¡Muy bien!\n\n" +
                    "Con tu balance mensual de Q%.2f, podrías:\n" +
                    "• Ahorrar Q%.2f adicionales cada mes\n" +
                    "• Alcanzar Q%.2f en 6 meses\n" +
                    "• Tener Q%.2f en un año\n\n" +
                    "🎯 **Para tus metas:**\n" +
                    "He visto que tienes metas importantes como una casa nueva. " +
                    "Con tu capacidad de ahorro actual, podrías hacer un plan realista para alcanzarlas.\n\n" +
                    "¿Te gustaría que te ayude a crear un plan específico para alguna meta?",
                    ahorros, balance, balance * 0.8, ahorros + (balance * 0.8 * 6), ahorros + (balance * 0.8 * 12));
        }

        if (message.contains("consejo") || message.contains("ayuda") || message.contains("qué hacer")) {
            return String.format("💡 **Consejos personalizados para ti:**\n\n" +
                    "Basándome en tu situación actual:\n\n" +
                    "**1. Fortalezas:** 💪\n" +
                    "• Excelente balance mensual de Q%.2f\n" +
                    "• Buenos ahorros de Q%.2f\n" +
                    "• Control adecuado de gastos\n\n" +
                    "**2. Oportunidades:** 🚀\n" +
                    "• Podrías invertir parte de tus ahorros\n" +
                    "• Considera un fondo de emergencia de Q%.2f (6 meses de gastos)\n" +
                    "• Evalúa inversiones de bajo riesgo\n\n" +
                    "**3. Siguiente paso:** 📝\n" +
                    "Te recomiendo definir un porcentaje fijo de ahorro mensual del 15-20%% de tus ingresos.",
                    balance, ahorros, egresos * 6);
        }

        if (message.contains("ingreso") || message.contains("sueldo") || message.contains("salario")) {
            return String.format("💼 **Análisis de tus ingresos:**\n\n" +
                    "Tu sueldo mensual de Q%.2f está muy bien. " +
                    "Tienes un balance positivo de Q%.2f cada mes.\n\n" +
                    "📈 **Oportunidades de crecimiento:**\n" +
                    "• Busca ingresos adicionales o freelance\n" +
                    "• Invierte en tu capacitación profesional\n" +
                    "• Considera inversiones que generen ingresos pasivos\n\n" +
                    "Con tu estabilidad actual, es el momento perfecto para hacer crecer tus ingresos. 🌟",
                    sueldo, balance);
        }

        // Respuesta general si no coincide con patrones específicos
        return String.format("🤔 Entiendo tu consulta. Basándome en tu situación financiera:\n\n" +
                "• Sueldo mensual: Q%.2f\n" +
                "• Balance disponible: Q%.2f\n" +
                "• Ahorros: Q%.2f\n\n" +
                "¿Podrías ser más específico sobre qué te gustaría saber?\n\n" +
                "Puedo ayudarte con:\n" +
                "• Análisis detallado de gastos\n" +
                "• Estrategias de ahorro\n" +
                "• Planificación para tus metas\n" +
                "• Consejos de inversión básicos",
                sueldo, balance, ahorros);
    }

    private double extractValue(String text, String pattern) {
        try {
            // Limpiar el texto de comas para el parsing a Double
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(text);
            if (m.find()) {
                String valueString = m.group(1).replace(",", "");
                return Double.parseDouble(valueString);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extrayendo valor con patrón '" + pattern + "': " + e.getMessage());
        }
        return 0.0;
    }

    private String getUserFinancialContext() {
        StringBuilder context = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        // Obtener perfil del usuario
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // 1. Perfil financiero básico
        Cursor profileCursor = db.query(
            DatabaseHelper.TABLE_USER_PROFILE,
            null,
            DatabaseHelper.COLUMN_USER_ID + " = ?",
            new String[]{String.valueOf(DatabaseHelper.DEFAULT_USER_PROFILE_ID)},
            null, null, null
        );

        if (profileCursor != null && profileCursor.moveToFirst()) {
            double sueldo = profileCursor.getDouble(profileCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SUELDO_MENSUAL));
            double egresos = profileCursor.getDouble(profileCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EGRESOS_MENSUALES));
            double ahorros = profileCursor.getDouble(profileCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AHORROS_DISPONIBLES));

            context.append("PERFIL FINANCIERO DEL USUARIO:\n");
            context.append(String.format(Locale.US, "- Sueldo mensual: Q%.2f\n", sueldo));
            context.append(String.format(Locale.US, "- Egresos mensuales promedio: Q%.2f\n", egresos));
            context.append(String.format(Locale.US, "- Ahorros disponibles: Q%.2f\n", ahorros));
            context.append(String.format(Locale.US, "- Balance mensual estimado: Q%.2f\n\n", sueldo - egresos));
        }
        if(profileCursor != null) profileCursor.close();

        // 2. Metas financieras
        context.append("METAS FINANCIERAS:\n");
        Cursor goalsCursor = db.query(DatabaseHelper.TABLE_FINANCIAL_GOALS, null, null, null, null, null, null);
        if (goalsCursor != null && goalsCursor.getCount() > 0) {
            while (goalsCursor.moveToNext()) {
                String goalName = goalsCursor.getString(goalsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL_NAME));
                double goalBudget = goalsCursor.getDouble(goalsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_GOAL_BUDGET));
                context.append(String.format(Locale.US, "- %s: Q%.2f presupuestado\n", goalName, goalBudget));
            }
        } else {
            context.append("- No hay metas financieras definidas\n");
        }
        if(goalsCursor != null) goalsCursor.close();
        context.append("\n");

        // 3. Transacciones recientes (últimos 30 días)
        long thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000);
        context.append("TRANSACCIONES RECIENTES (ÚLTIMOS 30 DÍAS):\n");

        Cursor transactionsCursor = db.query(
            DatabaseHelper.TABLE_TRANSACTIONS,
            null,
            DatabaseHelper.COLUMN_TRANSACTION_DATE_MILLIS + " >= ?",
            new String[]{String.valueOf(thirtyDaysAgo)},
            null, null,
            DatabaseHelper.COLUMN_TRANSACTION_DATE_MILLIS + " DESC",
            "20"
        );

        double totalIncome = 0, totalExpenses = 0;
        if (transactionsCursor != null && transactionsCursor.getCount() > 0) {
            while (transactionsCursor.moveToNext()) {
                String description = transactionsCursor.getString(transactionsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_DESCRIPTION));
                double amount = transactionsCursor.getDouble(transactionsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_AMOUNT));
                String type = transactionsCursor.getString(transactionsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_TYPE));
                long dateMillis = transactionsCursor.getLong(transactionsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_DATE_MILLIS));
                String category = transactionsCursor.getString(transactionsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TRANSACTION_CATEGORY));

                String date = dateFormat.format(new Date(dateMillis));
                String categoryText = (category != null && !category.isEmpty()) ? " (" + category + ")" : "";

                context.append(String.format(Locale.US, "- %s: %s Q%.2f - %s%s\n",
                    date,
                    type.equals("income") ? "Ingreso" : "Gasto",
                    amount,
                    description,
                    categoryText));

                if (type.equals("income")) {
                    totalIncome += amount;
                } else {
                    totalExpenses += amount;
                }
            }
            context.append(String.format(Locale.US, "\nRESUMEN ÚLTIMOS 30 DÍAS:\n"));
            context.append(String.format(Locale.US, "- Total ingresos: Q%.2f\n", totalIncome));
            context.append(String.format(Locale.US, "- Total gastos: Q%.2f\n", totalExpenses));
            context.append(String.format(Locale.US, "- Balance: Q%.2f\n\n", totalIncome - totalExpenses));
        } else {
            context.append("- No hay transacciones registradas en los últimos 30 días\n\n");
        }
        if(transactionsCursor != null) transactionsCursor.close();

        // 4. Recordatorios próximos
        context.append("RECORDATORIOS PRÓXIMOS:\n");
        long sevenDaysFromNow = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000);
        Cursor remindersCursor = db.query(
            DatabaseHelper.TABLE_REMINDERS,
            null,
            DatabaseHelper.COLUMN_REMINDER_TIME_MILLIS + " BETWEEN ? AND ?",
            new String[]{String.valueOf(System.currentTimeMillis()), String.valueOf(sevenDaysFromNow)},
            null, null,
            DatabaseHelper.COLUMN_REMINDER_TIME_MILLIS + " ASC"
        );

        if (remindersCursor != null && remindersCursor.getCount() > 0) {
            while (remindersCursor.moveToNext()) {
                String title = remindersCursor.getString(remindersCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_TITLE));
                String content = remindersCursor.getString(remindersCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_CONTENT));
                long reminderTime = remindersCursor.getLong(remindersCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_TIME_MILLIS));
                String periodicity = remindersCursor.getString(remindersCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REMINDER_PERIODICITY));

                String reminderDate = dateFormat.format(new Date(reminderTime));
                String periodicityText = (periodicity != null && !periodicity.equals("Una vez")) ? " (Periódico: " + periodicity + ")" : "";

                context.append(String.format(Locale.US, "- %s: %s - %s%s\n", reminderDate, title, content, periodicityText));
            }
        } else {
            context.append("- No hay recordatorios próximos\n");
        }
        if(remindersCursor != null) remindersCursor.close();

        // No cierres la base de datos aquí si dbHelper es un singleton o se reutiliza.
        // db.close();
        return context.toString();
    }
}
