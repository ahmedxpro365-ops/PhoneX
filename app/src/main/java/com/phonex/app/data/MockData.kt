package com.phonex.app.data

import com.phonex.app.domain.CallLogEntry
import com.phonex.app.domain.ContactEntry

object MockData {
    var contacts = mutableListOf(
        ContactEntry("1", "Alice Smith", "+1 555 123 4567", null),
        ContactEntry("2", "Bob Jones", "+1 555 987 6543", null),
        ContactEntry("3", "Charlie Brown", "+1 555 555 5555", null),
        ContactEntry("4", "Diana Prince", "+1 555 111 2222", null),
        ContactEntry("5", "Eve Adams", "+1 555 333 4444", null)
    )

    var callLogs = mutableListOf(
        CallLogEntry("Alice Smith", "+1 555 123 4567", android.provider.CallLog.Calls.INCOMING_TYPE, System.currentTimeMillis() - 1000 * 60 * 5, 120),
        CallLogEntry("Unknown", "+1 555 000 0000", android.provider.CallLog.Calls.MISSED_TYPE, System.currentTimeMillis() - 1000 * 60 * 60, 0),
        CallLogEntry("Bob Jones", "+1 555 987 6543", android.provider.CallLog.Calls.OUTGOING_TYPE, System.currentTimeMillis() - 1000 * 60 * 60 * 24, 45)
    )
}

