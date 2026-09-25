package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SecureLens", appName)
  }

  @Test
  fun `app initializes correctly`() {
    val app = ApplicationProvider.getApplicationContext<SecureLensApp>()
    assertNotNull(app)
    assertNotNull(app.database)
    assertNotNull(app.cryptoManager)
    assertNotNull(app.settingsStore)
  }
}

