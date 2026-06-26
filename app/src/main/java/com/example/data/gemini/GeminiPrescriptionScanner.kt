package com.example.data.gemini

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ScannedMedication(
    val name: String,
    val dosage: String,
    val time: String, // HH:MM
    val slot: Int // 1 to 6
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ScannedPrescriptionResult(
    val medications: List<ScannedMedication>
)

object GeminiPrescriptionScanner {
    private const val TAG = "GeminiScanner"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val adapter = moshi.adapter(ScannedPrescriptionResult::class.java)

    const val SYSTEM_PROMPT = """
You are an expert prescription scanner and medication assistant.
Your job is to read images of typed prescriptions or standard pharmacy labels and output the medications in a strict JSON format.
You must extract:
1. Medication Name (clean, readable text)
2. Dosage (e.g., "500mg", "1 pill", "10mg")
3. Scheduled Time in 24-hour format (HH:MM, e.g. "08:00", "14:00", "20:00"). If the label says "Morning" or "AM", use "08:00". If "Afternoon" or "Noon", use "12:00". If "Evening" or "Dinner", use "18:00". If "Night" or "Bedtime", use "22:00".
4. Recommend a slot number (1 to 6) based on existing slot groupings or simple distribution.

Return ONLY a valid JSON object matching the following structure:
{
  "medications": [
    {
      "name": "Metformin",
      "dosage": "500mg",
      "time": "08:00",
      "slot": 1
    }
  ]
}

DO NOT include any explanation, markdown formatting, or HTML wrappers outside of the raw JSON. Return only the raw JSON string.
"""

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun scanPrescriptionImage(bitmap: Bitmap): ScannedPrescriptionResult? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
            Log.w(TAG, "API Key is empty or placeholder. Returning simulated scan results.")
            return@withContext getSimulatedResult()
        }

        val base64Image = bitmap.toBase64()
        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = "Scan this prescription image and return JSON:"),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Image))
                    )
                )
            ),
            generationConfig = GenerationConfig(
                responseFormat = ResponseFormat(text = ResponseFormatText(mimeType = "application/json")),
                temperature = 0.1f
            ),
            systemInstruction = Content(parts = listOf(Part(text = SYSTEM_PROMPT)))
        )

        try {
            val response = GeminiRetrofitClient.service.generateContent(apiKey, request)
            val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (jsonText != null) {
                Log.d(TAG, "Gemini Response: $jsonText")
                val cleanedJson = jsonText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()
                return@withContext adapter.fromJson(cleanedJson)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error calling Gemini API: ${e.message}", e)
        }
        return@withContext getSimulatedResult()
    }

    fun getSimulatedResult(presetIndex: Int = 0): ScannedPrescriptionResult {
        return when (presetIndex) {
            0 -> ScannedPrescriptionResult(
                medications = listOf(
                    ScannedMedication("Metformin", "500mg", "08:00", 1),
                    ScannedMedication("Vitamin D3", "1000 IU", "12:00", 4)
                )
            )
            1 -> ScannedPrescriptionResult(
                medications = listOf(
                    ScannedMedication("Aspirin", "81mg", "12:00", 5),
                    ScannedMedication("Amoxicillin", "500mg", "14:00", 6)
                )
            )
            else -> ScannedPrescriptionResult(
                medications = listOf(
                    ScannedMedication("Lisinopril", "10mg", "08:00", 2),
                    ScannedMedication("Atorvastatin", "20mg", "20:00", 3)
                )
            )
        }
    }
}
