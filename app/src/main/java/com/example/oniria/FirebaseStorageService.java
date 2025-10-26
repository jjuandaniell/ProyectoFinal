package com.example.oniria;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FirebaseStorageService {

    private static final String TAG = "FirebaseStorageService";
    private static final String RECEIPTS_FOLDER = "receipts";

    private final FirebaseStorage storage;
    private final StorageReference storageRef;

    public interface UploadCallback {
        void onProgress(int progress);
        void onSuccess(String downloadUrl);
        void onError(String error);
    }

    public FirebaseStorageService() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    public void uploadReceipt(Uri fileUri, UploadCallback callback) {
        if (fileUri == null) {
            callback.onError("URI del archivo es nulo");
            return;
        }

        // Crear nombre único para el archivo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "receipt_" + timeStamp + ".jpg";

        // Referencia al archivo en Firebase Storage
        StorageReference receiptRef = storageRef.child(RECEIPTS_FOLDER + "/" + fileName);

        // Subir el archivo
        UploadTask uploadTask = receiptRef.putFile(fileUri);

        // Monitorear el progreso
        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            callback.onProgress((int) progress);
            Log.d(TAG, "Progreso de subida: " + progress + "%");
        }).addOnSuccessListener(taskSnapshot -> {
            // Obtener URL de descarga
            receiptRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                String downloadUrl = downloadUri.toString();
                Log.d(TAG, "Archivo subido exitosamente. URL: " + downloadUrl);
                callback.onSuccess(downloadUrl);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error al obtener URL de descarga", e);
                callback.onError("Error al obtener URL: " + e.getMessage());
            });
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error al subir archivo", e);
            callback.onError("Error de subida: " + e.getMessage());
        });
    }

    /**
     * Eliminar un recibo de Firebase Storage
     */
    public void deleteReceipt(String fileUrl, DeleteCallback callback) {
        try {
            StorageReference fileRef = storage.getReferenceFromUrl(fileUrl);

            fileRef.delete().addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Archivo eliminado exitosamente");
                callback.onSuccess();
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error al eliminar archivo", e);
                callback.onError("Error al eliminar: " + e.getMessage());
            });
        } catch (Exception e) {
            Log.e(TAG, "Error al parsear URL", e);
            callback.onError("URL inválida: " + e.getMessage());
        }
    }

    public interface DeleteCallback {
        void onSuccess();
        void onError(String error);
    }
}

