package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CreatVate Delo Minds", appName)
  }

  @Test
  fun `repository follow-up calculation is accurate`() {
    val repo = com.example.delominds.data.repository.DeloMindsRepository()
    val sub = repo.submitFollowUp(
      teacherId = "TCH-001",
      teacherName = "Mutupira",
      schoolId = "SCH-001",
      schoolName = "Riverside Primary School",
      grade = "Grade 6",
      weekNumber = 8,
      topic = "Obstacle Avoidance",
      answers = mapOf(1 to true, 2 to true, 3 to false)
    )
    assertEquals(2, sub.coveredCount)
    assertEquals(1, sub.notCoveredCount)
    assertEquals(66, sub.coveragePercentage) // 2 / 3 * 100 = 66%
  }
}
