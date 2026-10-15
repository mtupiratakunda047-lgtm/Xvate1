package com.example.delominds.data.sync

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

/**
 * Ties the phone to the server:
 *  - queues teacher actions (they are saved on the phone immediately, whether online or not)
 *  - sends queued actions when the server can be reached, and removes the ones it has answered
 *  - refreshes the offline snapshot
 *
 * Call these from a background dispatcher. Actions queued while offline are kept until they can be sent.
 */
class SyncEngine(private val client: ServerClient, private val store: OfflineStore) {

  data class Report(
    val online: Boolean,
    val sent: Int,
    val applied: Int,
    val refused: List<String>,   // messages for changes the server refused (e.g. not your class)
    val waiting: Int,            // changes still on the phone, to be sent later
    val needsSignIn: Boolean,    // the session has expired; sign in again to continue
  )

  // ----- queueing teacher actions (safe to call offline) -----

  fun queueFollowUp(assignmentId: Long, week: Int, answers: List<Pair<String, String>>): String {
    val arr = JSONArray()
    answers.forEach { (key, answer) -> arr.put(JSONObject().put("key", key).put("answer", answer)) }
    return store.enqueue(
      JSONObject().put("type", "followup").put("assignment_id", assignmentId).put("week", week)
        .put("payload", JSONObject().put("answers", arr))
    )
  }

  /** records: list of (studentId, status) where status is "present", "late" or "absent". */
  fun queueAttendance(assignmentId: Long, date: String, records: List<Pair<Long, String>>): String {
    val arr = JSONArray()
    records.forEach { (studentId, status) -> arr.put(JSONObject().put("student_id", studentId).put("status", status)) }
    return store.enqueue(
      JSONObject().put("type", "attendance")
        .put("payload", JSONObject().put("assignment_id", assignmentId).put("date", date).put("records", arr))
    )
  }

  fun queueExpoScore(payload: JSONObject): String = store.enqueue(JSONObject().put("type", "expo").put("payload", payload))

  /**
   * Queues an admin change (a new school, a new teacher, an assignment, a new student, a
   * payment, or a settings update). [type] is one of "school", "teacher", "assignment",
   * "student", "payment", "settings" — see SYNC_STATUS.md for each one's payload shape.
   * Safe to call offline: the change is saved on the phone and sent on the next successful sync.
   */
  fun queueAdminChange(type: String, payload: JSONObject): String =
    store.enqueue(JSONObject().put("type", type).put("payload", payload))

  fun pendingCount(): Int = store.loadOutbox().length()

  // ----- sending -----

  /** Sends queued changes. Changes the server answered (applied or refused) leave the outbox; the rest stay. */
  fun flush(): Report {
    val outbox = store.loadOutbox()
    if (outbox.length() == 0) return Report(online = true, sent = 0, applied = 0, refused = emptyList(), waiting = 0, needsSignIn = false)
    return try {
      val answer = client.sync(outbox)
      val results = answer.optJSONArray("results") ?: JSONArray()
      val answered = HashSet<String>()
      val refused = mutableListOf<String>()
      var applied = 0
      for (i in 0 until results.length()) {
        val r = results.getJSONObject(i)
        val id = r.optString("client_id", "")
        when (r.optString("status")) {
          "ok" -> { applied++; answered.add(id) }
          "rejected" -> { refused.add(r.optString("message")); answered.add(id) }
          // "retry": the server hit an error; the change stays in the outbox
        }
      }
      val remaining = JSONArray()
      for (i in 0 until outbox.length()) {
        val change = outbox.getJSONObject(i)
        if (change.optString("client_id") !in answered) remaining.put(change)
      }
      store.saveOutbox(remaining)
      Report(online = true, sent = outbox.length(), applied = applied, refused = refused, waiting = remaining.length(), needsSignIn = false)
    } catch (e: ServerException) {
      val expired = e.status == 401 || e.status == 403
      Report(online = !expired, sent = 0, applied = 0, refused = emptyList(), waiting = outbox.length(), needsSignIn = expired)
    } catch (e: IOException) {
      // No connection: nothing is lost, the changes stay on the phone.
      Report(online = false, sent = 0, applied = 0, refused = emptyList(), waiting = outbox.length(), needsSignIn = false)
    }
  }

  /**
   * Refreshes the offline snapshot, using the teacher snapshot or the admin snapshot depending
   * on the signed-in role (see [ServerClient.snapshotForCurrentRole]). Returns false when the
   * server cannot be reached; the old snapshot stays on the phone untouched.
   */
  fun refreshSnapshot(): Boolean = try {
    store.saveSnapshot(client.snapshotForCurrentRole())
    true
  } catch (e: IOException) {
    false
  }

  /** Sends queued changes, then refreshes the snapshot so the phone shows the latest data. */
  fun syncNow(): Report {
    val report = flush()
    if (report.online && !report.needsSignIn) refreshSnapshot()
    return report
  }

  fun cachedSnapshot(): JSONObject? = store.loadSnapshot()

  /** The role the cached snapshot was fetched for ("teacher" or an admin role), or null if unknown. */
  fun cachedRole(): String? = client.lastRole
}
