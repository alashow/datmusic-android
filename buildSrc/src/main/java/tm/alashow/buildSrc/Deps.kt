package tm.alashow.buildSrc

object Deps {
    object Gradle {
        const val dexCount = "com.getkeepsafe.dexcount:dexcount-gradle-plugin:2.0.0"
        const val playPublisher = "com.github.triplet.gradle:play-publisher:3.7.0"
        const val googleServices = "com.google.gms:google-services:4.3.13"
    }

    object Kotlin {
        const val version = "1.7.10"

        const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk7:$version"
        const val gradle = "org.jetbrains.kotlin:kotlin-gradle-plugin:$version"
        const val serialization = "org.jetbrains.kotlin:kotlin-serialization:$version"
        const val serializationJson = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0"

        const val coroutinesVersion = "1.6.4"
        const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
        const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"
        const val coroutineTesting = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion"
    }

    object Android {
        private const val gradleVersion = "7.2.2"

        const val gradle = "com.android.tools.build:gradle:$gradleVersion"

        const val multiDex = "androidx.multidex:multidex:2.0.1"

        const val activityVersion = "1.6.0-rc02"
        const val activityKtx = "androidx.activity:activity-ktx:$activityVersion"

        private const val navigationVersion = "2.5.1"
        const val navigationFragment = "androidx.navigation:navigation-fragment-ktx:$navigationVersion"
        const val navigationUi = "androidx.navigation:navigation-ui-ktx:$navigationVersion"
        const val navigationSafeArgs = "androidx.navigation:navigation-safe-args-gradle-plugin:$navigationVersion"
        const val navigationCompose = "androidx.navigation:navigation-compose:$navigationVersion"
        const val navigationHiltCompose = "androidx.hilt:hilt-navigation-compose:1.0.0"

        const val dataStore = "androidx.datastore:datastore-preferences:1.0.0"

        const val documentFile = "androidx.documentfile:documentfile:1.1.0-alpha01"

        const val palette = "androidx.palette:palette-ktx:1.0.0"
        const val media = "androidx.media:media:1.6.0"

        const val archCoreTesting = "androidx.arch.core:core-testing:2.1.0"

        object Compose {
            const val version = "1.3.0-beta02"
            const val compilerVersion = "1.3.0"

            const val ui = "androidx.compose.ui:ui:$version"
            const val uiUtil = "androidx.compose.ui:ui-util:$version"
            const val uiTooling = "androidx.compose.ui:ui-tooling:$version"
            const val foundation = "androidx.compose.foundation:foundation:$version"
            const val material = "androidx.compose.material:material:$version"
            const val material3 = "androidx.compose.material3:material3:1.0.0-beta02"
            const val materialIcons = "androidx.compose.material:material-icons-core:$version"
            const val materialIconsExtended = "androidx.compose.material:material-icons-extended:$version"
            const val constraintLayout = "androidx.constraintlayout:constraintlayout-compose:1.1.0-alpha03"
            const val liveData = "androidx.compose.runtime:runtime-livedata:$version"
            const val activity = "androidx.activity:activity-compose:$activityVersion"
            const val paging = "androidx.paging:paging-compose:1.0.0-alpha16"

            const val uiTestJunit = "androidx.compose.ui:ui-test-junit4:$version"
            const val uiTestManifeset = "androidx.compose.ui:ui-test-manifest:$version"

            private const val lottieVersion = "5.2.0"
            const val lottie = "com.airbnb.android:lottie-compose:$lottieVersion"

            const val coil = "io.coil-kt:coil-compose:${Utils.coilVersion}"
            const val reorderable = "org.burnoutcrew.composereorderable:reorderable:0.9.2"
            const val swipe = "me.saket.swipe:swipe:1.0.0"
        }

        object Accompanist {
            private const val version = "0.26.3-beta"

            const val pager = "com.google.accompanist:accompanist-pager:$version"
            const val permissions = "com.google.accompanist:accompanist-permissions:$version"
            const val placeholder = "com.google.accompanist:accompanist-placeholder-material:$version"
            const val swiperefresh = "com.google.accompanist:accompanist-swiperefresh:$version"
            const val systemUiController = "com.google.accompanist:accompanist-systemuicontroller:$version"
            const val navigationMaterial = "com.google.accompanist:accompanist-navigation-material:$version"
            const val navigationAnimation = "com.google.accompanist:accompanist-navigation-animation:$version"
            const val flowlayout = "com.google.accompanist:accompanist-flowlayout:$version"
        }

        object Lifecycle {
            private const val version = "2.6.0-alpha02"

            const val runtime = "androidx.lifecycle:lifecycle-runtime:$version"
            const val runtimeKtx = "androidx.lifecycle:lifecycle-runtime-ktx:$version"
            const val compiler = "androidx.lifecycle:lifecycle-compiler:$version"
            const val vmKotlin = "androidx.lifecycle:lifecycle-viewmodel-ktx:$version"
            const val vmSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:$version"
            const val extensions = "androidx.lifecycle:lifecycle-extensions:2.2.0"
            const val composeViewModels = "androidx.lifecycle:lifecycle-viewmodel-compose:$version"
        }

        object Room {
            private const val version = "2.4.1"

            const val compiler = "androidx.room:room-compiler:$version"
            const val runtime = "androidx.room:room-runtime:$version"
            const val ktx = "androidx.room:room-ktx:$version"
            const val paging = "androidx.room:room-paging:$version"
            const val testing = "androidx.room:room-testing:$version"
        }

        object Paging {
            private const val version = "3.2.0-alpha02"

            const val common = "androidx.paging:paging-common-ktx:$version"
            const val runtime = "androidx.paging:paging-runtime-ktx:$version"
        }

        object Test {
            const val core = "androidx.test:core:1.5.0-alpha02"
            const val rules = "androidx.test:rules:1.4.1-alpha07"
            const val runner = "androidx.test:runner:1.5.0-alpha04"
            const val junit = "androidx.test.ext:junit-ktx:1.1.4-alpha07"
        }
    }

    object Utils {
        const val timber = "com.jakewharton.timber:timber:5.0.1"
        const val threeTenAbp = "com.jakewharton.threetenabp:threetenabp:1.4.0"
        const val proguardSnippets = "com.github.yongjhih.android-proguards:android-proguards-all:-SNAPSHOT"

        const val threeTen = "org.threeten:threetenbp:1.6.1"

        const val coilVersion = "2.2.1"
        const val coil = "io.coil-kt:coil:$coilVersion"
        const val store = "com.dropbox.mobile.store:store4:4.0.5"

        // const val fetch = "androidx.tonyodev.fetch2:xfetch2:3.1.6"
        // const val fetchOkhttp = "androidx.tonyodev.fetch2okhttp:xfetch2okhttp:3.1.6"
        const val fetch = "com.github.alashow:Fetch:3.1.62"
        const val fetchOkhttp = "com.github.alashow:xfetch2okhttp:3.1.62"

        const val exoPlayer = "com.google.android.exoplayer:exoplayer-core:2.15.1"
        const val exoPlayerOkhttp = "com.google.android.exoplayer:extension-okhttp:2.15.0"
        const val exoPlayerFlac = "com.github.alashow.ExoPlayer-Extensions:extension-flac:v2.15.1"

        const val qonversion = "io.qonversion.android.sdk:sdk:3.3.1"
    }

    object OkHttp {
        private const val version = "5.0.0-alpha.10"

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
        private const val version = "2.42"

        const val dagger = "com.google.dagger:dagger:$version"
        const val compiler = "com.google.dagger:dagger-compiler:$version"

        const val hilt = "com.google.dagger:hilt-android:$version"
        const val hiltCompiler = "com.google.dagger:hilt-compiler:$version"
        const val hiltGradle = "com.google.dagger:hilt-android-gradle-plugin:$version"
        const val hiltTesting = "com.google.dagger:hilt-android-testing:$version"
    }

    object LeakCanary {
        private const val version = "2.7"

        const val leakCanary = "com.squareup.leakcanary:leakcanary-android:$version"
    }

    object Firebase {

        const val bom = "com.google.firebase:firebase-bom:30.5.0"
        const val messaging = "com.google.firebase:firebase-messaging-ktx"
        const val remoteConfig = "com.google.firebase:firebase-config-ktx"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        const val crashlyticsGradle = "com.google.firebase:firebase-crashlytics-gradle:2.9.1"
    }

    object Testing {
        const val junit = "junit:junit:4.13.2"
        const val truth = "com.google.truth:truth:1.1.3"
        const val robolectric = "org.robolectric:robolectric:4.8.2"
        const val mockito = "org.mockito:mockito-core:4.7.0"
        const val mockitoKotlin = "org.mockito.kotlin:mockito-kotlin:4.0.0"
        const val mockk = "io.mockk:mockk:1.12.8"
        const val turbine = "app.cash.turbine:turbine:0.8.0"
    }
}
