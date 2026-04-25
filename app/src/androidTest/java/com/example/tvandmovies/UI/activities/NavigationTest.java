package com.example.tvandmovies.UI.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.tvandmovies.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class NavigationTest {
    @Rule
    public ActivityScenarioRule<LoadingActivity> activityRule =
            new ActivityScenarioRule<>(LoadingActivity.class);

    @Test
    public void testFullNavigationFlow() {
        // Belépés vendégként (hogy biztosan eljussunk a MainActivity-re)
        onView(withId(R.id.cntnAsGuest)).perform(click());

        // HomeFragment ellenőrzése
        onView(withId(R.id.greeting)).check(matches(isDisplayed()));
        onView(withId(R.id.greeting)).check(matches(withText("Hello")));

        // Navigáció a Keresés (Explore) oldalra
        onView(withId(R.id.explore)).perform(click());
        onView(withId(R.id.titleSearch)).check(matches(isDisplayed()));
        onView(withId(R.id.titleSearch)).check(matches(withText("Keresés")));

        // Navigáció a Mentett (Bookmark) oldalra
        onView(withId(R.id.bookmark)).perform(click());
        onView(withId(R.id.mainTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.mainTitle)).check(matches(withText("Mentett tartalmak")));

        // Navigáció a Fiók (Account) oldalra
        onView(withId(R.id.account)).perform(click());
        onView(withId(R.id.textUserName)).check(matches(isDisplayed()));
        onView(withId(R.id.textUserName)).check(matches(withText("Vendég")));

        // Vissza a Főoldalra
        onView(withId(R.id.home)).perform(click());
        onView(withId(R.id.greeting)).check(matches(isDisplayed()));
    }
}
