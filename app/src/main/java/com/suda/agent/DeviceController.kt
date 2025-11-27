package com.suda.agent

import android.util.Log
import okhttp3.*
import org.json.JSONObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.*
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

    // [Part A] 기기(팬, 펌프, LED) 제어 - 재시도(Retry) 로직 포함
    fun controlDevice(device: String, turnOn: Boolean) {
        val action = if (turnOn) "ON" else "OFF"

        // UI 스레드가 멈추지 않도록 백그라운드에서 실행
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            Log.d(TAG, "======= [Control] 1. 제어 요청 진입 ($device -> $action) =======")

            // 1. 연결 상태 확인 (null이면 연결 시도 및 대기)
            if (webSocket == null) {
                Log.w(TAG, "⚠️ [Control] WebSocket이 null입니다. 연결을 시도합니다.")
                connectWebSocket()
                waitForConnection() // 연결될 때까지 최대 3초 대기
            }

            // 2. JSON 생성
            val json = JSONObject().apply {
                put("cmd", "control")
                put("device", device)
                put("state", if (turnOn) "on" else "off")
            }
            val payload = json.toString()
            Log.d(TAG, ">>> [Control] 2. 전송 시도: $payload")

            // 3. 1차 전송 시도
            var isSent = webSocket?.send(payload) ?: false

            // 4. 실패 시 재연결 및 재전송 (Retry Logic)
            if (!isSent) {
                Log.e(TAG, "❌ [Control] 1차 전송 실패! 소켓이 끊긴 것 같습니다. 재연결 시도...")

                // (1) 기존 소켓 정리 및 재연결
                try { webSocket?.close(1000, "Retry") } catch (e: Exception) {}
                webSocket = null // 확실하게 null 처리
                connectWebSocket()

                // (2) 연결될 때까지 대기 (중요!)
                if (waitForConnection()) {
                    // (3) 2차 전송 시도 (Resend)
                    Log.d(TAG, "🔄 [Control] 재연결 성공! 명령을 다시 보냅니다.")
                    isSent = webSocket?.send(payload) ?: false
                    Log.d(TAG, ">>> [Control] 2차 재전송 결과: $isSent")
                } else {
                    Log.e(TAG, "❌ [Control] 재연결 시간 초과 (서버가 죽었거나 네트워크 문제)")
                }
            }

            // 5. 최종 결과 확인
            if (isSent) {
                Log.d(TAG, "✅ [Control] 최종 전송 성공!")
            } else {
                Log.e(TAG, "☠️ [Control] 최종 전송 실패. 명령이 전달되지 않았습니다.")
            }
        }
    }
    private suspend fun waitForConnection(): Boolean {
        var retry = 0
        // webSocket 변수가 null이 아닐 때까지(onOpen이 호출될 때까지) 반복 체크
        while (webSocket == null && retry < 30) {
            kotlinx.coroutines.delay(100) // 0.1초 대기
            retry++
        }

        // 연결된 직후 바로 보내면 씹힐 수 있어서 0.5초 더 대기 (안전장치)
        if (webSocket != null) {
            kotlinx.coroutines.delay(500)
            return true
        }
        return false
    }
/*
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
*/

    // [Part B]센서(EC, 온도, 습도...) 조회 - websocket요청하고 대기해야함
    suspend fun getSensorValue(targetSensor: String): String {
        Log.d(TAG, "======= [1] getSensorValue 호출됨 (Target: $targetSensor) =======")

        // 1. WebSocket 객체가 아예 없으면 연결 시도
        if (webSocket == null) {
            Log.w(TAG, "⚠️ WebSocket 객체 없음. 연결 시도...")
            connectWebSocket()
            waitForConnection() // 연결될 때까지 대기하는 함수 (아래에 추가)
        }

        // 2. 약속(Deferred) 생성
        sensorDeferred = CompletableDeferred()

        val json = JSONObject().apply {
            put("cmd", "get")
            put("target", targetSensor)
        }

        Log.d(TAG, ">>> [2] 전송 시도: $json")

        // 3. 전송 시도
        var isSent = webSocket?.send(json.toString()) ?: false

        // [수정] 전송 실패 시 -> 재연결 후 재전송 시도 (Retry Logic)
        if (!isSent) {
            Log.e(TAG, "❌ 전송 실패! 재연결 시도 중...")

            // 기존 소켓 닫고 재연결
            try { webSocket?.close(1000, "Retry") } catch(e:Exception){}
            webSocket = null
            connectWebSocket()

            // 연결 대기 (최대 2초)
            if (waitForConnection()) {
                // 재전송
                isSent = webSocket?.send(json.toString()) ?: false
                Log.d(TAG, ">>> [Retry] 재전송 결과: $isSent")
            }
        }

        if (!isSent) {
            Log.e(TAG, "❌ 최종 전송 실패")
            return "전송 실패"
        }

        // 4. 응답 대기 (최대 3초)
        Log.d(TAG, "⏳ [4] 응답 대기 시작...")
        val result = withTimeoutOrNull(3000) {
            sensorDeferred?.await()
        }

        Log.d(TAG, "======= [5] 최종 결과: ${result ?: "Timeout"} =======")
        return result ?: "응답 없음"
    }
/*
    // [추가] 연결 대기 헬퍼 함수
    private suspend fun waitForConnection(): Boolean {
        var retry = 0
        while (webSocket == null && retry < 20) {
            kotlinx.coroutines.delay(100)
            retry++
        }
        return webSocket != null
    }

 */
    // WebSocket 이벤트 리스너
    private val wsListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Log.d(TAG, "✅ [WS] WebSocket 연결 성공 (onOpen)")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            // 여기가 핵심입니다. 서버에서 뭐라도 오면 무조건 찍힙니다.
            Log.d(TAG, "📩 [WS] 메시지 수신 (Raw): $text")

            try {
                // 라즈베리파이 응답 파싱
                val json = JSONObject(text)
                val type = json.optString("type")
                Log.d(TAG, "🔎 [WS] 파싱된 타입: $type")

                // 센서값 응답이 왔을 때
                if (type == "sensor_res") {
                    val value = json.optString("value")
                    Log.d(TAG, "💡 [WS] 센서값 획득: $value -> Deferred 전달")
                    sensorDeferred?.complete(value)
                }
                // 제어 응답이 왔을 때
                else if (type == "control_res") {
                    Log.d(TAG, "🎮 [WS] 제어 응답: ${json.optString("msg")}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "💥 [WS] 메시지 파싱 에러", e)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Log.e(TAG, "☠️ [WS] WebSocket 연결 실패 (onFailure)", t)
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "🔒 [WS] WebSocket 닫히는 중: $reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            Log.d(TAG, "🔒 [WS] WebSocket 완전히 닫힘")
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