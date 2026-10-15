# Sync status

## Working in code (not yet compiled or device-tested)

- Sign-in: one-time code for invited teachers, then their own password; password sign-in goes to
  the server. The demo one-tap sign-in and the password-less demo login are removed.
- Offline store (survives restarts), server client, sync engine.
- Queued and sent when online: follow-ups, attendance, Expo scores (ids matched through the
  cached snapshot), **and now admin changes**: new schools, new teachers, assignments, new
  students, payments, and settings updates (see "New sync op types" below). All are safe to
  create offline — they are saved on the phone immediately and sent on the next successful sync.
- Screens now read the cached snapshot instead of sample data. `DeloMindsRepository.loadFromServerSnapshot()`
  maps the snapshot into every list the screens use (schools, teachers, assignments, curriculum,
  students, payments, follow-ups, notifications, audit logs, settings, the signed-in account —
  see `SnapshotMapper.kt`), and merges in anything added locally that hasn't synced yet, so
  adding something offline never makes it disappear before the next sync confirms it.
- Admins and teachers now get different snapshots: `ServerClient.snapshotForCurrentRole()` calls
  `/api/mobile/snapshot` for a teacher and the new `/api/admin/snapshot` for `admin`,
  `super_admin` or `school_admin`.
- **Root-cause fix:** the app used to jump straight to the seeded demo dashboard on launch no
  matter who (if anyone) was signed in. It now always starts at the real sign-in screen, and only
  loads data / navigates past it once `ServerClient.isSignedIn()` is true, confirmed by a
  successful sync. The sample/demo data is still seeded at startup (it backs the in-app "switch
  role" demo picker in the top bar), but a real snapshot now overwrites it as soon as one loads.
- **Root-cause fix:** "Sign out" used to only change the screen — it never told the server or
  forgot the session, so relaunching the app silently resumed as the same person. It now flushes
  any pending changes, calls `/api/logout`, and forgets the session on the phone first.
- Sync when the app opens (snapshot applied immediately from cache, then again after the network
  round trip), right after sign-in (so the dashboard is populated immediately, not after waiting
  for the background worker), and every 15 minutes in the background when connected (WorkManager).
- Cleartext http allowed for local testing; remove when the server uses https.

## New sync op types (server work needed)

These are additions to the existing `/api/mobile/sync` contract — same envelope as the
already-working `followup` / `attendance` / `expo` ops (`{client_id, type, payload, created_at}`
in, `{client_id, status: "ok"|"rejected"|"retry", message?}` out). The server isn't part of this
repo, so these are the shapes the Android client now sends; whoever owns the backend needs to
implement matching handlers.

- **`school`** — `{name, school_code, location, address, phone, email, principal, grades_offered: [string]}`.
  Create (or match-by-`school_code` if it already exists — the app always sends the same code for
  a given school, generated from the name).
- **`teacher`** — `{name, email, phone, address}`. Create (or match-by-`email`).
- **`assignment`** — `{teacher_email, school_code, grade_group, grade, class_name, academic_year, term}`.
  Resolves `teacher_email` and `school_code` server-side, so this works even when the teacher or
  school was queued in the same offline batch, as long as the server applies `school`/`teacher`
  ops before `assignment` ops in a batch (natural processing order; the app doesn't guarantee
  array order beyond "oldest queued first").
- **`student`** — `{code, full_name, school_code, grade, class_name, guardian_name, guardian_phone}`.
  Create (or match-by-`code`, the human-readable student id shown in the app, e.g. "STU-0012").
- **`payment`** — `{receipt_number, student_code, school_code, grade, month, amount, currency, payment_method, status, notes}`.
  `status` is one of `PAID`, `PENDING`, `PARTIAL`, `OVERDUE`.
- **`settings`** — `{organisation_name, currency, monthly_fee, academic_year, programme_start_date, current_week, receipt_footer, app_web_address, smtp_host, smtp_port, sms_webhook_url}`.
  Full replace, last-write-wins (it's a singleton).

## `/api/admin/snapshot` — expected shape

Same GET-with-session-cookie pattern as the existing `/api/mobile/snapshot`. Every top-level key
is optional — the mapper only updates the lists it finds, so a trimmed-down response (e.g. no
`audit_logs` yet) is fine and won't blank out anything already loaded:

```
{
  "me": { "id", "name", "email", "phone", "role", "assigned_school_ids": [], "assigned_grade_groups": [], "must_change" },
  "schools": [{ "id", "school_code", "name", "location", "address", "phone", "email", "principal",
                "contact_person", "contact_phone", "contact_email", "grades_offered": [],
                "student_count", "teacher_count", "coverage_percent", "pending_follow_ups",
                "programme_start_date", "is_archived", "notes" }],
  "teachers": [{ "id", "name", "email", "phone", "address", "status", "pending_follow_ups",
                 "completed_follow_ups", "coverage_percent" }],
  "assignments": [{ "id", "teacher_id", "teacher_email", "teacher_name", "school_id", "school_code",
                     "school_name", "grade_group", "grade", "class_name", "academic_year", "term",
                     "status", "coverage_percent", "weekly_coverage": { "1": 100, "2": null, ... } }],
  "curriculum_parts": [{ "id", "code", "title", "grade_range", "color_hex", "total_weeks", "coverage_percent" }],
  "curriculum_weeks": [{ "part_id", "week_number", "term", "topic", "learning_objectives": [],
                          "big_idea", "key_words": [], "day1_core_concept", "day2_hands_on",
                          "day3_application", "worked_examples", "troubleshooting",
                          "grade_differentiation", "self_check", "sketch_notes",
                          "checkpoint_questions": [] }],
  "students": [{ "id", "code", "full_name", "school_id", "school_code", "school_name", "grade",
                 "class_name", "guardian_name", "guardian_phone", "attendance_present",
                 "attendance_late", "attendance_absent", "expo_score", "is_archived" }],
  "payments": [{ "id", "receipt_number", "student_id", "student_code", "student_name", "school_id",
                 "school_code", "school_name", "grade", "month", "amount", "currency",
                 "payment_date", "payment_method", "status", "notes" }],
  "follow_ups": [{ "id", "teacher_id", "teacher_name", "school_id", "school_code", "school_name",
                    "grade", "week_number", "topic", "submission_date", "answers": { "1": true, ... },
                    "covered_count", "not_covered_count", "coverage_percentage", "is_submitted",
                    "reopened_by_admin" }],
  "notifications": [{ "id", "title", "message", "timestamp", "is_read", "type" }],
  "audit_logs": [{ "id", "user", "action", "record", "details", "timestamp" }],
  "settings": { ...same fields as the "settings" op payload above... }
}
```

Note this is a different, broader shape than the existing `/api/mobile/snapshot` (which is scoped
to one teacher's own `assignments`/`students`/`questions` for `ServerBridge`'s id lookups — that
contract is unchanged).

## Still to do

- **Compile and fix build errors.** No Android SDK, Gradle, or network access in this environment,
  so this round was checked by reading only, plus a script that verified every brace/paren/bracket
  in all Kotlin files balances. That catches gross syntax slips, not type errors. Push to GitHub
  and let `.github/workflows/build.yml` (or Android Studio) run `gradle assembleDebug` for the real
  check — fix whatever it reports.
- **Device testing against a running server.** Needs the backend above actually built and reachable,
  then a real phone or emulator signed in against it.
- **HTTPS hosting** for teachers who aren't on the same network as the server.
- Google sign-in (excluded by instruction).
- Not yet queued offline (lower priority, not part of this pass): scheduled lessons
  (`scheduleLesson`/`removeScheduledLesson`), ad-hoc follow-up checkpoints (`addFollowUpCheckpoint`),
  and CSV student import (`importStudentsCsv`). These still only update the in-memory/local copy.
