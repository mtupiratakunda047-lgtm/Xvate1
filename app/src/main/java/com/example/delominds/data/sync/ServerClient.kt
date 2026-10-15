package com.example.delominds.data.sync

import android.content.Context
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/** The server answered with an error status (not a connection problem). */
class ServerException(val status: Int, message: String) : IOException(message)

/**
 * Talks to the Delo Minds server over HTTP(S). The session cookie from sign-in is kept
 * in SharedPreferences so the teacher stays signed in between launches.
 *
 * Network calls block. Call them from a background dispatcher (Dispatchers.IO), never the UI thread.
 * A connection problem throws IOException; SyncEngine treats that as "offline".
 */
class ServerClient(context: Context) {
  private val prefs = context.getSharedPreferences("delominds_server", Context.MODE_PRIVATE)
  private val jsonType = "application/json; charset=utf-8".toMediaType()
  private val http = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  /** Server address, e.g. https://delominds.example.org or http://192.168.1.42:3000 */
  var baseUrl: String
    get() = prefs.getString("base_url", "") ?: ""
    set(value) { prefs.edit().putString("base_url", value.trim().trimEnd('/')).apply() }

  private var session: String?
    get() = prefs.getString("session", null)
    set(value) {
      prefs.edit().apply { if (value == null) remove("session") else putString("session", value) }.apply()
    }

  /**
   * The role last seen for this session ("teacher", "school_admin", "admin" or "super_admin"),
   * cached on the phone so the app knows which snapshot to load (and which to re-load from the
   * offline copy) without a network round trip. Refreshed whenever [currentAccount] is called.
   */
  var lastRole: String?
    get() = prefs.getString("last_role", null)
    private set(value) {
      prefs.edit().apply { if (value == null) remove("last_role") else putString("last_role", value) }.apply()
    }

  fun isSignedIn(): Boolean = session != null

  /** Forget the session on this phone only. Cached data stays on the phone. */
  fun forgetSession() { session = null }

  private fun send(method: String, path: String, body: JSONObject?): JSONObject {
    check(baseUrl.isNotBlank()) { "The server address has not been set." }
    val requestBody: RequestBody? = when {
      body != null -> body.toString().toRequestBody(jsonType)
      method == "GET" -> null
      else -> "{}".toRequestBody(jsonType)
    }
    val builder = Request.Builder()
      .url(baseUrl + path)
      .header("X-Requested-With", "fetch") // the server refuses state-changing requests without this
      .method(method, requestBody)
    session?.let { builder.header("Cookie", "dm_session=$it") }

    http.newCall(builder.build()).execute().use { response ->
      // Keep the newest session cookie; the server clears it with an empty value on sign-out.
      response.headers("Set-Cookie").forEach { header ->
        if (header.startsWith("dm_session=")) {
          val value = header.substringAfter("dm_session=").substringBefore(";")
          session = value.ifBlank { null }
        }
      }
      val text = response.body?.string().orEmpty()
      val data = if (text.isBlank()) JSONObject() else try { JSONObject(text) } catch (e: Exception) { JSONObject() }
      if (!response.isSuccessful) {
        throw ServerException(response.code, data.optString("error", "The server returned ${response.code}."))
      }
      return data
    }
  }

  // ----- sign-in (one-time code for invited teachers, then the teacher's own password) -----

  fun requestSignInCode(email: String): String =
    send("POST", "/api/otp/request", JSONObject().put("email", email.trim())).optString("message")

  /** Returns true when the teacher must now choose their own password. */
  fun verifySignInCode(email: String, code: String): Boolean =
    send("POST", "/api/otp/verify", JSONObject().put("email", email.trim()).put("code", code.trim()))
      .optBoolean("must_change", false)

  fun signInWithPassword(email: String, password: String) {
    send("POST", "/api/login", JSONObject().put("email", email.trim()).put("password", password))
  }

  /** For an invited teacher (no old password) or a teacher changing a temporary password. */
  fun setPassword(newPassword: String, currentPassword: String? = null) {
    val body = JSONObject().put("password", newPassword)
    if (currentPassword != null) body.put("current", currentPassword)
    send("POST", "/api/me/password", body)
  }

  /** The signed-in account: role ("teacher", "admin", "school_admin", "super_admin") and must_change. */
  fun currentAccount(): JSONObject {
    val user = send("GET", "/api/me", null).optJSONObject("user") ?: JSONObject()
    val role = user.optString("role")
    if (role.isNotBlank()) lastRole = role
    return user
  }

  fun currentRole(): String = currentAccount().optString("role")

  /** True for every role except a plain classroom teacher: the roles the admin snapshot is for. */
  fun isAdminRole(role: String?): Boolean = role == "admin" || role == "super_admin" || role == "school_admin"

  fun signOut() {
    try { send("POST", "/api/logout", null) } finally { session = null; lastRole = null }
  }

  // ----- data -----

  /** Everything a teacher's phone needs for offline use: own assignments, students, questions. */
  fun snapshot(): JSONObject = send("GET", "/api/mobile/snapshot", null)

  /**
   * Everything an admin/school admin's phone needs for offline use: every school, teacher,
   * assignment, student, payment, the curriculum and system settings the role can see.
   * Called instead of [snapshot] when [lastRole] is an admin role. See SYNC_STATUS.md for the
   * expected shape.
   */
  fun adminSnapshot(): JSONObject = send("GET", "/api/admin/snapshot", null)

  /** Fetches whichever snapshot matches the signed-in role, refreshing [lastRole] first. */
  fun snapshotForCurrentRole(): JSONObject {
    val role = currentRole().ifBlank { lastRole }
    return if (isAdminRole(role)) adminSnapshot() else snapshot()
  }

  /** Replays queued changes. Each change has a client_id; the server applies each one at most once. */
  fun sync(ops: JSONArray): JSONObject = send("POST", "/api/mobile/sync", JSONObject().put("ops", ops))
}
