import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.0-1.0.12"
}

android {
    namespace = "com.MaeumSee"
    compileSdk = 35

    val properties = Properties()
    properties.load(FileInputStream(rootProject.file("local.properties")))


    defaultConfig {
        applicationId = "com.MaeumSee"
        minSdk = 26
        targetSdk = 34
        versionCode = 4
        versionName = "1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        buildConfigField("String", "GEMINI_API_KEY", properties.getProperty("GEMINI_API_KEY"))
    }


    signingConfigs {
        create("release") {
            val keystorePath = properties.getProperty("STORE_FILE")?.trim()

            // 🛠 디버깅: 로그로 경로 확인
            println("🔥 Keystore Path: '$keystorePath'")

            // 🚨 null 또는 비어 있는 값이면 오류 던지기
            if (keystorePath.isNullOrEmpty()) {
                throw GradleException("🚨 STORE_FILE 경로가 비어 있거나 null입니다! local.properties를 확인하세요.")
            }

            storeFile = File(keystorePath) // 👉 File() 객체로 감싸기
            storePassword = properties["STORE_PASSWORD"] as String
            keyAlias = properties["KEY_ALIAS"] as String
            keyPassword = properties["KEY_PASSWORD"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release") // 서명 설정 연결
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true // Data Binding 활성화
        buildConfig = true
    }
    configurations.all {
        resolutionStrategy {
            force("androidx.core:core-ktx:1.12.0")
            force("androidx.viewpager:viewpager:1.0.0") // ViewPager 강제 적용
        }
    }

}

dependencies {
    val room_version = "2.6.1"

    // Room
    implementation("androidx.room:room-runtime:$room_version")
    ksp("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    // Chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // AndroidX Core Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // Activity & Navigation
    implementation("androidx.activity:activity-compose:1.8.1")
    implementation("androidx.navigation:navigation-compose:2.7.5")

    // Reactive Streams & Guava
    implementation("com.google.guava:guava:31.0.1-android")
    implementation("org.reactivestreams:reactive-streams:1.0.4")

    // Compose
    implementation(platform("androidx.compose:compose-bom:2023.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // AI & Animation
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
    implementation("com.airbnb.android:lottie:6.1.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")

    // JSON
    implementation("com.google.code.gson:gson:2.10.1")

    // Calendar (변경된 부분)
    implementation("com.github.prolificinteractive:material-calendarview:1.4.3") {
        exclude(group = "com.android.support")  // 안드로이드 서포트 라이브러리 제외
    }

    // AndroidX 의존성 정리
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.compose.material3:material3:1.0.0-alpha02")
    // 중복되는 의존성 제거
    configurations.all {
        resolutionStrategy {
            force("androidx.core:core-ktx:1.12.0")
            exclude(group = "com.android.support")
        }
    }
}