/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.ui

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.google.android.vending.licensing.AESObfuscator
import com.google.android.vending.licensing.LicenseChecker
import com.google.android.vending.licensing.LicenseCheckerCallback
import com.google.android.vending.licensing.Policy
import com.google.android.vending.licensing.ServerManagedPolicy
import timber.log.Timber
import tm.alashow.base.util.IntentUtils
import tm.alashow.datmusic.R
import tm.alashow.ui.components.TextRoundedButton
import tm.alashow.ui.theme.AppTheme

open class LicenseCheckingActivity : ComponentActivity() {

    companion object {
        private val SALT = byteArrayOf(-26, 65, 3, -18, -102, -37, 54, -64, 51, 38, -93, -45, 77, -117, -36, -113, -11, 32, -64, 89)
        private const val BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAlhtOvrV/Q" +
            "utlUKMtW+Z6YV0CELOhGNfpi7tUWYXeITTvEgbz0SLM1q9v/q" +
            "mGUKMqmgI+xTVlk/RFDyVdC6z+LDFwf97K8n5mNCNGe3XQ2wg" +
            "R2mCRScgRAHwtwsYY3uw4z4cCkg2bu7zPexZLEm6n41jrxv1W" +
            "sRreqDJnc1bYAj7DECAvxlEmaM5mLTebTf+vqzWMM/OpAjjae" +
            "AoIqmX3+CL+lPUEfRCMQ8BNH0tHqv5WxLnSdvIWEgFe1PuaeL" +
            "RwY9yD4hiLJlVo6+qN6AIZka5jww/P2wzfHhlRPaBQKXDK+9O" +
            "qBaQojQJmnsuwwWkFUIakP6CsjvlSXpBL7L4v/QIDAQAB"
    }

    private val licenseCheckerCallback = object : LicenseCheckerCallback {
        override fun allow(reason: Int) {
            if (isFinishing) return
            Timber.d("LicenseChecker.allowed, reason = $reason")
        }

        override fun dontAllow(reason: Int) {
            Timber.d("LicenseChecker.dontAllow, reason = $reason")
            if (isFinishing) return
            if (reason == Policy.RETRY) {
                checkLicense()
            } else {
                onFail()
            }
        }

        override fun applicationError(errorCode: Int) {
            Timber.d("LicenceChecker.appError, error = $errorCode")
            onFail()
        }
    }

    private lateinit var policy: Policy
    private lateinit var licenseChecker: LicenseChecker

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            policy = ServerManagedPolicy(
                this,
                AESObfuscator(
                    SALT, packageName,
                    Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                )
            )
            licenseChecker = LicenseChecker(this, policy, BASE64_PUBLIC_KEY)
            checkLicense()
        } catch (e: Exception) {
            Timber.e(e, "Error while checking license")
            onFail()
        }
    }

    override fun onResume() {
        super.onResume()
        checkLicense()
    }

    override fun onDestroy() {
        super.onDestroy()
        licenseChecker.onDestroy()
    }

    private fun checkLicense() {
        if (!isNetworkAvailable(this)) {
            setContent {
                LicenseErrorScreen(
                    errorMessage = R.string.license_error_network,
                    buttonText = R.string.license_retry,
                    onButtonClick = ::checkLicense
                )
            }
        } else licenseChecker.checkAccess(licenseCheckerCallback)
    }

    private fun onFail() {
        setContent {
            LicenseErrorScreen()
        }
    }

    @Composable
    fun LicenseErrorScreen(
        errorMessage: Int = R.string.license_error,
        buttonText: Int = R.string.license_goToPlayStore,
        onButtonClick: (() -> Unit)? = null
    ) {
        AppTheme {

            Surface(Modifier.fillMaxSize()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(AppTheme.specs.padding)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(AppTheme.specs.padding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(stringResource(R.string.error_title), style = MaterialTheme.typography.h4)
                        Text(
                            stringResource(errorMessage),
                            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Thin),
                            textAlign = TextAlign.Center
                        )

                        val context = LocalContext.current
                        TextRoundedButton(
                            onClick = {
                                if (onButtonClick == null)
                                    IntentUtils.openUrl(context, "https://play.google.com/store/apps/details?id=tm.alashow.datmusic")
                                else onButtonClick()
                            },
                            text = stringResource(buttonText),
                        )
                    }
                }
            }
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkInfo = connectivityManager.getNetworkCapabilities(network) ?: return false
            return when {
                networkInfo.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                networkInfo.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                networkInfo.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                networkInfo.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }
}
