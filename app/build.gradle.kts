import java.util.Properties

val signingPropertiesFile = rootProject.file("keystore.properties")
val signingProperties = Properties()
if (signingPropertiesFile.exists()) {
    signingPropertiesFile.reader(Charsets.UTF_8).use(signingProperties::load)
}

plugins {
    id("com.android.application")
}

android {
    namespace = "com.pickup.tool"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pickup.tool"
        minSdk = 23
        targetSdk = 35
        // 包名与旧工程不同，属于全新应用，版本号重新起算。
        versionCode = 210
        versionName = "2.1.0"
    }

    if (signingPropertiesFile.exists()) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(signingProperties["storeFile"] as String)
                storePassword = signingProperties["storePassword"] as String
                keyAlias = signingProperties["keyAlias"] as String
                keyPassword = signingProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (signingPropertiesFile.exists()) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
