package com.kalimbamiditrainer

import android.app.Activity
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.util.concurrent.Executors
import kotlin.math.PI
import kotlin.math.sin

class MainActivity : Activity() {
    private val audioExecutor = Executors.newSingleThreadExecutor()
    private val notes = listOf(
        "D6" to 1174.66, "B5" to 987.77,
        "G5" to 783.99, "E5" to 659.25,
        "C5" to 523.25, "A4" to 440.0,
        "F4" to 349.23, "D4" to 293.66,
        "C4" to 261.63, "E4" to 329.63,
        "G4" to 392.0, "B4" to 493.88,
        "D5" to 587.33, "F5" to 698.46,
        "A5" to 880.0, "C6" to 1046.50,
        "E6" to 1318.51
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }
        val title = TextView(this).apply {
            text = "Kalimba MIDI Trainer"
            textSize = 26f
        }
        layout.addView(title)

        val importButton = Button(this).apply {
            text = "Seleziona file MIDI"
            setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/midi", "audio/x-midi", "application/octet-stream"))
                }
                startActivityForResult(intent, 100)
            }
        }
        layout.addView(importButton)

        val status = TextView(this).apply {
            text = "Tocca un tasto per suonare"
            textSize = 18f
        }
        layout.addView(status)

        notes.forEach { (name, frequency) ->
            val button = Button(this).apply {
                text = name
                setOnClickListener {
                    status.text = "Nota: $name"
                    audioExecutor.execute { playTone(frequency) }
                }
            }
            layout.addView(button)
        }

        setContentView(ScrollView(this).apply {
            addView(layout)
        })
    }

    private fun playTone(frequency: Double) {
        val sampleRate = 22050
        val count = sampleRate / 2
        val samples = ShortArray(count)
        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val envelope = (1.0 - i.toDouble() / count)
            val wave = sin(2.0 * PI * frequency * t)
            samples[i] = (wave * envelope * 12000).toInt().toShort()
        }
        val track = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            count * 2,
            AudioTrack.MODE_STATIC
        )
        try {
            track.write(samples, 0, samples.size)
            track.play()
            Thread.sleep(550)
            track.stop()
        } finally {
            track.release()
        }
    }

    override fun onDestroy() {
        audioExecutor.shutdown()
        super.onDestroy()
    }
}
