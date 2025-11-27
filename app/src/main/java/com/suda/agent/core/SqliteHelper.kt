package com.suda.agent.core

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SqliteHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "SUDA_API_MAPPING.db"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("CREATE TABLE API_CALL (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", token_header TEXT" +
                ", api_method TEXT" +
                ", answer_kr TEXT" +
                ", answer_en TEXT" +
                ")")

        db?.execSQL("CREATE TABLE API_PARAM (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT " +
                ", api_call_id INTEGER " +
                ", param TEXT" +
                ", value TEXT" +
                ", FOREIGN KEY (api_call_id) REFERENCES API_CALL (id) ON DELETE CASCADE)")

        db?.execSQL("CREATE TABLE LLM_PARAM (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", api_call_id INTEGER" +
                ", param TEXT" +
                ", value TEXT" +
                ", FOREIGN KEY (api_call_id) REFERENCES API_CALL (id) ON DELETE CASCADE)")

        db?.execSQL("CREATE TABLE PARALLEL_ANSWER (" +
                "llm_response TEXT PRIMARY KEY" +
                ", answer_kr TEXT" +
                ", answer_en TEXT)")

        db?.execSQL("CREATE TABLE MULTITURN_ANSWER (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT" +
                ", token_header TEXT" +
                ", multiturn_end INTEGER" +
                ", answer_order INTEGER" +
                ", answer_kr TEXT" +
                ", answer_en TEXT)")

        db?.execSQL("CREATE TABLE MULTITURN_LLM_PARAM (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT " +
                ", multiturn_answer_id INTEGER " +
                ", param TEXT" +
                ", value TEXT" +
                ", FOREIGN KEY (multiturn_answer_id) REFERENCES API_CALL (id) ON DELETE CASCADE)")

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS API_CALL")
        db?.execSQL("DROP TABLE IF EXISTS API_PARAM")
        db?.execSQL("DROP TABLE IF EXISTS LLM_PARAM")
        db?.execSQL("DROP TABLE IF EXISTS PARALLEL_ANSWER")
        db?.execSQL("DROP TABLE IF EXISTS MULTITURN_ANSWER")
        db?.execSQL("DROP TABLE IF EXISTS MULTITURN_LLM_PARAM")
        onCreate(db)
    }

    fun insert(tokenHeader: String, apiMethod: String, answerKr: String, answerEn: String, llmParams: Map<String, String>, apiParams: Map<String, String>) {
        val check = selectByMultiParam(tokenHeader, llmParams)

        if (check != null) {
            return
        }

        val db = writableDatabase
        db.beginTransaction()

        try {
            val apiCall = ContentValues().apply {
                put("token_header", tokenHeader)
                put("api_method", apiMethod)
                put("answer_kr", answerKr)
                put("answer_en", answerEn)
            }
            val apiCallId = db.insert("API_CALL", null, apiCall)

            llmParams.forEach { (param, value) ->
                val llmParam = ContentValues().apply {
                    put("api_call_id", apiCallId)
                    put("param", param)
                    put("value", value)
                }
                db.insert("LLM_PARAM", null, llmParam)
            }

            apiParams.forEach{ (param, value) ->
                val apiParam = ContentValues().apply {
                    put("api_call_id", apiCallId)
                    put("param", param)
                    put("value", value)
                }
                db.insert("API_PARAM", null, apiParam)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun insertMultiturnAnswer(tokenHeader: String, answerOrder: Int, isMultiturnEnd: Boolean, answerKr: String, answerEn: String, params: Map<String, String>) {
        val check = selectMultiturnAnswerByParam(tokenHeader, answerOrder, params)

        if (check != null) {
            return
        }

        val db = writableDatabase
        db.beginTransaction()

        try {
            val multiturnAnswer = ContentValues().apply {
                put("token_header", tokenHeader)
                put("answer_order", answerOrder)
                put("answer_kr", answerKr)
                put("answer_en", answerEn)
                put("multiturn_end", if (isMultiturnEnd) 1 else 0)
            }
            val id = db.insert("MULTITURN_ANSWER", null, multiturnAnswer)

            params.forEach { (param, value) ->
                val param = ContentValues().apply {
                    put("multiturn_answer_id", id)
                    put("param", param)
                    put("value", value)
                }
                db.insert("MULTITURN_LLM_PARAM", null, param)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun selectMultiturnAnswerByParam(tokenHeader: String, multiturnCount: Int, params: Map<String, String>): MultiturnAnswer? {
        var result: MultiturnAnswer? = null
        val db = this.readableDatabase

        var query = "SELECT ma.answer_order, ma.multiturn_end, ma.answer_kr, ma.answer_en from MULTITURN_ANSWER ma "

        params.keys.forEachIndexed { index, _ ->
            query += "left join MULTITURN_LLM_PARAM lp$index on ma.id = lp$index.multiturn_answer_id "
        }

        query += "where ma.token_header = ? AND ma.answer_order = ? "

        params.keys.forEachIndexed { index, _ ->
            query += "AND lp$index.param = ? AND lp$index.value = ?"
        }

        var queryArgs: Array<String> = arrayOf(tokenHeader, multiturnCount.toString())

        params.forEach { (key, value) ->
            queryArgs = queryArgs.plus(key).plus(value)
        }

        val cursor = db.rawQuery(query, queryArgs)

        if (cursor.moveToFirst()) {
            val answerOrder = cursor.getInt(cursor.getColumnIndexOrThrow("answer_order"))
            val answerKR = cursor.getString(cursor.getColumnIndexOrThrow("answer_kr"))
            val answerEN = cursor.getString(cursor.getColumnIndexOrThrow("answer_en"))
            val multiturnEnd = cursor.getInt(cursor.getColumnIndexOrThrow("multiturn_end"))

            result = MultiturnAnswer(if (multiturnEnd == 0) false else true, answerOrder, answerKR, answerEN)
        }
        cursor.close()

        return result
    }

    fun selectByMultiParam(tokenHeader: String, params: Map<String, String>): ApiCallParam? {
        var result: ApiCallParam? = null
        val db = this.readableDatabase

        var query = "SELECT ac.api_method, ac.answer_kr, ac.answer_en, ap.param AS api_param, ap.value AS api_value " +
                "FROM API_CALL ac " +
                "LEFT JOIN API_PARAM ap ON ac.id = ap.api_call_id "

        params.keys.forEachIndexed { index, _ ->
            query += "LEFT JOIN LLM_PARAM lp$index ON ac.id = lp$index.api_call_id "
        }

        query += "WHERE ac.token_header = ? "

        params.keys.forEachIndexed { index, _ ->
            query += "AND lp$index.param = ? AND lp$index.value = ? "
        }

        var queryArgs = arrayOf(tokenHeader)

        params.forEach { (key, value) ->
            queryArgs = queryArgs.plus(key).plus(value)
        }

        val cursor = db.rawQuery(query, queryArgs)

        if (cursor.moveToFirst()) {
            val apiMethod = cursor.getString(cursor.getColumnIndexOrThrow("api_method"))
            val answerKR = cursor.getString(cursor.getColumnIndexOrThrow("answer_kr"))
            val answerEN = cursor.getString(cursor.getColumnIndexOrThrow("answer_en"))

            val apiParams = HashMap<String, String>()

            do {
                val apiParam = cursor.getString(cursor.getColumnIndexOrThrow("api_param"))
                val apiValue = cursor.getString(cursor.getColumnIndexOrThrow("api_value"))

                if (apiParam != null && apiValue != null) {
                    apiParams[apiParam] = apiValue
                }
            } while (cursor.moveToNext())

            result = ApiCallParam(apiMethod, answerKR, answerEN, apiParams)
        }
        cursor.close()

        return result
    }


    fun selectParallelAnswer(llmResponse: String): ParallelAnswer? {
        var result: ParallelAnswer? = null
        val db = this.readableDatabase

        val cursor = db.rawQuery("""
            SELECT answer_kr, answer_en
            FROM PARALLEL_ANSWER
            WHERE llm_response = ?
        """.trimIndent(), arrayOf(llmResponse)
        )

        if (cursor.moveToFirst()) {
            val answerKR = cursor.getString(cursor.getColumnIndexOrThrow("answer_kr"))
            val answerEN = cursor.getString(cursor.getColumnIndexOrThrow("answer_en"))

            result = ParallelAnswer(answerKR, answerEN)
        }
        cursor.close()

        return result
    }

    fun insertParallelAnswer(llmResponse: String, answerKr: String, answerEn: String) {
        val check = selectParallelAnswer(llmResponse)

        if (check != null) {
            return
        }

        val db = writableDatabase
        db.beginTransaction()

        try {
            val parallelAnswer = ContentValues().apply {
                put("llm_response", llmResponse)
                put("answer_en", answerEn)
                put("answer_kr", answerKr)
            }
            db.insert("PARALLEL_ANSWER", null, parallelAnswer)

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun selectByApiParam(tokenHeader: String, params: Map<String, String>): ApiCallParam? {
        var result: ApiCallParam? = null
        val db = this.readableDatabase

        var query = "SELECT ac.api_method, ac.answer_kr, ac.answer_en, lp.param, lp.value " +
                "from API_CALL ac " +
                "left join LLM_PARAM lp on ac.id = lp.api_call_id "

        params.keys.forEachIndexed { index, _ ->
            query += "left join API_PARAM ap$index on ac.id = ap$index.api_call_id "
        }

        query += "where ac.token_header = ? "

        params.keys.forEachIndexed { index, _ ->
            query += "AND ap$index.param = ? AND ap$index.value = ?"
        }

        var queryArgs = arrayOf(tokenHeader)

        params.forEach { (key, value) ->
            queryArgs = queryArgs.plus(key).plus(value)
        }

        val cursor = db.rawQuery(query, queryArgs)

        if (cursor.moveToFirst()) {
            val apiMethod = cursor.getString(cursor.getColumnIndexOrThrow("api_method"))
            val answerKR = cursor.getString(cursor.getColumnIndexOrThrow("answer_kr"))
            val answerEN = cursor.getString(cursor.getColumnIndexOrThrow("answer_en"))

            val apiParams = HashMap<String, String>()

            do {
                val apiParam = cursor.getString(cursor.getColumnIndexOrThrow("param"))
                val apiValue = cursor.getString(cursor.getColumnIndexOrThrow("value"))

                apiParams[apiParam] = apiValue
            } while (cursor.moveToNext())

            result = ApiCallParam(apiMethod, answerKR, answerEN, apiParams)
        }
        cursor.close()

        return result
    }

    data class ApiCallParam (
        val apiMethod: String,
        val answerKr: String,
        val answerEn: String,
        val params: Map<String, String>
    )

    data class ParallelAnswer (
        val answerKr: String,
        val answerEn: String
    )

    data class MultiturnAnswer (
        val isMultiturnEnd: Boolean,
        val answerOrder: Int,
        val answerKr: String,
        val answerEn: String
    )

    fun initApiParam() {
        // ===============================================================
        // [1. 물 순환 펌프 제어] <maum_0> / type=1,2
        // ===============================================================
        insert("<maum_0>", "","물을 순환시킵니다.", "Starting the circulating pump.", mapOf("type" to "1"), mapOf())
        insert("<maum_0>", "", "물 순환을 멈췄습니다.", "Stopping the circulating pump.", mapOf("type" to "2"), mapOf())

        // ===============================================================
        // [2. LED 제어] <maum_1> / type=1,2
        // ===============================================================
        insert("<maum_1>", "","조명을 켰습니다.", "Turning on the LED light.", mapOf("type" to "1"), mapOf())
        insert("<maum_1>", "", "조명을 껐습니다.", "Turning off the LED light.", mapOf("type" to "2"), mapOf())

        // ===============================================================
        // [3. 팬 제어] <maum_2> / type=1,2
        // ===============================================================
        insert("<maum_2>", "","팬을 켰습니다.", "Turning on the fan.", mapOf("type" to "1"), mapOf())
        insert("<maum_2>", "","팬을 껐습니다.", "Turning off the fan.", mapOf("type" to "2"), mapOf())

        // ===============================================================
        // [4. 환경 조회] <maum_3> / object=1~5
        //   1: EC, 2: 온도, 3: 습도, 4: 햇빛, 5: CO₂
        // ===============================================================
        insert("<maum_3>", "", "현재 양액 농도(EC)를 측정하고 있습니다.","Checking the current EC level.", mapOf("object" to "1"), mapOf())
        insert("<maum_3>", "","현재 온도를 확인하고 있습니다.","Checking the current temperature.",mapOf("object" to "2"), mapOf())
        insert("<maum_3>", "", "현재 습도를 확인하고 있습니다.","Checking the current humidity.",mapOf("object" to "3"), mapOf())
        insert("<maum_3>", "", "현재 일조량을 측정하고 있습니다.","Checking the current light level.",mapOf("object" to "4"), mapOf())
        insert("<maum_3>", "", "현재 이산화탄소 농도를 측정하고 있습니다.","Checking the CO2 level.", mapOf("object" to "5"), mapOf())

        // ===============================================================
        // [5. 상태 조회] <maum_4> / object=1~2
        //   1: 배터리 상태, 2: 전체 컨디션
        // ===============================================================
        insert("<maum_4>", "", "현재 배터리는 100% 남아 있으며 충전 중입니다.", "The battery is currently at 100% and charging.", mapOf("object" to "1"), mapOf())
        insert("<maum_4>", "", "이산화탄소가 많고, 햇빛이 더 필요합니다.","CO₂ is high and the plants need more light.",mapOf("object" to "2"), mapOf())

        // ===============================================================
        // [6. 배터리 시스템 제어] <maum_5> / type=1,2
        // ===============================================================
        insert("<maum_5>", "", "저전력 모드를 켰습니다.","Battery saver mode has been turned on.",mapOf("type" to "1"), mapOf())
        insert("<maum_5>", "", "저전력 모드를 해제했습니다.","Battery saver mode has been turned off.",mapOf("type" to "2"), mapOf())

        // ===============================================================
        // [7. 전체 시스템 제어] <maum_6> / type=1
        // ===============================================================
        insert("<maum_6>", "", "모든 장치를 종료했습니다.", "All devices have been turned off.", mapOf("type" to "1"), mapOf())

        // ===============================================================
        // [8. 병해충 진단] <maum_7> / type=1
        // ===============================================================
        insert("<maum_7>", "", "병해충 진단을 시작합니다.", "Starting pest and disease diagnosis.", mapOf("type" to "1"), mapOf())

        // ===============================================================
        // [멀티턴] 되묻기 질문
        //   - maum_0 : 제어 계열 (펌프/LED/팬 등)
        //   - maum_3 : 환경/센서 조회 계열
        // ===============================================================
        //insertMultiturnAnswer("<maum_0>", 1, false,"어떤 기능을 제어할까요? (물 순환, 조명, 팬 등)", "Which function would you like to control? (pump, light, fan, etc.)",mapOf("type" to "-1"))
        //insertMultiturnAnswer("<maum_0>", 2, true,"이해하지 못했습니다. 다시 말씀해 주세요.","I didn't understand. Please try again.", mapOf("type" to "-1"))

        //insertMultiturnAnswer("<maum_3>", 1, false,"어떤 환경 정보를 확인해 드릴까요? (온도, 습도, EC, 햇빛, CO₂)", "Which environmental information would you like to check? (temperature, humidity, EC, light, CO₂)", mapOf("object" to "-1"))
        //insertMultiturnAnswer("<maum_3>", 2, true,"죄송합니다. 다시 말씀해 주세요.", "Sorry, please say that again.",mapOf("object" to "-1"))
    }

}