package org.ferdidrgn.hudaquran

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class HudaQuranMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Yeni FCM Token alındı: $token")
        // Burada token'ı SharedPreferences veya veritabanına kaydedebilir,
        // ya da kendi sunucunuza (backend) gönderebilirsiniz.
        saveTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Gelen push bildirimlerini burada işleyebilirsin
    }

    private fun saveTokenToServer(token: String) {
        // Token kayıt mantığı (Örn: Multiplatform Settings veya Ktor ile backend'e gönderme)
    }
}