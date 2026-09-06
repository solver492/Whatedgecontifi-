package com.example

import com.example.domain.telegram.TelegramBridgeScript
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun telegramInstallCommand_hasNoParasiticPrefix() {
    val expected = "pkg update -y && pkg install python -y && pip install telethon aiohttp"
    val actual = TelegramBridgeScript.INSTALL_COMMAND.trim()

    // Assert exact equality
    assertEquals(expected, actual)

    // Assert no parasitic prefix like 'ps-ox' or leading space/newline
    assertFalse("Must not start with ps-ox", actual.startsWith("ps-ox"))
    assertTrue("Must start with pkg update", actual.startsWith("pkg update"))
    assertFalse("Must not contain unneeded carriage returns", actual.contains("\r"))
  }
}
