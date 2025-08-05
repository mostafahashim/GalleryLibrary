plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
}

android {
    namespace = "hashim.gallery"
    compileSdk = 36


    buildFeatures {
        dataBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "hashim.gallery"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-ktx:1.10.1")

    //glide
    implementation("jp.wasabeef:glide-transformations:4.3.0")
    // If you want to use the GPU Filters
    implementation("com.github.bumptech.glide:okhttp3-integration:4.16.0@aar")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    implementation("com.github.2coffees1team:GlideToVectorYou:v2.0.0")
    kapt ("com.github.bumptech.glide:compiler:4.16.0")

    implementation(project(":gallerylib"))
//    implementation ("com.github.mostafahashim:GalleryLibrary:1.1.6")
    implementation("com.github.2coffees1team:GlideToVectorYou:v2.0.0")
}