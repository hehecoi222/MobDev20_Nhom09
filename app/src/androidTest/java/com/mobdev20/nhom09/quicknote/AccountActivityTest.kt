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
class AccountActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(AccountActivity::class.java)

    @Test
    fun checkSignInView() {
        Espresso.onView(ViewMatchers.withId(R.id.top_sigin_bar)).check(
           matches(isDisplayed()))
    }

    @Test
    fun checkUserInfoView() {
        Espresso.onView(ViewMatchers.withId(R.id.user_info)).check(
            matches(isDisplayed()))
    }
    @Test
    fun checkSignInButtonView() {
        Espresso.onView(ViewMatchers.withId(R.id.signin_button)).check(
            matches(isDisplayed()))
    }
    @Test
    fun checkFunctioncolView() {
        Espresso.onView(ViewMatchers.withId(R.id.function_col)).check(
            matches(isDisplayed()))
    }

    @Test
    fun clickSignInButton() {
        onView(withId(R.id.signin_button)).perform(click())
    }

}