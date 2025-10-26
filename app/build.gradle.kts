import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

// Leer propiedades del archivo local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

android {
    namespace = "com.example.oniria"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.oniria"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API Key de Groq almacenada de forma segura desde local.properties
        val groqApiKey = localProperties.getProperty("GROQ_API_KEY") ?: ""
        buildConfigField("String", "GROQ_API_KEY", "\"$groqApiKey\"")

        // n8n webhook URL para notificar subida de recibos
        val n8nWebhookUrl = localProperties.getProperty("N8N_WEBHOOK_URL") ?: "https://primary-production-aa47.up.railway.app/webhook/OniriaGastosIngresos"
        buildConfigField("String", "N8N_WEBHOOK_URL", "\"$n8nWebhookUrl\"")
    }

    buildFeatures {
        buildConfig = true
    }

    signingConfigs {
        create("release") {
            // Estas propiedades se pueden leer desde gradle.properties o variables de entorno
            storeFile = file("keystore/oniria-release-key.jks")
            storePassword = "oniria2025"
            keyAlias = "oniria-key"
            keyPassword = "oniria2025"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/license.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/notice.txt",
                "META-INF/ASL2.0",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("com.github.AnyChart:AnyChart-Android:1.1.2")

    // MPAndroidChart para gráficos nativos
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Usar una versión compatible de Guava (se mantiene por si es necesaria)
    implementation("com.google.guava:guava:31.1-android")

    // Dependencias para HTTP requests y JSON para la API de IA
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.json:json:20240303")

    // Firebase Storage para subir imágenes
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-auth")

    // FileProvider para captura de fotos
    implementation("androidx.core:core:1.12.0")

    // Dependencia para leer datos EXIF de las imágenes
    implementation("androidx.exifinterface:exifinterface:1.3.7")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
