package com.example.oniria;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class AIAssistantActivity extends AppCompatActivity {
    private RecyclerView chatRecyclerView;
    private EditText messageEditText;
    private ImageButton sendButton;
    private ProgressBar loadingProgressBar;
    private TextView emptyStateText;

    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private AIAssistantService aiService;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();

        aiService = new AIAssistantService(this);
        mainHandler = new Handler(Looper.getMainLooper());

        // Mensaje de bienvenida
        addWelcomeMessage();
    }

    private void initializeViews() {
        chatRecyclerView = findViewById(R.id.chatRecyclerView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        emptyStateText = findViewById(R.id.emptyStateText);

        // Configurar toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Oniria Assistant");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        updateEmptyState();
    }

    private void setupClickListeners() {
        sendButton.setOnClickListener(v -> sendMessage());

        messageEditText.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });
    }

    private void addWelcomeMessage() {
        String welcomeMessage = "¡Hola! Soy tu asistente financiero personal de Oniria. 🤖\n\n" +
                "Tengo acceso a todos tus datos financieros y puedo ayudarte con:\n" +
                "• Análisis de tus gastos e ingresos\n" +
                "• Consejos para alcanzar tus metas\n" +
                "• Planificación de presupuestos\n" +
                "• Recordatorios de pagos\n" +
                "• Y mucho más...\n\n" +
                "¿En qué puedo ayudarte hoy?";

        ChatMessage welcomeMsg = new ChatMessage(welcomeMessage, false);
        messages.add(welcomeMsg);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        updateEmptyState();
        scrollToBottom();
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // Añadir mensaje del usuario
        ChatMessage userMessage = new ChatMessage(messageText, true);
        messages.add(userMessage);
        chatAdapter.notifyItemInserted(messages.size() - 1);
        updateEmptyState();
        scrollToBottom();

        // Limpiar campo de texto y mostrar loading
        messageEditText.setText("");
        showLoading(true);
        sendButton.setEnabled(false);

        // Enviar mensaje a la IA
        aiService.getAIResponse(messageText, new AIAssistantService.AIResponseCallback() {
            @Override
            public void onResponse(String response) {
                mainHandler.post(() -> {
                    showLoading(false);
                    sendButton.setEnabled(true);

                    // Añadir respuesta de la IA
                    ChatMessage aiMessage = new ChatMessage(response, false);
                    messages.add(aiMessage);
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    updateEmptyState();
                    scrollToBottom();
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    showLoading(false);
                    sendButton.setEnabled(true);

                    String errorMessage = "Lo siento, ocurrió un error al procesar tu mensaje. " +
                                        "Por favor, verifica tu conexión a internet e intenta nuevamente.\n\n" +
                                        "Error: " + error;

                    ChatMessage errorMsg = new ChatMessage(errorMessage, false);
                    messages.add(errorMsg);
                    chatAdapter.notifyItemInserted(messages.size() - 1);
                    updateEmptyState();
                    scrollToBottom();

                    Toast.makeText(AIAssistantActivity.this, "Error al conectar con la IA", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showLoading(boolean show) {
        LinearLayout loadingContainer = findViewById(R.id.loadingContainer);
        if (loadingContainer != null) {
            loadingContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    private void updateEmptyState() {
        emptyStateText.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
        chatRecyclerView.setVisibility(messages.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void scrollToBottom() {
        if (messages.size() > 0) {
            chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
