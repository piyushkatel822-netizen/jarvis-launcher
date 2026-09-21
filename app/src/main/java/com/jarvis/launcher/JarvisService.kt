package com.jarvis.launcher

import android.app.*
import android.content.*
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.*
import android.provider.Settings
import android.speech.*
import android.view.*
import android.widget.*
import java.util.Locale

class JarvisService : Service() {

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var recognizer: SpeechRecognizer? = null

    private val channelId = "jarvis_service"

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()
        createNotification()
        createOverlay()
        startListening()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            "JARVIS Voice Service",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager =
            getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(channel)
    }

    private fun createNotification() {

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("JARVIS is active")
            .setContentText("Voice activation is running")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    private fun createOverlay() {

        if (!Settings.canDrawOverlays(this)) {
            return
        }

        val text = TextView(this).apply {
            text = "● JARVIS"
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.BLACK)
            gravity = Gravity.CENTER
            setPadding(24, 8, 24, 8)
        }

        overlayView = text

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )

        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 8

        windowManager =
            getSystemService(WINDOW_SERVICE) as WindowManager

        windowManager.addView(text, params)
    }

    private fun startListening() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            return
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        recognizer?.setRecognitionListener(
            object : RecognitionListener {

                override fun onResults(results: Bundle?) {

                    val matches =
                        results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )

                    val spoken =
                        matches?.joinToString(" ")
                            ?.lowercase(Locale.getDefault())
                            ?: ""

                    if (spoken.contains("jarvis stop")) {
                        stopSelf()
                        return
                    }

                    if (spoken.contains("jarvis")) {
                        speakResponse("Yes, I am listening.")
                    }

                    restartListening()
                }

                override fun onError(error: Int) {
                    restartListening()
                }

                override fun onReadyForSpeech(params: Bundle?) {}
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {}
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(
                    eventType: Int,
                    params: Bundle?
                ) {}
            }
        )

        restartListening()
    }

    private fun restartListening() {

        Handler(Looper.getMainLooper()).postDelayed({

            try {

                val intent = Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                )

                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )

                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault()
                )

                intent.putExtra(
                    RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                    false
                )

                recognizer?.startListening(intent)

            } catch (_: Exception) {
            }

        }, 500)
    }

    private var tts: android.speech.tts.TextToSpeech? = null

    private fun speakResponse(text: String) {
        if (tts == null) {
            tts = android.speech.tts.TextToSpeech(this) { status ->
                if (status == android.speech.tts.TextToSpeech.SUCCESS) {
                    speakWithTts(text)
                }
            }
        } else {
            speakWithTts(text)
        }
    }

    private fun speakWithTts(text: String) {
        val engine = tts ?: return
        engine.language = Locale.US
        engine.speak(
            text,
            android.speech.tts.TextToSpeech.QUEUE_FLUSH,
            null,
            "JARVIS_RESPONSE"
        )
    }

    override fun onDestroy() {

        recognizer?.destroy()
        recognizer = null

        if (overlayView != null) {

            try {
                windowManager.removeView(overlayView)
            } catch (_: Exception) {
            }

            overlayView = null
        }

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
