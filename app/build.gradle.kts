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
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    // 如果你使用 ViewBinding (推荐)
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // 核心库
    // implementation(libs.androidx.core.ktx) // 如果你混用 Kotlin 可以保留, 但需要确保 version catalog 或直接版本号可用
    implementation("androidx.core:core-ktx:1.12.0") // 直接指定版本示例
    implementation("androidx.appcompat:appcompat:1.6.1") // 直接指定版本示例
    implementation("com.google.android.material:material:1.11.0") // 直接指定版本示例
    implementation("androidx.activity:activity:1.8.2") // 直接指定版本示例
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // 直接指定版本示例


    // RecyclerView for lists
    implementation("androidx.recyclerview:recyclerview:1.3.2") // 使用最新稳定版

    // Room Persistence Library for Database
    val room_version = "2.6.1" // 使用最新稳定版
    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")
    // 可选 - RxJava3 支持 Room
    implementation("androidx.room:room-rxjava3:$room_version")
    // 可选 - Guava 支持 Room, 包括 Optional 和 ListenableFuture
    implementation("androidx.room:room-guava:$room_version")
    // 可选 - 测试帮助库
    testImplementation("androidx.room:room-testing:$room_version")
    // 可选 - Paging 3 Integration
    implementation("androidx.room:room-paging:$room_version")


    // 测试库
    // testImplementation(libs.junit) // 需要确保 version catalog 或直接版本号可用
    testImplementation("junit:junit:4.13.2") // 直接指定版本示例
    // androidTestImplementation(libs.androidx.junit) // 需要确保 version catalog 或直接版本号可用
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // 直接指定版本示例
    // androidTestImplementation(libs.androidx.espresso.core) // 需要确保 version catalog 或直接版本号可用
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // 直接指定版本示例
}
