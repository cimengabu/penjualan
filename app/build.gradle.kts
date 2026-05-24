plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    kotlin("plugin.parcelize")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
}

android {
    compileSdk = 35
    namespace = "com.example.penjualan"
    defaultConfig {
        applicationId = "com.example.penjualan"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.12"
    }
    packaging {
        resources {
            excludes += setOf("META-INF/AL2.0", "META-INF/LGPL2.1")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core AndroidX
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.2")
    implementation("androidx.activity:activity-ktx:1.9.2")

    // AppCompat (wajib untuk AppCompatActivity, AlertDialog, SearchView)
    implementation("androidx.appcompat:appcompat:1.7.0")

    // Material Design Components (wajib untuk MaterialCardView, FAB, TextInputLayout, Chip, dll)
    implementation("com.google.android.material:material:1.12.0")

    // ConstraintLayout (wajib untuk layout XML)
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // CardView (wajib untuk activity_card.xml)
    implementation("androidx.cardview:cardview:1.0.0")

    // RecyclerView (wajib untuk daftar produk & kategori)
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Hilt for DI
    implementation("com.google.dagger:hilt-android:2.52")
    kapt("com.google.dagger:hilt-compiler:2.52")

    // Firebase (BOM ensures compatible versions)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-database-ktx")

    // Room (local cache)
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

    // Image loading
    implementation("io.coil-kt:coil:2.7.0")

    // Print framework (AndroidX)
    implementation("androidx.print:print:1.0.0")
}

kapt {
    correctErrorTypes = true
}