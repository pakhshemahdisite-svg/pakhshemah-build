import groovy.json.JsonSlurper

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val ciKeystorePath = System.getenv("PM_ANDROID_KEYSTORE_PATH")
val ciKeystorePassword = System.getenv("PM_ANDROID_KEYSTORE_PASSWORD")
val ciKeyAlias = System.getenv("PM_ANDROID_KEY_ALIAS")
val ciKeyPassword = System.getenv("PM_ANDROID_KEY_PASSWORD")

fun quotedBuildConfig(value: String): String =
    "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""


@Suppress("UNCHECKED_CAST")
val whiteLabelClients: List<Map<String, Any?>> = run {
    val clientsDir = rootProject.projectDir.parentFile.resolve("clients")
    clientsDir.listFiles()
        ?.filter { it.isDirectory && !it.name.startsWith("_") }
        ?.mapNotNull { dir ->
            val configFile = dir.resolve("client.json")
            if (!configFile.isFile) null
            else JsonSlurper().parseText(configFile.readText()) as Map<String, Any?>
        }
        ?.sortedBy { it["clientId"].toString() }
        ?: emptyList()
}

fun Map<String, Any?>.string(key: String): String =
    requireNotNull(this[key]) { "Missing client config key: $key" }.toString()

@Suppress("UNCHECKED_CAST")
fun Map<String, Any?>.map(key: String): Map<String, Any?> =
    requireNotNull(this[key]) { "Missing client config object: $key" } as Map<String, Any?>


fun Map<String, Any?>.boolean(key: String, default: Boolean = false): Boolean =
    when (val value = this[key]) {
        is Boolean -> value
        is String -> value.equals("true", ignoreCase = true)
        is Number -> value.toInt() != 0
        null -> default
        else -> default
    }

android {
    namespace = "com.pakhshmahdi.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.iamir.commerce"
        minSdk = 26
        targetSdk = 36
        versionCode = 41
        versionName = "0.10.6"

        vectorDrawables.useSupportLibrary = true
    }

    flavorDimensions += "client"

    productFlavors {
        whiteLabelClients.forEach { client ->
            val flavorName = client.string("androidFlavor")
            val branding = client.map("branding")
            val features = client.map("features")

            create(flavorName) {
                dimension = "client"
                applicationId = client.string("androidApplicationId")

                buildConfigField("String", "CLIENT_ID", quotedBuildConfig(client.string("clientId")))
                buildConfigField("String", "APP_NAME", quotedBuildConfig(client.string("appName")))
                buildConfigField("String", "APP_SUBTITLE", quotedBuildConfig(client.string("appSubtitle")))
                buildConfigField("String", "API_BASE_URL", quotedBuildConfig(client.string("apiBaseUrl")))
                buildConfigField("String", "STORE_API_URL", quotedBuildConfig(client.string("storeApiUrl")))
                buildConfigField("String", "PWS_AJAX_URL", quotedBuildConfig(client.string("pwsAjaxUrl")))
                buildConfigField("String", "SUPPORT_URL", quotedBuildConfig(client.string("supportUrl")))
                buildConfigField("String", "CONTACT_URL", quotedBuildConfig(client.string("contactUrl")))

                buildConfigField("String", "BRAND_PRIMARY", quotedBuildConfig(branding.string("primary")))
                buildConfigField("String", "BRAND_PRIMARY_DARK", quotedBuildConfig(branding.string("primaryDark")))
                buildConfigField("String", "BRAND_ACCENT", quotedBuildConfig(branding.string("accent")))
                buildConfigField("String", "BRAND_DARK_BACKGROUND", quotedBuildConfig(branding.string("darkBackground")))

                buildConfigField("boolean", "FEATURE_WISHLIST", features.boolean("wishlist", true).toString())
                buildConfigField("boolean", "FEATURE_OTP", features.boolean("otp", true).toString())
                buildConfigField("boolean", "FEATURE_COUPONS", features.boolean("coupons", true).toString())
                buildConfigField("boolean", "FEATURE_NOTIFICATIONS", features.boolean("notifications", true).toString())
                buildConfigField("boolean", "FEATURE_SUPPORT", features.boolean("support", true).toString())
                buildConfigField("boolean", "FEATURE_PWS_SHIPPING", features.boolean("pwsShipping", true).toString())
                buildConfigField("boolean", "FEATURE_ONLINE_PAYMENT", features.boolean("onlinePayment", true).toString())

                resValue("string", "app_name", client.string("appName"))
            }
        }
    }

    val hasExternalSigning =
        !ciKeystorePath.isNullOrBlank() &&
        !ciKeystorePassword.isNullOrBlank() &&
        !ciKeyAlias.isNullOrBlank() &&
        !ciKeyPassword.isNullOrBlank()

    signingConfigs {
        getByName("debug") {
            if (hasExternalSigning) {
                storeFile = file(requireNotNull(ciKeystorePath))
                storePassword = ciKeystorePassword
                keyAlias = ciKeyAlias
                keyPassword = ciKeyPassword
            }
        }

        create("clientRelease") {
            if (hasExternalSigning) {
                storeFile = file(requireNotNull(ciKeystorePath))
                storePassword = ciKeystorePassword
                keyAlias = ciKeyAlias
                keyPassword = ciKeyPassword
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            isShrinkResources = false
            if (hasExternalSigning) {
                signingConfig = signingConfigs.getByName("clientRelease")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.activity:activity-compose:1.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
