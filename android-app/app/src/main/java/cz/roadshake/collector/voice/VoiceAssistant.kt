package cz.roadshake.collector.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import java.util.Locale

class VoiceAssistant(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech = TextToSpeech(context, this)
    private var initialized = false

    override fun onInit(status: Int) {
        initialized = status == TextToSpeech.SUCCESS
        if (initialized) {
            tts.language = Locale("cs", "CZ")
            tts.setSpeechRate(1.0f)
        }
    }

    fun speak(text: String) {
        if (!initialized) return
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "roadshake_tts")
    }

    fun speechIntent(prompt: String): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "cs-CZ")
        putExtra(RecognizerIntent.EXTRA_PROMPT, prompt)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }
}
