package com.suda.agent

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
object VisionController {
    private const val TAG = "VisionAI"
    private const val OPENAI_API_KEY = BuildConfig.OPENAI_API_KEY

    private var imageCapture: ImageCapture? = null

    // 1. 카메라 초기화 (MainActivity onCreate에서 호출)
    fun bindCamera(context: Context, lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            // 후면 카메라 선택 (USB 카메라는 보통 후면으로 인식됨)
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            try {
                cameraProvider.unbindAll()
                // 미리보기 없이 캡처 기능만 바인딩 (Headless 모드)
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, imageCapture)
                Log.d(TAG, "카메라 초기화 성공")
            } catch (e: Exception) {
                Log.e(TAG, "카메라 초기화 실패", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    // 2. 사진 촬영 및 파일 저장
    suspend fun takePhoto(context: Context): File? = suspendCancellableCoroutine { cont ->
        val capture = imageCapture
        if (capture == null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        // 저장할 파일 생성
        val photoFile = File(context.externalCacheDir, "plant_diagnosis.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Log.d(TAG, "사진 촬영 성공: ${photoFile.absolutePath}")
                    cont.resume(photoFile)
                }
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "사진 촬영 실패", exc)
                    cont.resume(null)
                }
            }
        )
    }
    // [추가] 현재 인터넷이 되는지 확인하는 함수
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // 현재 활성화된 네트워크 확인
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        // 인터넷 사용 가능 여부 체크
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    // 3. OpenAI Vision API 호출
    fun analyzeDisease(imageFile: File): String {
        try {
            // (1) 이미지를 Base64로 인코딩
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream) // 용량 줄이기
            val base64Image = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

            // (2) JSON Body 생성
            val jsonBody = JSONObject().apply {
                put("model", "gpt-4o") // 또는 gpt-4-turbo
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", JSONArray().apply {
                            put(JSONObject().apply {
                                put("type", "text")
                                put("text", "이 식물의 잎을 보고 병해충 여부를 진단해줘. 병명과 해결책을 한국어로 1문장으로 짧게 요약해서 말해줘.")
                            })
                            put(JSONObject().apply {
                                put("type", "image_url")
                                put("image_url", JSONObject().apply {
                                    put("url", "data:image/jpeg;base64,$base64Image")
                                })
                            })
                        })
                    })
                })
                put("max_tokens", 300)
            }

            // (3) HTTP 요청
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val request = Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .addHeader("Authorization", "Bearer $OPENAI_API_KEY")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return "진단 서버 연결 실패: ${response.code}"

                val resJson = JSONObject(response.body?.string() ?: "")
                val content = resJson.getJSONArray("choices")
                    .getJSONObject(0).getJSONObject("message").getString("content")

                Log.d(TAG, "진단 결과: $content")
                return content
            }

        } catch (e: Exception) {
            Log.e(TAG, "OpenAI API 에러", e)
            return "진단 중 오류가 발생했습니다."
        }
    }
}