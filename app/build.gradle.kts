import java.util.Properties
import java.io.FileInputStream
plugins {
    // 使用 ID 直接应用插件，替代 alias(libs.plugins.androidApplication)
    id("com.android.application")
}

android {
    namespace = "com.example.myllmapp"
    compileSdk = 34 // 或者你使用的 SDK 版本

    defaultConfig {
        applicationId = "com.example.myllmapp"
        minSdk = 24 // 推荐的最低 SDK 版本
        targetSdk = 34 // 或者你使用的 SDK 版本
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
            // 对于生产环境，可以使用空字符串
            buildConfigField("String", "DASHSCOPE_API_KEY", "\"\"")
        }
        debug {
            // 从local.properties读取API Key
            val properties = Properties()
            val localPropertiesFile = project.rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                properties.load(FileInputStream(localPropertiesFile))
            }
            val apiKey = properties.getProperty("dashscope.api.key") ?: ""
            buildConfigField("String", "DASHSCOPE_API_KEY", "\"$apiKey\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    // 确保生成BuildConfig
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
    
    packagingOptions {
        exclude("/META-INF/DEPENDENCIES")
        exclude("/META-INF/LICENSE")
        exclude("/META-INF/LICENSE.txt")
        exclude("/META-INF/license.txt")
        exclude("/META-INF/NOTICE")
        exclude("/META-INF/NOTICE.txt")
        exclude("/META-INF/notice.txt")
        exclude("/META-INF/ASL2.0")
        exclude("/META-INF/*.kotlin_module")
    }
}

dependencies {
    // 核心库
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // RecyclerView for lists
    implementation("androidx.recyclerview:recyclerview:1.3.2") // 使用最新稳定版

    // Room Persistence Library for Database
    val room_version = "2.6.1" // 使用最新稳定版
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-rxjava3:$room_version")
    implementation("androidx.room:room-guava:$room_version")
    testImplementation("androidx.room:room-testing:$room_version")
    implementation("androidx.room:room-paging:$room_version")

    // Retrofit & OkHttp for network requests
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // 测试库
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.openai:openai-java:0.31.0") {
        // 排除问题依赖
        exclude(group = "com.google.errorprone", module = "error_prone_annotations")
    }
    
    // 添加兼容版本的error_prone_annotations
    implementation("com.google.errorprone:error_prone_annotations:2.15.0")
}