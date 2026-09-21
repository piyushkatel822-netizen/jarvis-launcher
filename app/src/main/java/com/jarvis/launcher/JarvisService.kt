package com.jarvis.launcher

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

class JarvisService : Service() {

    private val channelId = "jarvis_voice"
    private val handler = Handler(Looper.getMainLooper())

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var listening = false

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("JARVIS")
            .setContentText("JARVIS is listening")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)

        setupTTS()
        startRecognition()
    }

    private fun createNotificationChannel() {
        val manager =
            getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "JARVIS Voice",
            NotificationManager.IMPORTANCE_LOW
        )

        manager.createNotificationChannel(channel)
    }

    private fun setupTTS() {
        tts = TextToSpeech(this) { result ->

            if (result == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    private fun speak(text: String) {
        val engine = tts ?: return

        engine.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "JARVIS_REPLY"
        )
    }

    private fun startRecognition() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Speech recognition is not available on this phone.")
            return
        }

        if (recognizer == null) {

            recognizer =
                SpeechRecognizer.createSpeechRecognizer(this)

            recognizer?.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(
                        params: Bundle?
                    ) {
                        listening = true
                    }

                    override fun onBeginningOfSpeech() {}

                    override fun onRmsChanged(
                        rmsdB: Float
                    ) {}

                    override fun onBufferReceived(
                        buffer: ByteArray?
                    ) {}

                    override fun onEndOfSpeech() {
                        listening = false
                    }

                    override fun onError(
                        error: Int
                    ) {
                        listening = false
                        restartRecognition()
                    }

                    override fun onResults(
                        results: Bundle?
                    ) {

                        val list =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )

                        val question =
                            list?.firstOrNull()
                                ?.trim()
                                ?: ""

                        if (question.isNotEmpty()) {
                            handleQuestion(question)
                        }

                        restartRecognition()
                    }

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {}

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {}
                }
            )
        }

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )

            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                Locale.getDefault()
            )

            putExtra(
                RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                false
            )
        }

        try {
            recognizer?.startListening(intent)
        } catch (_: Exception) {
            restartRecognition()
        }
    }

    private fun restartRecognition() {

        handler.postDelayed(
            {
                if (!listening) {
                    startRecognition()
                }
            },
            1000
        )
    }

    private fun handleQuestion(question: String) {

        val q = question.lowercase(Locale.getDefault())

        if (q.contains("jarvis stop")) {
            speak("Okay. JARVIS is stopping.")
            handler.postDelayed(
                { stopSelf() },
                1200
            )
            return
        }

        when {

            q.contains("hello") ||
            q.contains("hi") -> {
                speak("Hello. I am JARVIS. How can I help you?")
            }

            q.contains("your name") -> {
                speak("My name is JARVIS.")
            }

            q.contains("time") -> {
                val time =
                    java.text.SimpleDateFormat(
                        "hh:mm a",
                        Locale.getDefault()
                    ).format(
                        java.util.Date()
                    )

                speak("The time is $time")
            }

            else -> {
                speak(
                    "I heard your question: $question. " +
                    "My AI brain is not connected yet."
                )
            }
        }
    }

    override fun onDestroy() {

        handler.removeCallbacksAndMessages(null)

        recognizer?.stopListening()
        recognizer?.destroy()
        recognizer = null

        tts?.stop()
        tts?.shutdown()
        tts = null

        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
