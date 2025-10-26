package com.example.oniria;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "oniria.db";
    private static final int DATABASE_VERSION = 7;

    private static final String TAG = "DatabaseHelper";
    private static int transactionCounter = 0;

    // Tabla UserProfile
    public static final String TABLE_USER_PROFILE = "user_profile";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_SUELDO_MENSUAL = "sueldo_mensual";
    public static final String COLUMN_EGRESOS_MENSUALES = "egresos_mensuales";
    public static final String COLUMN_AHORROS_DISPONIBLES = "ahorros_disponibles";
    public static final String COLUMN_PROFILE_SETUP_COMPLETE = "profile_setup_complete";
    public static final long DEFAULT_USER_PROFILE_ID = 1;

    private static final String SQL_CREATE_TABLE_USER_PROFILE =
            "CREATE TABLE " + TABLE_USER_PROFILE + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_SUELDO_MENSUAL + " REAL," +
                    COLUMN_EGRESOS_MENSUALES + " REAL," +
                    COLUMN_AHORROS_DISPONIBLES + " REAL," +
                    COLUMN_PROFILE_SETUP_COMPLETE + " INTEGER NOT NULL DEFAULT 0);";

    // Tabla FinancialGoals
    public static final String TABLE_FINANCIAL_GOALS = "financial_goals";
    public static final String COLUMN_GOAL_ID = "goal_id";
    public static final String COLUMN_GOAL_NAME = "goal_name";
    public static final String COLUMN_GOAL_BUDGET = "goal_budget";
    private static final String SQL_CREATE_TABLE_FINANCIAL_GOALS =
            "CREATE TABLE " + TABLE_FINANCIAL_GOALS + " (" +
                    COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_GOAL_NAME + " TEXT NOT NULL," +
                    COLUMN_GOAL_BUDGET + " REAL NOT NULL);";

    // Tabla Reminders
    public static final String TABLE_REMINDERS = "reminders";
    public static final String COLUMN_REMINDER_ID = "reminder_id";
    public static final String COLUMN_REMINDER_TITLE = "reminder_title";
    public static final String COLUMN_REMINDER_CONTENT = "reminder_content";
    public static final String COLUMN_REMINDER_TIME_MILLIS = "reminder_time_millis";
    public static final String COLUMN_REMINDER_IS_PERIODIC = "reminder_is_periodic";
    public static final String COLUMN_REMINDER_PERIODICITY = "reminder_periodicity";
    public static final String COLUMN_REMINDER_ORIGINAL_TIME_MILLIS = "reminder_original_time_millis";
    private static final String SQL_CREATE_TABLE_REMINDERS =
            "CREATE TABLE " + TABLE_REMINDERS + " (" +
                    COLUMN_REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_REMINDER_TITLE + " TEXT NOT NULL," +
                    COLUMN_REMINDER_CONTENT + " TEXT," +
                    COLUMN_REMINDER_TIME_MILLIS + " INTEGER NOT NULL," +
                    COLUMN_REMINDER_IS_PERIODIC + " INTEGER NOT NULL DEFAULT 0," +
                    COLUMN_REMINDER_PERIODICITY + " TEXT," +
                    COLUMN_REMINDER_ORIGINAL_TIME_MILLIS + " INTEGER);";

    // Tabla Transactions
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COLUMN_TRANSACTION_ID = "transaction_id";
    public static final String COLUMN_TRANSACTION_DESCRIPTION = "description";
    public static final String COLUMN_TRANSACTION_AMOUNT = "amount";
    public static final String COLUMN_TRANSACTION_TYPE = "type"; // "income" o "expense"
    public static final String COLUMN_TRANSACTION_DATE_MILLIS = "date_millis";
    public static final String COLUMN_TRANSACTION_CATEGORY = "category";

    private static final String SQL_CREATE_TABLE_TRANSACTIONS =
            "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                    COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_TRANSACTION_DESCRIPTION + " TEXT NOT NULL," +
                    COLUMN_TRANSACTION_AMOUNT + " REAL NOT NULL," +
                    COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL CHECK(" + COLUMN_TRANSACTION_TYPE + " IN ('income', 'expense'))," +
                    COLUMN_TRANSACTION_DATE_MILLIS + " INTEGER NOT NULL," +
                    COLUMN_TRANSACTION_CATEGORY + " TEXT);";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: Creando la base de datos y tablas... Versión: " + DATABASE_VERSION);
        db.execSQL(SQL_CREATE_TABLE_USER_PROFILE);
        db.execSQL(SQL_CREATE_TABLE_FINANCIAL_GOALS);
        db.execSQL(SQL_CREATE_TABLE_REMINDERS);
        db.execSQL(SQL_CREATE_TABLE_TRANSACTIONS);
        Log.i(TAG, "onCreate: Todas las tablas creadas.");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "onUpgrade: Actualizando la base de datos de la versión " + oldVersion + " a " + newVersion);

        if (oldVersion < 5) {
            try {
                Log.i(TAG, "Actualizando a versión < 5: Añadiendo " + COLUMN_PROFILE_SETUP_COMPLETE + " a " + TABLE_USER_PROFILE);
                Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_USER_PROFILE + "'", null);
                if (c != null && c.getCount() > 0) {
                    c.close(); 
                    db.execSQL("ALTER TABLE " + TABLE_USER_PROFILE + " ADD COLUMN " + COLUMN_PROFILE_SETUP_COMPLETE + " INTEGER NOT NULL DEFAULT 0;");
                    Log.i(TAG, "Columna " + COLUMN_PROFILE_SETUP_COMPLETE + " añadida a " + TABLE_USER_PROFILE + ".");
                } else if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error al añadir columna " + COLUMN_PROFILE_SETUP_COMPLETE + " a " + TABLE_USER_PROFILE + ": " + e.getMessage());
            }
        }

        if (oldVersion < 7) {
            try {
                Log.i(TAG, "Actualizando a versión < 7: Añadiendo " + COLUMN_TRANSACTION_CATEGORY + " a " + TABLE_TRANSACTIONS);
                Cursor cursor = db.rawQuery("PRAGMA table_info(" + TABLE_TRANSACTIONS + ")", null);
                boolean columnExists = false;
                if (cursor != null) {
                    int nameColumnIndex = cursor.getColumnIndex("name");
                    while (cursor.moveToNext()) {
                        if (nameColumnIndex != -1 && COLUMN_TRANSACTION_CATEGORY.equals(cursor.getString(nameColumnIndex))) {
                            columnExists = true;
                            break;
                        }
                    }
                    cursor.close();
                }
                if (!columnExists) {
                    db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN " + COLUMN_TRANSACTION_CATEGORY + " TEXT;");
                    Log.i(TAG, "Columna " + COLUMN_TRANSACTION_CATEGORY + " añadida a " + TABLE_TRANSACTIONS + ".");
                } else {
                    Log.i(TAG, "Columna " + COLUMN_TRANSACTION_CATEGORY + " ya existe en " + TABLE_TRANSACTIONS + ". No se requiere ALTER.");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error durante la migración al añadir columna " + COLUMN_TRANSACTION_CATEGORY + " a " + TABLE_TRANSACTIONS + ": " + e.getMessage());
            }
        }
        
        Log.i(TAG, "onUpgrade: Asegurando todas las tablas para la version " + newVersion + " usando CREATE TABLE IF NOT EXISTS.");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USER_PROFILE + " (" +
                COLUMN_USER_ID + " INTEGER PRIMARY KEY," +
                COLUMN_SUELDO_MENSUAL + " REAL," +
                COLUMN_EGRESOS_MENSUALES + " REAL," +
                COLUMN_AHORROS_DISPONIBLES + " REAL," +
                COLUMN_PROFILE_SETUP_COMPLETE + " INTEGER NOT NULL DEFAULT 0);"
        );
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_FINANCIAL_GOALS + " (" +
                COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_GOAL_NAME + " TEXT NOT NULL," +
                COLUMN_GOAL_BUDGET + " REAL NOT NULL);"
        );
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REMINDERS + " (" +
                COLUMN_REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_REMINDER_TITLE + " TEXT NOT NULL," +
                COLUMN_REMINDER_CONTENT + " TEXT," +
                COLUMN_REMINDER_TIME_MILLIS + " INTEGER NOT NULL," +
                COLUMN_REMINDER_IS_PERIODIC + " INTEGER NOT NULL DEFAULT 0," +
                COLUMN_REMINDER_PERIODICITY + " TEXT," +
                COLUMN_REMINDER_ORIGINAL_TIME_MILLIS + " INTEGER);"
        );
        
        Log.d(TAG, "onUpgrade: Ejecutando CREATE TABLE IF NOT EXISTS para " + TABLE_TRANSACTIONS);
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TRANSACTIONS + " (" +
                COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_TRANSACTION_DESCRIPTION + " TEXT NOT NULL," +
                COLUMN_TRANSACTION_AMOUNT + " REAL NOT NULL," +
                COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL CHECK(" + COLUMN_TRANSACTION_TYPE + " IN ('income', 'expense'))," +
                COLUMN_TRANSACTION_DATE_MILLIS + " INTEGER NOT NULL," +
                COLUMN_TRANSACTION_CATEGORY + " TEXT);"
        );
        Log.i(TAG, "Proceso de onUpgrade completado para version " + newVersion + ". Todas las tablas deberían existir con el esquema más reciente.");
    }

    // --- Métodos para UserProfile ---
    public boolean upsertUserProfile(float sueldo, float egresos, float ahorros, boolean isProfileSetupComplete) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SUELDO_MENSUAL, sueldo);
        values.put(COLUMN_EGRESOS_MENSUALES, egresos);
        values.put(COLUMN_AHORROS_DISPONIBLES, ahorros);
        values.put(COLUMN_PROFILE_SETUP_COMPLETE, isProfileSetupComplete ? 1 : 0);
        int rowsAffected = db.update(TABLE_USER_PROFILE, values, COLUMN_USER_ID + " = ?",
                new String[]{String.valueOf(DEFAULT_USER_PROFILE_ID)});
        boolean success;
        if (rowsAffected > 0) {
            success = true;
        } else {
            values.put(COLUMN_USER_ID, DEFAULT_USER_PROFILE_ID);
            long newRowId = db.insert(TABLE_USER_PROFILE, null, values);
            success = newRowId != -1;
        }
        return success;
    }

    public UserProfileData getUserProfile() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        UserProfileData userProfile = null;
        try {
            cursor = db.query(TABLE_USER_PROFILE,
                    new String[]{COLUMN_SUELDO_MENSUAL, COLUMN_EGRESOS_MENSUALES, COLUMN_AHORROS_DISPONIBLES, COLUMN_PROFILE_SETUP_COMPLETE},
                    COLUMN_USER_ID + " = ?",
                    new String[]{String.valueOf(DEFAULT_USER_PROFILE_ID)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                float sueldo = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_SUELDO_MENSUAL));
                float egresos = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_EGRESOS_MENSUALES));
                float ahorros = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_AHORROS_DISPONIBLES));
                int setupCompleteFlag = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROFILE_SETUP_COMPLETE));
                userProfile = new UserProfileData(sueldo, egresos, ahorros, setupCompleteFlag == 1);
            } else {
                Log.d(TAG, "getUserProfile: No se encontró perfil. Creando uno por defecto.");
                ContentValues defaultValues = new ContentValues();
                defaultValues.put(COLUMN_USER_ID, DEFAULT_USER_PROFILE_ID);
                defaultValues.put(COLUMN_SUELDO_MENSUAL, 0f);
                defaultValues.put(COLUMN_EGRESOS_MENSUALES, 0f);
                defaultValues.put(COLUMN_AHORROS_DISPONIBLES, 0f);
                defaultValues.put(COLUMN_PROFILE_SETUP_COMPLETE, 0);
                db.insert(TABLE_USER_PROFILE, null, defaultValues);
                userProfile = new UserProfileData(0, 0, 0, false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener UserProfile: " + e.getMessage());
            userProfile = new UserProfileData(0, 0, 0, false); // Fallback
        } finally {
            if (cursor != null) cursor.close();
        }
        return userProfile;
    }

    public static class UserProfileData {
        public final float sueldoMensual;
        public final float egresosMensuales;
        public final float ahorrosDisponibles;
        public final boolean isProfileSetupComplete;
        public UserProfileData(float sueldoMensual, float egresosMensuales, float ahorrosDisponibles, boolean isProfileSetupComplete) {
            this.sueldoMensual = sueldoMensual;
            this.egresosMensuales = egresosMensuales;
            this.ahorrosDisponibles = ahorrosDisponibles;
            this.isProfileSetupComplete = isProfileSetupComplete;
        }
    }

    // --- Métodos para FinancialGoals ---
    public boolean addFinancialGoal(String name, float budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL_NAME, name);
        values.put(COLUMN_GOAL_BUDGET, budget);
        long newRowId = db.insert(TABLE_FINANCIAL_GOALS, null, values);
        return newRowId != -1;
    }

    public List<FinancialGoalData> getAllFinancialGoals() {
        List<FinancialGoalData> goalsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_FINANCIAL_GOALS, null, null, null, null, null, COLUMN_GOAL_ID + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_GOAL_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_NAME));
                    float budget = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_GOAL_BUDGET));
                    goalsList.add(new FinancialGoalData(id, name, budget));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener todas las FinancialGoals: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        return goalsList;
    }
    
    public void clearAllFinancialGoals() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FINANCIAL_GOALS, null, null);
    }

    public boolean updateFinancialGoal(long goalId, String name, float budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GOAL_NAME, name);
        values.put(COLUMN_GOAL_BUDGET, budget);
        int rowsAffected = db.update(TABLE_FINANCIAL_GOALS, values, COLUMN_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        return rowsAffected > 0;
    }

    public boolean deleteFinancialGoal(long goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_FINANCIAL_GOALS, COLUMN_GOAL_ID + " = ?", new String[]{String.valueOf(goalId)});
        return rowsAffected > 0;
    }

    public static class FinancialGoalData {
        public final long id;
        public final String name;
        public final float budget;
        public FinancialGoalData(long id, String name, float budget) {
            this.id = id;
            this.name = name;
            this.budget = budget;
        }
    }
    
    // --- Métodos para Reminders ---
    public long addReminder(String title, String content, long timeMillis, boolean isPeriodic, String periodicity, long originalTimeMillis) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REMINDER_TITLE, title);
        values.put(COLUMN_REMINDER_CONTENT, content);
        values.put(COLUMN_REMINDER_TIME_MILLIS, timeMillis);
        values.put(COLUMN_REMINDER_IS_PERIODIC, isPeriodic ? 1 : 0);
        values.put(COLUMN_REMINDER_PERIODICITY, periodicity);
        values.put(COLUMN_REMINDER_ORIGINAL_TIME_MILLIS, originalTimeMillis);
        return db.insert(TABLE_REMINDERS, null, values);
    }

    public ReminderData getReminder(long reminderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        ReminderData reminderData = null;
        try {
            cursor = db.query(TABLE_REMINDERS, null, COLUMN_REMINDER_ID + " = ?", new String[]{String.valueOf(reminderId)}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                reminderData = new ReminderData(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TITLE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_CONTENT)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TIME_MILLIS)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_IS_PERIODIC)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_PERIODICITY)),
                        cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ORIGINAL_TIME_MILLIS))
                );
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return reminderData;
    }

    public List<ReminderData> getAllReminders() {
        List<ReminderData> remindersList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_REMINDERS, null, null, null, null, null, COLUMN_REMINDER_TIME_MILLIS + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    remindersList.add(new ReminderData(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TITLE)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_CONTENT)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_TIME_MILLIS)),
                            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_IS_PERIODIC)) == 1,
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_PERIODICITY)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_REMINDER_ORIGINAL_TIME_MILLIS))
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return remindersList;
    }

    public boolean updateReminderTime(long reminderId, long newTimeMillis, long newOriginalTimeMillis) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REMINDER_TIME_MILLIS, newTimeMillis);
        values.put(COLUMN_REMINDER_ORIGINAL_TIME_MILLIS, newOriginalTimeMillis);
        int rowsAffected = db.update(TABLE_REMINDERS, values, COLUMN_REMINDER_ID + " = ?", new String[]{String.valueOf(reminderId)});
        return rowsAffected > 0;
    }

    public boolean deleteReminder(long reminderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_REMINDERS, COLUMN_REMINDER_ID + " = ?", new String[]{String.valueOf(reminderId)});
        return rowsAffected > 0;
    }

    public static class ReminderData {
        public final long id;
        public final String title;
        public final String content;
        public final long timeMillis;
        public final boolean isPeriodic;
        public final String periodicity;
        public final long originalTimeMillis;
        public ReminderData(long id, String title, String content, long timeMillis, boolean isPeriodic, String periodicity, long originalTimeMillis) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.timeMillis = timeMillis;
            this.isPeriodic = isPeriodic;
            this.periodicity = periodicity;
            this.originalTimeMillis = originalTimeMillis;
        }
    }

    // --- Métodos para Transactions ---
    public boolean addTransaction(String description, float amount, String type, long dateMillis, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSACTION_DESCRIPTION, description);
        values.put(COLUMN_TRANSACTION_AMOUNT, amount);
        values.put(COLUMN_TRANSACTION_TYPE, type);
        values.put(COLUMN_TRANSACTION_DATE_MILLIS, dateMillis);
        values.put(COLUMN_TRANSACTION_CATEGORY, category);
        long newRowId = db.insert(TABLE_TRANSACTIONS, null, values);

        // Enviar datos al webhook de n8n si la transacción se guardó exitosamente
        if (newRowId != -1) {
            sendTransactionToWebhook(description, amount, type, dateMillis, category);
        }

        return newRowId != -1;
    }

    private void sendTransactionToWebhook(String description, float amount, String type, long dateMillis, String category) {
        new Thread(() -> {
            try {
                // Generar ID autoincremental único
                synchronized (DatabaseHelper.class) {
                    transactionCounter++;
                }
                long uniqueId = System.currentTimeMillis() + transactionCounter;

                // Formatear la fecha en formato yyyy-MM-dd
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(dateMillis);
                String fecha = sdf.format(cal.getTime());

                // Determinar el tipo de transacción
                String tipoTexto = type.equals("income") ? "Ingreso" : "Egreso";

                // Formatear el monto como string sin decimales innecesarios
                String montoStr = String.format(Locale.US, "%.0f", amount);

                // Crear conexión HTTP
                java.net.URL url = new java.net.URL("https://primary-production-aa47.up.railway.app/webhook/OniriaGastosIngresos");
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setDoOutput(true);

                // Crear JSON estructurado con los datos incluyendo ID
                String jsonInputString = "{" +
                        "\"ID\": \"" + uniqueId + "\"," +
                        "\"Fecha\": \"" + fecha + "\"," +
                        "\"Descripción\": \"" + description + "\"," +
                        "\"Tipo\": \"" + tipoTexto + "\"," +
                        "\"Monto\": \"" + montoStr + "\"," +
                        "\"Categoría\": \"" + category + "\"" +
                        "}";

                Log.d(TAG, "Enviando JSON al webhook: " + jsonInputString);

                // Enviar datos
                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Obtener respuesta
                int responseCode = conn.getResponseCode();
                Log.d(TAG, "Webhook response code: " + responseCode);

                if (responseCode == java.net.HttpURLConnection.HTTP_OK) {
                    Log.d(TAG, "Transacción enviada exitosamente al webhook");
                } else {
                    Log.w(TAG, "Webhook respondió con código: " + responseCode);
                }

                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "Error al enviar datos al webhook: " + e.getMessage(), e);
            }
        }).start();
    }

    public List<TransactionData> getAllTransactions() {
        List<TransactionData> transactionsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_TRANSACTIONS, null, null, null, null, null, COLUMN_TRANSACTION_DATE_MILLIS + " DESC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    transactionsList.add(new TransactionData(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DESCRIPTION)),
                            cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE_MILLIS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY))
                    ));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) cursor.close();
        }
        return transactionsList;
    }

    public List<TransactionData> getCurrentMonthTransactions() {
        List<TransactionData> transactionsList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        Calendar calendar = Calendar.getInstance();
        // Establecer al primer día del mes actual
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTimeMillis = calendar.getTimeInMillis();

        // Establecer al primer día del siguiente mes
        calendar.add(Calendar.MONTH, 1);
        long endTimeMillis = calendar.getTimeInMillis();

        Log.d(TAG, "getCurrentMonthTransactions: Rango de fechas: " + 
                     formatMillisForLog(startTimeMillis) + " a " + formatMillisForLog(endTimeMillis));

        try {
            String[] columns = {
                COLUMN_TRANSACTION_ID,
                COLUMN_TRANSACTION_DESCRIPTION,
                COLUMN_TRANSACTION_AMOUNT,
                COLUMN_TRANSACTION_TYPE,
                COLUMN_TRANSACTION_DATE_MILLIS,
                COLUMN_TRANSACTION_CATEGORY
            };
            String selection = COLUMN_TRANSACTION_DATE_MILLIS + " >= ? AND " + COLUMN_TRANSACTION_DATE_MILLIS + " < ?";
            String[] selectionArgs = {String.valueOf(startTimeMillis), String.valueOf(endTimeMillis)};
            String orderBy = COLUMN_TRANSACTION_DATE_MILLIS + " DESC";

            cursor = db.query(TABLE_TRANSACTIONS, columns, selection, selectionArgs, null, null, orderBy);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    transactionsList.add(new TransactionData(
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DESCRIPTION)),
                            cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_AMOUNT)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_TYPE)),
                            cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DATE_MILLIS)),
                            cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY))
                    ));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener transacciones del mes actual: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        Log.d(TAG, "getCurrentMonthTransactions: Total de transacciones encontradas: " + transactionsList.size());
        return transactionsList;
    }

    public static class TransactionData {
        public final long id;
        public final String description;
        public final float amount;
        public final String type;
        public final long dateMillis;
        public final String category;
        public TransactionData(long id, String description, float amount, String type, long dateMillis, String category) {
            this.id = id;
            this.description = description;
            this.amount = amount;
            this.type = type;
            this.dateMillis = dateMillis;
            this.category = category;
        }
    }

    // --- NUEVOS MÉTODOS PARA GRÁFICOS ---
    private String formatMillisForLog(long millis) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return sdf.format(cal.getTime());
    }

    public List<DataEntry> getMonthlyExpensesByCategory(int year, int month) {
        Log.d(TAG, "getMonthlyExpensesByCategory: Solicitado para Año: " + year + ", Mes: " + (month + 1)); // month es 0-indexed
        List<DataEntry> dataEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTimeMillis = calendar.getTimeInMillis();
        Log.d(TAG, "getMonthlyExpensesByCategory: startTimeMillis: " + startTimeMillis + " (" + formatMillisForLog(startTimeMillis) + ")");

        calendar.add(Calendar.MONTH, 1);
        long endTimeMillis = calendar.getTimeInMillis();
        Log.d(TAG, "getMonthlyExpensesByCategory: endTimeMillis: " + endTimeMillis + " (" + formatMillisForLog(endTimeMillis) + ")");

        String query = "SELECT " + COLUMN_TRANSACTION_CATEGORY + ", SUM(" + COLUMN_TRANSACTION_AMOUNT + ") as total_amount " +
                       "FROM " + TABLE_TRANSACTIONS + " " +
                       "WHERE " + COLUMN_TRANSACTION_TYPE + " = ? AND " +
                       COLUMN_TRANSACTION_DATE_MILLIS + " >= ? AND " +
                       COLUMN_TRANSACTION_DATE_MILLIS + " < ? " +
                       "GROUP BY " + COLUMN_TRANSACTION_CATEGORY;
        Log.d(TAG, "getMonthlyExpensesByCategory: Query: " + query);
        
        try {
            cursor = db.rawQuery(query, new String[]{"expense", String.valueOf(startTimeMillis), String.valueOf(endTimeMillis)});
            Log.d(TAG, "getMonthlyExpensesByCategory: Cursor count: " + (cursor != null ? cursor.getCount() : "null cursor"));
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY));
                    float totalAmount = cursor.getFloat(cursor.getColumnIndexOrThrow("total_amount"));
                    if (category == null || category.trim().isEmpty()) {
                        category = "Sin Categoría";
                    }
                    dataEntries.add(new ValueDataEntry(category, totalAmount));
                    Log.d(TAG, "getMonthlyExpensesByCategory: Añadido DataEntry - Categoría: " + category + ", Monto: " + totalAmount);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener gastos mensuales por categoría: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        Log.d(TAG, "getMonthlyExpensesByCategory: Total DataEntries generadas: " + dataEntries.size());
        return dataEntries;
    }

    public List<DataEntry> getMonthlyIncomeByDescription(int year, int month) {
        Log.d(TAG, "getMonthlyIncomeByDescription: Solicitado para Año: " + year + ", Mes: " + (month + 1)); // month es 0-indexed
        List<DataEntry> dataEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTimeMillis = calendar.getTimeInMillis();
        Log.d(TAG, "getMonthlyIncomeByDescription: startTimeMillis: " + startTimeMillis + " (" + formatMillisForLog(startTimeMillis) + ")");

        calendar.add(Calendar.MONTH, 1);
        long endTimeMillis = calendar.getTimeInMillis();
        Log.d(TAG, "getMonthlyIncomeByDescription: endTimeMillis: " + endTimeMillis + " (" + formatMillisForLog(endTimeMillis) + ")");

        String query = "SELECT " + COLUMN_TRANSACTION_DESCRIPTION + ", SUM(" + COLUMN_TRANSACTION_AMOUNT + ") as total_amount " +
                       "FROM " + TABLE_TRANSACTIONS + " " +
                       "WHERE " + COLUMN_TRANSACTION_TYPE + " = ? AND " +
                       COLUMN_TRANSACTION_DATE_MILLIS + " >= ? AND " +
                       COLUMN_TRANSACTION_DATE_MILLIS + " < ? " +
                       "GROUP BY " + COLUMN_TRANSACTION_DESCRIPTION;
        Log.d(TAG, "getMonthlyIncomeByDescription: Query: " + query);

        try {
            cursor = db.rawQuery(query, new String[]{"income", String.valueOf(startTimeMillis), String.valueOf(endTimeMillis)});
            Log.d(TAG, "getMonthlyIncomeByDescription: Cursor count: " + (cursor != null ? cursor.getCount() : "null cursor"));
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_DESCRIPTION));
                    float totalAmount = cursor.getFloat(cursor.getColumnIndexOrThrow("total_amount"));
                    dataEntries.add(new ValueDataEntry(description, totalAmount));
                    Log.d(TAG, "getMonthlyIncomeByDescription: Añadido DataEntry - Descripción: " + description + ", Monto: " + totalAmount);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener ingresos mensuales por descripción: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        Log.d(TAG, "getMonthlyIncomeByDescription: Total DataEntries generadas: " + dataEntries.size());
        return dataEntries;
    }

    public List<DataEntry> getMonthlyIncomeByCategory(int year, int month) {
        Log.d(TAG, "getMonthlyIncomeByCategory: Solicitado para Año: " + year + ", Mes: " + (month + 1)); // month es 0-indexed
        List<DataEntry> dataEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1, 0, 0, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startTimeMillis = calendar.getTimeInMillis();
        Log.d(TAG, "getMonthlyIncomeByCategory: startTimeMillis: " + startTimeMillis + " (" + formatMillisForLog(startTimeMillis) + ")");

        calendar.add(Calendar.MONTH, 1);
        long endTimeMillis = calendar.getTimeInMillis();
        Log.d(TAG, "getMonthlyIncomeByCategory: endTimeMillis: " + endTimeMillis + " (" + formatMillisForLog(endTimeMillis) + ")");

        String query = "SELECT " + COLUMN_TRANSACTION_CATEGORY + ", SUM(" + COLUMN_TRANSACTION_AMOUNT + ") as total_amount " +
                       "FROM " + TABLE_TRANSACTIONS + " " +
                       "WHERE " + COLUMN_TRANSACTION_TYPE + " = ? AND " +
                       COLUMN_TRANSACTION_DATE_MILLIS + " >= ? AND " +
                       COLUMN_TRANSACTION_DATE_MILLIS + " < ? " +
                       "GROUP BY " + COLUMN_TRANSACTION_CATEGORY;
        Log.d(TAG, "getMonthlyIncomeByCategory: Query: " + query);

        try {
            cursor = db.rawQuery(query, new String[]{"income", String.valueOf(startTimeMillis), String.valueOf(endTimeMillis)});
            Log.d(TAG, "getMonthlyIncomeByCategory: Cursor count: " + (cursor != null ? cursor.getCount() : "null cursor"));
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSACTION_CATEGORY));
                    float totalAmount = cursor.getFloat(cursor.getColumnIndexOrThrow("total_amount"));
                    if (category == null || category.trim().isEmpty()) {
                        category = "Sin Categoría";
                    }
                    dataEntries.add(new ValueDataEntry(category, totalAmount));
                    Log.d(TAG, "getMonthlyIncomeByCategory: Añadido DataEntry - Categoría: " + category + ", Monto: " + totalAmount);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener ingresos mensuales por categoría: " + e.getMessage(), e);
        } finally {
            if (cursor != null) cursor.close();
        }
        Log.d(TAG, "getMonthlyIncomeByCategory: Total DataEntries generadas: " + dataEntries.size());
        return dataEntries;
    }

    public List<DataEntry> getFinancialGoalsAsDataEntries() {
        // ... (el método de metas no necesita cambios de log por ahora, ya que funciona)
        List<DataEntry> dataEntries = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_FINANCIAL_GOALS, 
                              new String[]{COLUMN_GOAL_NAME, COLUMN_GOAL_BUDGET}, 
                              null, null, null, null, COLUMN_GOAL_NAME + " ASC");
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GOAL_NAME));
                    float budget = cursor.getFloat(cursor.getColumnIndexOrThrow(COLUMN_GOAL_BUDGET));
                    dataEntries.add(new ValueDataEntry(name, budget));
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error al obtener metas financieras como DataEntry: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
        Log.d(TAG, "Metas financieras obtenidas: " + dataEntries.size() + " entradas.");
        return dataEntries;
    }
}
