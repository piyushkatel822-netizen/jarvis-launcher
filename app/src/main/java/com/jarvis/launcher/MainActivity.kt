package com.jarvis.launcher

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val microphoneRequestCode = 100

    private lateinit var statusText: TextView
    private lateinit var startButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createInterface()
        updatePermissionStatus()
    }

    private fun createInterface() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
        }

        statusText = TextView(this).apply {
            textSize = 24f
            gravity = Gravity.CENTER
        }

        startButton = Button(this).apply {
            text = "START JARVIS"
            textSize = 18f
            setOnClickListener {
                updatePermissionStatus()
            }
        }

        layout.addView(
            statusText,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        layout.addView(
            startButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 40
            }
        )

        setContentView(layout)
    }

    private fun updatePermissionStatus() {
        val microphoneAllowed =
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                    PackageManager.PERMISSION_GRANTED

        if (microphoneAllowed) {
            statusText.text = "JARVIS\n\n🎤 Microphone Ready"
            startButton.text = "START JARVIS"
        } else {
            statusText.text = "JARVIS\n\n🎤 Microphone permission required"
            startButton.text = "ALLOW MICROPHONE"
            requestMicrophonePermission()
        }
    }

    private fun requestMicrophonePermission() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                microphoneRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions,
            grantResults
        )

        if (requestCode == microphoneRequestCode) {
            updatePermissionStatus()
        }
    }

    override fun onResume() {
        super.onResume()
        if (::statusText.isInitialized) {
            updatePermissionStatus()
        }
    }
}
