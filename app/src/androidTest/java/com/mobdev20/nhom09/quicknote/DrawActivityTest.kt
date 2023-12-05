package com.mobdev20.nhom09.quicknote

import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DrawActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(DrawActivity::class.java)

    @Test
    fun checkImageView() {
        Espresso.onView(ViewMatchers.withId(R.id.imageView)).check(matches(isDisplayed()))
    }

    @Test
    fun checkButton() {
        Espresso.onView(ViewMatchers.withId(R.id.button)).perform(click())
    }
    @Test
    fun clickButton() {
        onView(withId(R.id.button)).perform(click())
    }

}