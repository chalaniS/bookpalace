package com.bookpalace.app

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bookpalace.app.ui.DashboardActivity
import org.hamcrest.Matchers.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BookIntegrationTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DashboardActivity::class.java)

    @Test
    fun testAddBookFlow() {
        // 1. Click FAB to open Add Book Dialog
        onView(withId(R.id.fabAddBook)).perform(click())

        // 2. Fill in book details
        onView(withId(R.id.etTitle)).perform(typeText("Integration Test Book"), closeSoftKeyboard())
        onView(withId(R.id.etAuthor)).perform(typeText("Test Author"), closeSoftKeyboard())
        onView(withId(R.id.etPublisher)).perform(typeText("Test Publisher"), closeSoftKeyboard())
        onView(withId(R.id.etYear)).perform(typeText("2024"), closeSoftKeyboard())
        onView(withId(R.id.etCategory)).perform(typeText("Education"), closeSoftKeyboard())

        // 3. Click Add button
        onView(withId(R.id.btnAddBook)).perform(click())

        // 4. Verify that we are back on the main screen (FAB is visible)
        onView(withId(R.id.fabAddBook)).check(matches(isDisplayed()))
    }

    @Test
    fun testSearchBookFlow() {
        // Type search query in SearchView
        onView(withId(R.id.searchView)).perform(typeText("Integration Test Book"), pressImeActionButton())

        // Verify that the list is updated (at least the RecyclerView is still displayed)
        onView(withId(R.id.recyclerViewBooks)).check(matches(isDisplayed()))
    }
}
