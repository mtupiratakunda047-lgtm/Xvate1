package com.example.delominds.data.sync

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Keeps the teacher's data and unsent changes in files inside the app, so they survive
 * closing the app, restarting the phone, and losing the connection.
 *
 *  - snapshot.json: the last snapshot received from the server (read-only data for offline screens)
 *  - outbox.json:   changes made on the phone that the server has not confirmed yet
 *
 * Every write goes to a temporary file first and then replaces the old file, so a crash
 * mid-write cannot leave half-written data behind.
 */
class OfflineStore(context: Context) {
  private val snapshotFile = File(context.filesDir, "snapshot.json")
  private val outboxFile = File(context.filesDir, "outbox.json")
  private val lock = Any()

  fun saveSnapshot(snapshot: JSONObject) = synchronized(lock) { writeAtomic(snapshotFile, snapshot.toString()) }

  fun loadSnapshot(): JSONObject? = synchronized(lock) {
    var result: JSONObject? = null
    if (snapshotFile.exists()) {
      try {
        result = JSONObject(snapshotFile.readText())
      } catch (e: Exception) {
        result = null // unreadable file: treat as no snapshot
      }
    }
    result
  }

  fun loadOutbox(): JSONArray = synchronized(lock) {
    var result = JSONArray()
    if (outboxFile.exists()) {
      try {
        result = JSONArray(outboxFile.readText())
      } catch (e: Exception) {
        result = JSONArray() // unreadable file: start empty rather than crash
      }
    }
    result
  }

  fun saveOutbox(outbox: JSONArray) = synchronized(lock) { writeAtomic(outboxFile, outbox.toString()) }

  /**
   * Adds a change to the outbox. [change] holds the fields the server expects for its type
   * (for example "type", "assignment_id", "week", "payload"). Returns the new client_id.
   */
  fun enqueue(change: JSONObject): String = synchronized(lock) {
    val id = UUID.randomUUID().toString()
    val outbox = loadOutbox()
    outbox.put(JSONObject(change.toString()).put("client_id", id).put("created_at", System.currentTimeMillis()))
    writeAtomic(outboxFile, outbox.toString())
    id
  }

  private fun writeAtomic(target: File, text: String) {
    val temp = File(target.parentFile, target.name + ".tmp")
    temp.writeText(text)
    if (!temp.renameTo(target)) {
      target.delete()
      temp.renameTo(target)
    }
  }
}
