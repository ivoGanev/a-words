package com.ivo.ganev.awords

import android.view.LayoutInflater
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.ivo.ganev.awords.view.TextViewClickable
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBe
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    //@get:Rule val rule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {

    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val tv = TextViewClickable(appContext)

        // the word should now be clickable
        tv.text = "Hello World"

        tv.onWordClickedListener = object : TextViewClickable.OnWordClickedListener {
            override fun onWordClick(word: String) {
                println(word)
            }
        }

        tv.onWordClickedListener shouldNotBe null
    }
}