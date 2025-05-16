plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.haui.notetakingapp"
    compileSdk = 35

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.haui.notetakingapp"
        minSdk = 25
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"your-cloud-name\"")
        buildConfigField("String", "CLOUDINARY_API_KEY", "\"your-api-key\"")
        buildConfigField("String", "CLOUDINARY_API_SECRET", "\"your-api-secret\"")

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
    // AndroidX Core Libraries
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Cloudinary for media uploads
    implementation("com.cloudinary:cloudinary-android:2.3.1")

    // Circle ImageView for profile picture
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Room components
    implementation("androidx.room:room-runtime:2.7.1")
    implementation(libs.lifecycle.process)
    annotationProcessor("androidx.room:room-compiler:2.7.1")

    // LiveData and ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.0")
    // Add lifecycle process for ProcessLifecycleOwner
    implementation("androidx.lifecycle:lifecycle-process:2.8.0")

    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.14.2")
    annotationProcessor("com.github.bumptech.glide:compiler:4.14.2")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Paint
    implementation("com.github.gcacace:signature-pad:1.2.0")
    implementation("com.github.kristiyanP:colorpicker:v1.1.10")
    implementation("com.google.android.material:material:1.1.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
