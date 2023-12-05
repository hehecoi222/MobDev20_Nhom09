package com.mobdev20.nhom09.quicknote

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.mobdev20.nhom09.quicknote.helpers.EspressoIdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    var activityRule: ActivityTestRule<MainActivity?>? =
        ActivityTestRule(MainActivity::class.java)

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
    }
    @Test
    fun displayTopAppBar() {
        onView(withId(R.id.top_app_bar)).check(matches(isDisplayed()))
    }

    @Test
    fun displayNoteTitle() {
        onView(withId(R.id.note_title_compose)).check(matches(isDisplayed()))
    }

    @Test
    fun displayNoteBody() {
        onView(withId(R.id.note_body)).check(matches(isDisplayed()))
    }

    @Test
    fun displayBottomSheet() {
        onView(withId(R.id.bottom_sheet_drawer)).check(matches(isDisplayed()))
    }

    @Test
    fun inputNoteTitle() {
        onView(withId(R.id.note_title_compose)).perform(typeText("Hello World"))
    }

    @Test
    fun inputNoteBody() {
        onView(withId(R.id.note_body)).perform(typeText("Hello World"))
    }

    @Test
    fun clearNoteTitle() {
        onView(withId(R.id.note_title_compose)).perform(clearText())
    }

    @Test
    fun clearNoteBody() {
        onView(withId(R.id.note_body)).perform(clearText())
    }
}
