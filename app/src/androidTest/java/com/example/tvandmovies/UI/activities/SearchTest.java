package com.example.tvandmovies.UI.activities;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
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
public class SearchTest {

    @Rule
    public ActivityScenarioRule<LoadingActivity> activityRule =
            new ActivityScenarioRule<>(LoadingActivity.class);

    @Test
    public void testSearchAndFilters() {
        // Belépés vendégként
        onView(withId(R.id.cntnAsGuest)).perform(click());

        // Navigáció a Keresés oldalra
        onView(withId(R.id.explore)).perform(click());

        // Keresés indítása: "Batman"
        // A SearchView belső azonosítóját használjuk
        onView(withId(androidx.appcompat.R.id.search_src_text))
                .perform(typeText("Batman"), closeSoftKeyboard());

        // Ellenőrizzük, hogy látható-e a RecyclerView (találati lista)
        onView(withId(R.id.recyclerView)).check(matches(isDisplayed()));

        // Szűrők tesztelése: Kattintsunk a "Filmek" chipre
        onView(withId(R.id.movieChipButton)).perform(click());
        
        // Ellenőrizzük, hogy a chip kiválasztott állapotba került-e
        onView(withId(R.id.movieChipButton)).check(matches(isChecked()));

        // Kattintsunk a "Sorozatok" chipre
        onView(withId(R.id.seriesChipButton)).perform(click());
        onView(withId(R.id.seriesChipButton)).check(matches(isChecked()));
    }
}
