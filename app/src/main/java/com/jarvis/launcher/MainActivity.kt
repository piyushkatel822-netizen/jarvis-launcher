package com.jarvis.launcher

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUI()
    }

    private fun buildUI() {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(40, 40, 40, 40)
        }

        status = TextView(this).apply {
            text = "JARVIS\n\nReady"
            textSize = 24f
            gravity = Gravity.CENTER
        }

        val microphoneButton = Button(this).apply {
            text = "ALLOW MICROPHONE"
            setOnClickListener {
                requestMicrophone()
            }
        }

        val overlayButton = Button(this).apply {
            text = "ALLOW TOP JARVIS"
            setOnClickListener {
                openOverlaySettings()
            }
        }

        val startButton = Button(this).apply {
            text = "START JARVIS"
            setOnClickListener {
                startJarvis()
            }
        }

        val stopButton = Button(this).apply {
            text = "STOP JARVIS"
            setOnClickListener {
                stopService(
                    Intent(this@MainActivity, JarvisService::class.java)
                )
                status.text = "JARVIS\n\nStopped"
            }
        }

        layout.addView(status)
        layout.addView(microphoneButton)
        layout.addView(overlayButton)
        layout.addView(startButton)
        layout.addView(stopButton)

        setContentView(layout)
    }

    private fun requestMicrophone() {
        if (
            checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                100
            )
        }
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun startJarvis() {

        if (
            checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestMicrophone()
            return
        }

        if (!Settings.canDrawOverlays(this)) {
            openOverlaySettings()
            return
        }

        val intent = Intent(this, JarvisService::class.java)

        startForegroundService(intent)

        status.text = "JARVIS\n\n● ACTIVE"
    }
}
