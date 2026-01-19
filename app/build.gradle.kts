plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.taskmanagment"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.taskmanagment"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    packagingOptions {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt"
            )
        }
    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.jetbrains.kotlin") {
            when (requested.name) {
                "kotlin-stdlib-jdk7", "kotlin-stdlib-jdk8" -> {
                    useTarget("org.jetbrains.kotlin:kotlin-stdlib:${requested.version}")
                }
            }
        }
    }
    exclude(group = "com.google.guava", module = "listenablefuture")
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.activity:activity:1.8.0")
    implementation("androidx.recyclerview:recyclerview:1.3.1")
    implementation("androidx.cardview:cardview:1.0.0")

    //Support pour Fragments
    implementation("androidx.fragment:fragment:1.6.2")

    //Navigation Components (optionnel mais recommandé)
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")

    //Bibliothèque grapgique
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Xerces pour validation XML
    implementation("xerces:xercesImpl:2.12.2")
    implementation(libs.compiler)

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
