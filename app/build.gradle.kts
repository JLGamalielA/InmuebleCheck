
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.googleGmsGoogleServices)
}

android {
    namespace = "com.example.inmueblecheck"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.inmueblecheck"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation("androidx.room:room-runtime:2.6.1") //Persistencia Offline (Room)
    annotationProcessor("androidx.room:room-compiler:2.6.1") // Se usa annotationProcessor en vez de kapt
    implementation("androidx.work:work-runtime:2.9.0") //Sincronización en Segundo Plano"
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-auth")        // Autenticación
    implementation("com.google.firebase:firebase-firestore")   // Base de Datos
    implementation("com.google.firebase:firebase-storage")     // Almacenamiento de archivos
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.3") // ViewModels
    implementation("androidx.lifecycle:lifecycle-runtime:2.8.3")
    implementation("androidx.navigation:navigation-fragment:2.7.7") // Navegación con Fragments
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("androidx.work:work-runtime:2.9.0") //Sincronización en Segundo Plano
    implementation("com.google.android.material:material:1.12.0")// UI
    implementation("com.google.android.gms:play-services-location:21.3.0")  //Servicios de Ubicación (GPS)
    implementation("com.github.bumptech.glide:glide:4.16.0")  //Carga de Imágenes desde URL
    implementation("com.google.guava:guava:31.0.1-android")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}