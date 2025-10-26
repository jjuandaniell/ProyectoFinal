package com.example.oniria;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ReceiptCaptureActivity extends AppCompatActivity {

    private static final String TAG = "ReceiptCaptureActivity";
    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private ImageView previewImageView;
    private Button captureButton;
    private Button uploadButton;
    private Button deleteButton;
    private Button openDriveFolderButton;
    private ProgressBar uploadProgressBar;
    private TextView statusTextView;

    private File currentPhotoFile;
    private String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_capture);

        initializeViews();
        setupClickListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Capturar Recibo");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initializeViews() {
        previewImageView = findViewById(R.id.previewImageView);
        captureButton = findViewById(R.id.captureButton);
        uploadButton = findViewById(R.id.uploadButton);
        deleteButton = findViewById(R.id.deleteButton);
        openDriveFolderButton = findViewById(R.id.openDriveFolderButton);
        uploadProgressBar = findViewById(R.id.uploadProgressBar);
        statusTextView = findViewById(R.id.statusTextView);

        // Estado inicial
        uploadButton.setEnabled(false);
        deleteButton.setEnabled(false);
        openDriveFolderButton.setVisibility(View.GONE);
        uploadProgressBar.setVisibility(View.GONE);
    }

    private void setupClickListeners() {
        captureButton.setOnClickListener(v -> checkCameraPermissionAndCapture());
        uploadButton.setOnClickListener(v -> uploadImage());
        deleteButton.setOnClickListener(v -> showDeleteConfirmation());
        openDriveFolderButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://drive.google.com/drive/u/0/folders/1tkRS9nq1N7EWvX6Mz-nqVeYpytkbPwJa"));
            startActivity(browserIntent);
        });
    }

    private void checkCameraPermissionAndCapture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, "No se encontró aplicación de cámara", Toast.LENGTH_SHORT).show();
            return;
        }

        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException ex) {
            Log.e(TAG, "Error creando archivo de imagen", ex);
            Toast.makeText(this, "Error al crear archivo de imagen", Toast.LENGTH_SHORT).show();
            return;
        }

        currentPhotoFile = photoFile;
        Uri photoURI = FileProvider.getUriForFile(this,
                "com.example.oniria.fileprovider",
                photoFile);

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
        takePictureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        takePictureIntent.setClipData(ClipData.newUri(getContentResolver(), "Image", photoURI));

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "RECEIPT_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
            imageFileName,
            ".jpg",
            storageDir
        );

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            displayCapturedImage();
        }
    }

    private void displayCapturedImage() {
        if (currentPhotoFile != null && currentPhotoFile.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoPath);
                Bitmap rotatedBitmap = rotateImageIfRequired(bitmap, currentPhotoPath);
                previewImageView.setImageBitmap(rotatedBitmap);

                saveBitmapToFile(rotatedBitmap, currentPhotoFile);

                uploadButton.setEnabled(true);
                deleteButton.setEnabled(true);

                statusTextView.setText("Foto capturada. Puedes subirla o eliminarla.");
                statusTextView.setTextColor(getResources().getColor(android.R.color.holo_blue_dark, null));

                Log.d(TAG, "Imagen capturada y rotada: " + currentPhotoPath);
            } catch (IOException e) {
                Log.e(TAG, "Error al rotar la imagen", e);
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private Bitmap rotateImageIfRequired(Bitmap img, String path) throws IOException {
        ExifInterface ei = new ExifInterface(path);
        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(img, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(img, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(img, 270);
            default:
                return img;
        }
    }

    private static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void saveBitmapToFile(Bitmap bitmap, File file) {
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        } catch (IOException e) {
            Log.e(TAG, "Error al guardar el bitmap rotado", e);
        }
    }

    private void uploadImage() {
        if (currentPhotoFile == null || !currentPhotoFile.exists()) {
            Toast.makeText(this, "No hay imagen para subir", Toast.LENGTH_SHORT).show();
            return;
        }

        captureButton.setEnabled(false);
        uploadButton.setEnabled(false);
        deleteButton.setEnabled(false);
        uploadProgressBar.setVisibility(View.VISIBLE);
        statusTextView.setText("Subiendo imagen a Drive...");
        statusTextView.setTextColor(getResources().getColor(android.R.color.holo_orange_dark, null));

        uploadReceiptToN8n(currentPhotoFile);
    }

    private void uploadReceiptToN8n(final File imageFile) {
        final String webhookUrl = "https://primary-production-aa47.up.railway.app/webhook/Oniria-Recibos";

        if (!imageFile.exists()) {
            Log.e("UploadError", "El archivo de imagen que se intenta subir no existe: " + imageFile.getPath());
            return;
        }

        Log.d("UploadInfo", "Iniciando subida para el archivo: " + imageFile.getName() + " a la URL: " + webhookUrl);

        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                        "file",
                        imageFile.getName(),
                        RequestBody.create(imageFile, MediaType.parse("image/jpeg"))
                )
                .build();

        Request request = new Request.Builder()
                .url(webhookUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("UploadFail", "Error de red al conectar con n8n.", e);
                runOnUiThread(() -> {
                   resetUploadUIOnError("✗ Error de red al conectar con n8n.");
                   Toast.makeText(ReceiptCaptureActivity.this, "Error de conexión, intente más tarde", Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (responseBody == null) {
                        Log.e("UploadServerError", "Respuesta vacía del servidor n8n. Código: " + response.code());
                        runOnUiThread(() -> {
                            resetUploadUIOnError("✗ Error del servidor n8n: " + response.code());
                            Toast.makeText(ReceiptCaptureActivity.this, "Error del servidor: Respuesta vacía", Toast.LENGTH_LONG).show();
                        });
                        return;
                    }

                    final String responseBodyString = responseBody.string();

                    if (response.isSuccessful()) {
                        Log.d("UploadSuccess", "¡Subida exitosa! Respuesta de n8n: " + responseBodyString);
                        runOnUiThread(() -> {
                            uploadProgressBar.setVisibility(View.GONE);
                            statusTextView.setText("✓ Imagen subida a Drive exitosamente");
                            statusTextView.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                            Toast.makeText(ReceiptCaptureActivity.this, "Recibo subido a Drive", Toast.LENGTH_SHORT).show();
                            openDriveFolderButton.setVisibility(View.VISIBLE);
                            clearCurrentImage();
                            captureButton.setEnabled(true);
                        });
                    } else {
                        Log.e("UploadServerError", "Error desde el servidor n8n. Código: " + response.code() + ". Respuesta: " + responseBodyString);
                        runOnUiThread(() -> {
                            resetUploadUIOnError("✗ Error del servidor n8n: " + response.code());
                            Toast.makeText(ReceiptCaptureActivity.this, "Error del servidor n8n", Toast.LENGTH_LONG).show();
                        });
                    }
                }
            }
        });
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Eliminar foto")
            .setMessage("¿Estás seguro de que deseas eliminar esta foto?")
            .setPositiveButton("Eliminar", (dialog, which) -> deleteCurrentImage())
            .setNegativeButton("Cancelar", null)
            .show();
    }

    private void deleteCurrentImage() {
        if (currentPhotoFile != null && currentPhotoFile.exists()) {
            if (currentPhotoFile.delete()) {
                clearCurrentImage();
                statusTextView.setText("Foto eliminada");
                statusTextView.setTextColor(getResources().getColor(android.R.color.darker_gray, null));
                Toast.makeText(this, "Foto eliminada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Error al eliminar la foto", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearCurrentImage() {
        previewImageView.setImageResource(android.R.color.transparent); 
        currentPhotoFile = null;
        currentPhotoPath = null;
        uploadButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    private void resetUploadUIOnError(String statusMessage) {
        uploadProgressBar.setVisibility(View.GONE);
        captureButton.setEnabled(true);
        uploadButton.setEnabled(true);
        deleteButton.setEnabled(true);
        statusTextView.setText(statusMessage);
        statusTextView.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentPhotoFile != null && currentPhotoFile.exists()) {
            currentPhotoFile.delete();
        }
    }
}
