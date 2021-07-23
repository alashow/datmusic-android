package tm.alashow.buildSrc

object Deps {
    object Gradle {
        const val dexCount = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:2.0.0"
        const val playPublisher = "com.github.triplet.gradle:play-publisher:3.5.0-agp7.0"
        const val googleServices = "com.google.gms:google-services:4.3.8"
    }

    object Kotlin {
        const val version = "1.5.10"

        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"
        const val gradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val serialization = "org.jetbrains.kotlin:kotlin-serialization:$version"
        const val serializationRuntime = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1"

        const val coroutinesVersion = "1.5.0"
        const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"
    }

    object Android {
        private const val gradleVersion = "7.1.0-alpha02"

        const val gradle = "com.android.tools.build:gradle:$gradleVersion"

        const val multiDex = "androidx.multidex:multidex:2.0.1"

        const val activityVersion = "1.3.0-beta02"
        const val activityKtx = "androidx.activity:activity-ktx:$activityVersion"
        const val fragmentKtx = "androidx.fragment:fragment-ktx:1.4.0-alpha02"

        private const val navigationVersion = "2.4.0-alpha02"
        const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:$navigationVersion"
        const val navigationUi = "androidx.navigation:navigation-ui-ktx:$navigationVersion"
        const val navigationSafeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion"
        const val navigationHilt = "androidx.hilt:hilt-navigation-compose:1.0.0-alpha02"

        const val dataStore = "androidx.datastore:datastore-preferences:1.0.0-beta02"

        const val documentFile = "androidx.documentfile:documentfile:1.0.1"

        object Compose {
            const val version = "1.0.0-rc01"

            const val ui = "androidx.compose.ui:ui:$version"
            const val uiTooling = "androidx.compose.ui:ui-tooling:$version"
            const val foundation = "androidx.compose.foundation:foundation:$version"
            const val materialDesign = "androidx.compose.material:material:$version"
            const val materialDesignIcons = "androidx.compose.material:material-icons-core:$version"
            const val materialDesignIconsExtended = "androidx.compose.material:material-icons-extended:$version"
            const val constraintLayout = "androidx.constraintlayout:constraintlayout-compose:1.0.0-alpha08"
            const val liveData = "androidx.compose.runtime:runtime-livedata:$version"
            const val activity = "androidx.activity:activity-compose:$activityVersion"
            const val viewModels = "androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha06"
            const val paging = "androidx.paging:paging-compose:1.0.0-alpha10"

            private const val lottieVersion = "1.0.0-rc01-1-SNAPSHOT"
            const val lottie = "com.airbnb.android:lottie-compose:$lottieVersion"
        }

        object Accompanist {
            private const val version = "0.12.0"

            const val coil = "com.google.accompanist:accompanist-coil:$version"
            const val insets = "com.google.accompanist:accompanist-insets:$version"
            const val insetsUi = "com.google.accompanist:accompanist-insets-ui:$version"
            const val permissions = "com.google.accompanist:accompanist-permissions:$version"
            const val placeholder = "com.google.accompanist:accompanist-placeholder-material:$version"
            const val swiperefresh = "com.google.accompanist:accompanist-swiperefresh:$version"
            const val systemUiController = "com.google.accompanist:accompanist-systemuicontroller:$version"
        }

        object Lifecycle {
            private const val version = "2.4.0-alpha01"
            private const val vmSavedStateVersion = "2.4.0-alpha01"

            const val runtime = "androidx.lifecycle:lifecycle-runtime:$version"
            const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            const val compiler = "androidx.lifecycle:lifecycle-compiler:$version"
            const val vmKotlin = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val vmSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:$vmSavedStateVersion"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:2.2.0"
        }

        object Room {
            private const val roomVersion = "2.4.0-alpha02"

            const val compiler = "androidx.room:room-compiler:$roomVersion"
            const val runtime = "androidx.room:room-runtime:$roomVersion"
            const val ktx = "androidx.room:room-ktx:$roomVersion"
        }

        object Paging {
            private const val version = "3.0.0"

            const val common = "androidx.paging:paging-common-ktx:$version"
            const val runtime = "androidx.paging:paging-runtime-ktx:$version"
        }
    }

    object Utils {
        const val localizationActivity = "com.github.akexorcist:Localization:3226dc6d5d"
        const val timber = "com.jakewharton.timber:timber:4.7.1"
        const val threeTenAbp = "com.jakewharton.threetenabp:threetenabp:1.3.1"
        const val proguardSnippets = "com.github.yongjhih.android-proguards:android-proguards-all:-SNAPSHOT"

        const val junit = "junit:junit:4.13.2"
        const val threeTen = "org.threeten:threetenbp:1.5.1"

        const val coilVersion = "1.2.1"
        const val coil = "io.coil-kt:coil:$coilVersion"
        const val store = "com.dropbox.mobile.store:store4:4.0.2-KT15"

        const val fetch = "androidx.tonyodev.fetch2:xfetch2:3.1.6"
        const val fetchOkhttp = "androidx.tonyodev.fetch2okhttp:xfetch2okhttp:3.1.6"
    }

    object OkHttp {
        private const val version = "5.0.0-alpha.2"

        const val okhttp = "com.squareup.okhttp3:okhttp:$version"
        const val logger = "com.squareup.okhttp3:logging-interceptor:$version"
    }

    object Retrofit {
        private const val version = "2.9.0"
        private const val retroAuthVersion = "3.1.0"

        const val retrofit = "com.squareup.retrofit2:retrofit:$version"
        const val rxjavaAdapter = "com.squareup.retrofit2:adapter-rxjava2:$version"
        const val retroAuth = "com.andretietz.retroauth:retroauth-android:$retroAuthVersion"
        const val kotlinSerializerConverter = "com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0"
    }

    object Dagger {
        private const val version = "2.36"

        const val dagger = "com.google.dagger:dagger:$version"
        const val compiler = "com.google.dagger:dagger-compiler:$version"

        const val hiltVersion = "2.36"
        const val hilt = "com.google.dagger:hilt-android:$hiltVersion"
        const val hiltCompiler = "com.google.dagger:hilt-compiler:$hiltVersion"
        const val hiltGradle = "com.google.dagger:hilt-android-gradle-plugin:$hiltVersion"
    }

    object LeakCanary {
        private const val version = "2.7"

        const val leakCanary = "com.squareup.leakcanary:leakcanary-android:$version"
    }

    object Firebase {

        const val bom = "com.google.firebase:firebase-bom:28.2.1"
        const val messaging = "com.google.firebase:firebase-messaging-ktx"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        const val crashlyticsGradle = "com.google.firebase:firebase-crashlytics-gradle:2.7.0"
    }
}
