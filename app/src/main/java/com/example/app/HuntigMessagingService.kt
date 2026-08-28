package com.example.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * PARTE CLIENTE das notificações push (ver docs/PLANO_DESENVOLVIMENTO.md e
 * docs/ACOES_MANUAIS.md para a parte de servidor, que fica por fazer).
 *
 * Esta classe trata de duas coisas:
 * 1. Guardar o token FCM de cada utilizador no Firebase (onNewToken), para
 *    que uma Cloud Function possa mais tarde enviar-lhe notificações
 *    dirigidas.
 * 2. Mostrar a notificação quando uma mensagem chega (onMessageReceived) —
 *    mas isto só acontece se ALGUMA COISA a enviar primeiro. Sem Cloud
 *    Functions (ou outro mecanismo de servidor) a disparar mensagens
 *    quando acontece uma admissão aprovada, um evento novo, etc., esta
 *    função nunca vai ser chamada sozinha — só serve de infraestrutura
 *    pronta a usar assim que essa parte existir.
 */
class HuntigMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "novo token: $token")
        guardarTokenNoFirebase(token)
    }

    private fun guardarTokenNoFirebase(token: String) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
            // Ainda não há sessão iniciada (ex: app acabou de arrancar antes do
            // login) — o token fica por guardar agora. LoginActivity/
            // RegistoUserActivity chamam guardarTokenAtualNoFirebase() logo a
            // seguir a autenticar com sucesso, para cobrir este caso.
            Log.d("FCM", "sem utilizador autenticado, token nao guardado agora")
            return
        }
        FirebaseDatabase.getInstance().getReference("Users").child(uid).child("fcmToken")
            .setValue(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (!notificacoesEstaoAtivadas()) {
            // Utilizador desligou as notificações em Definições — o token
            // continua registado (para o caso de as voltar a ligar), só não
            // mostramos nada enquanto estiver desligado.
            return
        }

        val titulo = message.notification?.title ?: getString(R.string.app_name)
        val corpo = message.notification?.body ?: ""

        mostrarNotificacao(titulo, corpo)
    }

    private fun mostrarNotificacao(titulo: String, corpo: String) {
        val canalId = "apphuntig_geral"

        // Canais de notificação são obrigatórios a partir do Android 8 (API 26).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                canalId,
                "Notificações gerais",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(canal)
        }

        val intent = Intent(this, VerificarLoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE
        } else {
            0
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlags)

        val notificacao = NotificationCompat.Builder(this, canalId)
            .setContentTitle(titulo)
            .setContentText(corpo)
            .setSmallIcon(R.drawable.ic_menu)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(System.currentTimeMillis().toInt(), notificacao)
    }

    companion object {
        /**
         * Chamar isto logo a seguir a um login/registo com sucesso, para
         * garantir que o token FCM fica associado à conta mesmo que já
         * existisse antes do login (ex: reinstalação da app).
         */
        fun guardarTokenAtualNoFirebase() {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            com.google.firebase.messaging.FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    FirebaseDatabase.getInstance().getReference("Users").child(uid)
                        .child("fcmToken").setValue(token)
                }
        }
    }
}
