package com.bookpalace.app

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bookpalace.app.ui.DashboardActivity
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationIntegrationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)

    @Test
    fun testSendNotificationFlow() {
        // 1. Navigate to Notification Fragment via Bottom Navigation
        onView(withId(R.id.nav_borrowbooklist)).perform(click())

        // 2. Verify we are on the Send Notification screen
        onView(withText("Compose Notification")).check(matches(isDisplayed()))

        // 3. Fill in Notification Title
        onView(withId(R.id.etTitle))
            .perform(typeText("Emergency Alert"), closeSoftKeyboard())

        // 4. Fill in Notification Message
        onView(withId(R.id.etMessage))
            .perform(typeText("The library will be closed today due to maintenance."), closeSoftKeyboard())

        // 5. Select All students
        onView(withId(R.id.cbSelectAll)).perform(click())

        // 6. Verify selected count text updates
        onView(withId(R.id.tvSelectedCount)).check(matches(isDisplayed()))

        // 7. Click Send FAB
        // Removed scrollTo() because fabSend is a child of CoordinatorLayout, not a scrollable view.
        onView(withId(R.id.fabSend)).perform(click())

        // 8. Verify Confirmation Dialog appears
        onView(withText("Send Notification"))
            .inRoot(isDialog())
            .check(matches(isDisplayed()))

        onView(withText("Send"))
            .inRoot(isDialog())
            .perform(click())
    }

    @Test
    fun testEmptyFieldsValidation() {
        // 1. Navigate to Notification Fragment
        onView(withId(R.id.nav_borrowbooklist)).perform(click())

        // 2. Ensure FAB is disabled when fields are empty
        onView(withId(R.id.fabSend)).check(matches(not(isEnabled())))
    }
}
