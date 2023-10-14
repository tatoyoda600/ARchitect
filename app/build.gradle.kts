plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.pfortbe22bgrupo2.architectapp"
    compileSdk = 34

    defaultConfig {
        configurations.all {
            resolutionStrategy {
                // emoji2 1.4.0 is imported by some module and requires SDK 34, which is not currently supported
                // This overrides the import with the 1.3.0 version
                force("androidx.emoji2:emoji2-views-helper:1.3.0")
                force("androidx.emoji2:emoji2:1.3.0")
            }
        }
        applicationId = "com.pfortbe22bgrupo2.architectapp"
        minSdk = 28
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {

//    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // ARCore and SceneView (Androidx version of Sceneform: https://github.com/SceneView/sceneview-android)
    implementation("io.github.sceneview:arsceneview:0.10.2")

    //Coroutines
    api("org.jetbrains.kotlin:kotlin-stdlib:")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")

    // AndroidX
    api("androidx.core:core-ktx:1.10.1")
    api("androidx.appcompat:appcompat:1.6.1")
    api ("androidx.fragment:fragment-ktx:1.6.1")

    // SceneView
//    implementation("io.github.sceneview:sceneview:1.0.12")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}