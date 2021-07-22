/*
 * Copyright (C) 2021, Alashov Berkeli
 * All rights reserved.
 */
package tm.alashow.datmusic.fcm

import com.google.firebase.messaging.FirebaseMessagingService as FirebaseService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import tm.alashow.datmusic.data.interactors.RegisterFcmToken
import tm.alashow.datmusic.notifications.AppNotification
import tm.alashow.datmusic.notifications.notify

@AndroidEntryPoint
class FirebaseMessagingService : FirebaseService(), CoroutineScope by MainScope() {

    @Inject
    lateinit var registerFcmToken: RegisterFcmToken

    override fun onNewToken(token: String) {
        launch(Dispatchers.Default) {
            registerFcmToken(RegisterFcmToken.Params(token)).collect { result ->
                Timber.d("Result: $result")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        when {
            remoteMessage.data.isNotEmpty() -> notify(AppNotification.fromRemoteMessage(remoteMessage))
            else -> super.onMessageReceived(remoteMessage)
        }
    }
}
