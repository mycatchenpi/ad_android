plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.spotify"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.example.spotify"
        minSdk = 29
        targetSdk = 33
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // recyclerview
    implementation ("androidx.recyclerview:recyclerview:1.3.1")
    // com.squareup.okhttp3/okhttp
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    // retrofit2
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    // gson
    implementation ("com.google.code.gson:gson:2.10.1")

    // spotify
    //implementation ("com.spotify.android:auth:2.1.0") // Maven dependency
    implementation("com.spotify.android:auth:1.2.5")
    runtimeOnly ("androidx.browser:browser:1.5.0")
    runtimeOnly ("androidx.appcompat:appcompat:1.6.1")

    // glide for image download
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
}