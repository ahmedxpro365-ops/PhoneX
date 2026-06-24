package com.phonex.app.domain

import android.content.Context
import android.provider.CallLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class CallLogEntry(
    val name: String?,
    val number: String,
    val type: Int,
    val date: Long,
    val duration: Long
)

class CallLogRepository(private val context: Context) {
    suspend fun getCallLogs(): List<CallLogEntry> = withContext(Dispatchers.IO) {
        val callLogs = mutableListOf<CallLogEntry>()
        try {
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.CACHED_NAME,
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                null, null, "${CallLog.Calls.DATE} DESC"
            )

            cursor?.use {
                val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)

                while (it.moveToNext() && callLogs.size < 100) {
                    callLogs.add(
                        CallLogEntry(
                            name = if (nameIndex != -1) it.getString(nameIndex) else null,
                            number = if (numberIndex != -1) it.getString(numberIndex) else "",
                            type = if (typeIndex != -1) it.getInt(typeIndex) else 0,
                            date = if (dateIndex != -1) it.getLong(dateIndex) else 0L,
                            duration = if (durationIndex != -1) it.getLong(durationIndex) else 0L
                        )
                    )
                }
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
        callLogs
    }
}
