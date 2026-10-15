package com.example.delominds.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.delominds.data.model.UserAccount
import com.example.delominds.data.model.UserRole
import com.example.delominds.ui.Screen
import com.example.ui.theme.*

@Composable
fun DeloTopBar(
  currentScreen: Screen,
  currentUser: UserAccount,
  unreadNotifications: Int,
  availableAccounts: List<UserAccount>,
  onSwitchRole: (UserAccount) -> Unit,
  onOpenNotifications: () -> Unit,
  onSignOut: () -> Unit,
  onNavigateBack: (() -> Unit)? = null
) {
  var showRoleMenu by remember { mutableStateOf(false) }

  Surface(
    color = DeloSurface,
    tonalElevation = 4.dp,
    modifier = Modifier.fillMaxWidth().testTag("top_bar")
  ) {
    Column {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .statusBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (onNavigateBack != null) {
            IconButton(
              onClick = onNavigateBack,
              modifier = Modifier.testTag("back_button")
            ) {
              Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = DeloGold
              )
            }
          } else {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(DeloGold),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "D",
                color = DeloGoldDark,
                fontWeight = FontWeight.Black,
                fontSize = 18.sp
              )
            }
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Text(
              text = "CREATVATE",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = DeloGold,
              letterSpacing = 1.sp
            )
            Text(
              text = getScreenTitle(currentScreen),
              fontSize = 16.sp,
              fontWeight = FontWeight.SemiBold,
              color = DeloOnBackground,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Notifications Bell
          Box(modifier = Modifier.padding(end = 4.dp)) {
            IconButton(
              onClick = onOpenNotifications,
              modifier = Modifier.testTag("notifications_button")
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notifications",
                tint = if (unreadNotifications > 0) DeloGold else DeloOnSurfaceMuted
              )
            }
            if (unreadNotifications > 0) {
              Badge(
                containerColor = DeloRed,
                contentColor = Color.White,
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .padding(top = 6.dp, end = 6.dp)
              ) {
                Text(text = "$unreadNotifications", fontSize = 10.sp)
              }
            }
          }

          // User Role Pill with Dropdown
          Box {
            Row(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(DeloSurfaceVariant)
                .border(1.dp, DeloBorder, RoundedCornerShape(20.dp))
                .clickable { showRoleMenu = true }
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("role_switcher"),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = currentUser.name.split(" ").first(),
                color = DeloOnSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "(${currentUser.role.displayName.take(5)})",
                color = DeloGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "Switch User",
                tint = DeloGold,
                modifier = Modifier.size(16.dp)
              )
            }

            DropdownMenu(
              expanded = showRoleMenu,
              onDismissRequest = { showRoleMenu = false },
              modifier = Modifier.background(DeloSurface)
            ) {
              Text(
                text = "SWITCH USER / ROLE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = DeloGold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
              )
              Divider(color = DeloBorder)
              availableAccounts.forEach { acc ->
                DropdownMenuItem(
                  text = {
                    Column {
                      Text(
                        text = acc.name,
                        color = if (acc.id == currentUser.id) DeloGold else DeloOnSurface,
                        fontWeight = if (acc.id == currentUser.id) FontWeight.Bold else FontWeight.Normal
                      )
                      Text(
                        text = acc.role.displayName,
                        fontSize = 11.sp,
                        color = DeloOnSurfaceMuted
                      )
                    }
                  },
                  onClick = {
                    showRoleMenu = false
                    onSwitchRole(acc)
                  }
                )
              }
              Divider(color = DeloBorder)
              DropdownMenuItem(
                text = { Text("Sign Out", color = DeloRed) },
                leadingIcon = {
                  Icon(Icons.Default.ExitToApp, contentDescription = null, tint = DeloRed)
                },
                onClick = {
                  showRoleMenu = false
                  onSignOut()
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminChipNavBar(
  currentScreen: Screen,
  onNavigate: (Screen) -> Unit
) {
  val scrollState = rememberScrollState()

  val items = listOf(
    "Dashboard" to Screen.ADMIN_DASHBOARD,
    "Curriculum" to Screen.ADMIN_CURRICULUM,
    "Schools" to Screen.ADMIN_SCHOOLS,
    "Teachers" to Screen.ADMIN_TEACHERS,
    "Students" to Screen.ADMIN_STUDENTS,
    "Attendance" to Screen.ADMIN_ATTENDANCE,
    "Payments" to Screen.ADMIN_PAYMENTS,
    "Expo Rubric" to Screen.ADMIN_EXPO_RUBRIC,
    "Reports" to Screen.ADMIN_REPORTS,
    "Settings" to Screen.ADMIN_SETTINGS,
    "Audit Log" to Screen.ADMIN_AUDIT_LOG
  )

  Surface(color = DeloBackground) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scrollState)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items.forEach { (label, screen) ->
        val isSelected = currentScreen == screen
        FilterChip(
          selected = isSelected,
          onClick = { onNavigate(screen) },
          label = {
            Text(
              text = label,
              fontSize = 12.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = DeloGold,
            selectedLabelColor = DeloGoldDark,
            containerColor = DeloSurfaceVariant,
            labelColor = DeloOnSurface
          ),
          border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = if (isSelected) DeloGold else DeloBorder
          ),
          modifier = Modifier.testTag("admin_tab_$label")
        )
      }
    }
  }
}

@Composable
fun TeacherBottomBar(
  currentScreen: Screen,
  onNavigate: (Screen) -> Unit
) {
  NavigationBar(
    containerColor = DeloSurface,
    contentColor = DeloOnSurface,
    modifier = Modifier
      .navigationBarsPadding()
      .testTag("teacher_bottom_bar")
  ) {
    NavigationBarItem(
      selected = currentScreen == Screen.TEACHER_HOME,
      onClick = { onNavigate(Screen.TEACHER_HOME) },
      icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
      label = { Text("Home", fontSize = 11.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = DeloGoldDark,
        selectedTextColor = DeloGold,
        indicatorColor = DeloGold,
        unselectedIconColor = DeloOnSurfaceMuted,
        unselectedTextColor = DeloOnSurfaceMuted
      )
    )

    NavigationBarItem(
      selected = currentScreen == Screen.TEACHER_LESSONS || currentScreen == Screen.TEACHER_LESSON_DETAIL,
      onClick = { onNavigate(Screen.TEACHER_LESSONS) },
      icon = { Icon(Icons.Default.MenuBook, contentDescription = "Lessons") },
      label = { Text("Lessons", fontSize = 11.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = DeloGoldDark,
        selectedTextColor = DeloGold,
        indicatorColor = DeloGold,
        unselectedIconColor = DeloOnSurfaceMuted,
        unselectedTextColor = DeloOnSurfaceMuted
      )
    )

    NavigationBarItem(
      selected = currentScreen == Screen.TEACHER_FOLLOW_UP_QUESTION ||
          currentScreen == Screen.TEACHER_FOLLOW_UP_REVIEW ||
          currentScreen == Screen.TEACHER_FOLLOW_UP_SUCCESS,
      onClick = { onNavigate(Screen.TEACHER_PROGRESS) },
      icon = { Icon(Icons.Default.FactCheck, contentDescription = "Follow-ups") },
      label = { Text("Follow-ups", fontSize = 11.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = DeloGoldDark,
        selectedTextColor = DeloGold,
        indicatorColor = DeloGold,
        unselectedIconColor = DeloOnSurfaceMuted,
        unselectedTextColor = DeloOnSurfaceMuted
      )
    )

    NavigationBarItem(
      selected = currentScreen == Screen.TEACHER_ATTENDANCE,
      onClick = { onNavigate(Screen.TEACHER_ATTENDANCE) },
      icon = { Icon(Icons.Default.Checklist, contentDescription = "Attend.") },
      label = { Text("Attend.", fontSize = 11.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = DeloGoldDark,
        selectedTextColor = DeloGold,
        indicatorColor = DeloGold,
        unselectedIconColor = DeloOnSurfaceMuted,
        unselectedTextColor = DeloOnSurfaceMuted
      )
    )

    NavigationBarItem(
      selected = currentScreen == Screen.TEACHER_MORE,
      onClick = { onNavigate(Screen.TEACHER_MORE) },
      icon = { Icon(Icons.Default.MoreHoriz, contentDescription = "More") },
      label = { Text("More", fontSize = 11.sp) },
      colors = NavigationBarItemDefaults.colors(
        selectedIconColor = DeloGoldDark,
        selectedTextColor = DeloGold,
        indicatorColor = DeloGold,
        unselectedIconColor = DeloOnSurfaceMuted,
        unselectedTextColor = DeloOnSurfaceMuted
      )
    )
  }
}

private fun getScreenTitle(screen: Screen): String = when (screen) {
  Screen.SIGN_IN -> "Sign in"
  Screen.ADMIN_DASHBOARD -> "Dashboard"
  Screen.ADMIN_CURRICULUM -> "Curriculum"
  Screen.ADMIN_CURRICULUM_DETAIL -> "Week Lesson"
  Screen.ADMIN_SCHOOLS -> "Schools"
  Screen.ADMIN_SCHOOL_DETAIL -> "School Detail"
  Screen.ADMIN_TEACHERS -> "Teachers"
  Screen.ADMIN_TEACHER_DETAIL -> "Teacher Profile"
  Screen.ADMIN_STUDENTS -> "Students"
  Screen.ADMIN_PAYMENTS -> "Payments"
  Screen.ADMIN_RECEIPT_DETAIL -> "Receipt"
  Screen.ADMIN_ATTENDANCE -> "Attendance"
  Screen.ADMIN_EXPO_RUBRIC -> "Expo Rubric"
  Screen.ADMIN_REPORTS -> "Reports"
  Screen.ADMIN_SETTINGS -> "Settings"
  Screen.ADMIN_AUDIT_LOG -> "Audit Log"
  Screen.TEACHER_HOME -> "Teacher home"
  Screen.TEACHER_LESSONS -> "Lesson Plans"
  Screen.TEACHER_LESSON_DETAIL -> "Lesson View"
  Screen.TEACHER_FOLLOW_UP_QUESTION -> "Follow-up"
  Screen.TEACHER_FOLLOW_UP_REVIEW -> "Check answers"
  Screen.TEACHER_FOLLOW_UP_SUCCESS -> "Submitted"
  Screen.TEACHER_ATTENDANCE -> "Attendance"
  Screen.TEACHER_PROGRESS -> "Teacher progress"
  Screen.TEACHER_MORE -> "More Options"
}
