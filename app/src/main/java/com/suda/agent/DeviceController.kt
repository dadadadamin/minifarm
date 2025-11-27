package com.suda.agent

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull

object DeviceController {
    private const val TAG = "SmartHome"
    private val client = OkHttpClient()

    // 라즈베리파이 IP 설정(라즈베리파이랑 맞추기!!!!!!!!!!)
    private const val PI_IP = "192.168.0.9"

    // WebSocket 설정 (Python Websockets 포트 8765)
    private const val WS_URL = "ws://$PI_IP:8765"
    private var webSocket: WebSocket? = null
    private var sensorDeferred: CompletableDeferred<String>? = null // 응답 대기용

    // 연결 (앱 켜질 때 MainActivity에서 호출)
    fun connectWebSocket() {
        val request = Request.Builder().url(WS_URL).build()
        webSocket = client.newWebSocket(request, wsListener)
    }

    // [Part A] 기기(팬, 펌프, LED) 제어 -websocket으로 명령 전송
    fun controlDevice(device: String, turnOn: Boolean) {
        if (webSocket == null) {
            connectWebSocket() // 끊겨있으면 재연결 시도
        }

        // JSON 만들기: {"cmd": "control", "device": "pump", "state": "on"}
        val json = JSONObject().apply {
            put("cmd", "control")
            put("device", device)
            put("state", if (turnOn) "on" else "off")
        }

        // 전송 (응답 기다리지 않고 쏘고 끝냄)
        webSocket?.send(json.toString())
        Log.d(TAG, ">>> WebSocket 제어 명령 전송: $json")
    }

    // [Part B]센서(EC, 온도, 습도...) 조회 - websocket요청하고 대기해야함

    // 센서값 요청하고 기다리는 함수 (suspend)
    suspend fun getSensorValue(targetSensor: String): String {
        if (webSocket == null) {
            connectWebSocket() // 끊겨있으면 재연결 시도
            return "연결 중..."
        }

        // 1. 약속(Deferred) 생성
        sensorDeferred = CompletableDeferred()

        // 2. 요청 전송 {"cmd": "get", "target": "1"}
        val json = JSONObject().apply {
            put("cmd", "get")
            put("target", targetSensor)
        }
        webSocket?.send(json.toString())

        // 3. 응답 대기 (최대 3초)
        val result = withTimeoutOrNull(3000) {
            sensorDeferred?.await()
        }
        return result ?: "응답 없음"
    }

    // WebSocket 이벤트 리스너
    private val wsListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "WebSocket 연결됨")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            try {
                // 라즈베리파이 응답: {"value": "24.5"}
                val json = JSONObject(text)
                val type = json.optString("type")

                // 센서값 응답이 왔을 때
                if (type == "sensor_res") {
                    val value = json.optString("value")
                    sensorDeferred?.complete(value)
                }
                // 제어 응답이 왔을 때 (로그만 찍음)
                else if (type == "control_res") {
                    Log.d(TAG, "라즈베리파이 응답: ${json.optString("msg")}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "메시지 파싱 에러", e)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "WebSocket 연결 실패", t)
        }
    }





    //가능한지는 테스트 필요
    fun shutdownSudaKit() {
        try {
            Log.w(TAG, ">>> 시스템 종료 시도...")

            // "su": 슈퍼유저(Root) 권한 획득
            // "-c": 뒤에 오는 명령어 실행
            // "reboot -p": 전원 끄기 (Power off)
            Runtime.getRuntime().exec(arrayOf("su", "-c", "reboot -p"))

        } catch (e: Exception) {
            Log.e(TAG, "시스템 종료 실패 (루트 권한이 없거나 막혀있음)", e)
        }
    }
}