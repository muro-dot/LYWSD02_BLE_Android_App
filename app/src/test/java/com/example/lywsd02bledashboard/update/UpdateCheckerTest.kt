package com.example.lywsd02bledashboard.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 깃허브 릴리즈 버전 비교 로직(Semantic Versioning) 단위 테스트
 */
class UpdateCheckerTest {

    @Test
    fun testIsNewerVersion_whenRemoteIsHigher_returnsTrue() {
        assertTrue(UpdateChecker.isNewerVersion("1.1.1", "1.2.0"))
        assertTrue(UpdateChecker.isNewerVersion("1.0.0", "1.0.1"))
        assertTrue(UpdateChecker.isNewerVersion("1.0.0", "2.0.0"))
        assertTrue(UpdateChecker.isNewerVersion("1.2.0", "1.2.1"))
    }

    @Test
    fun testIsNewerVersion_whenRemoteIsEqualOrLower_returnsFalse() {
        assertFalse(UpdateChecker.isNewerVersion("1.2.0", "1.2.0"))
        assertFalse(UpdateChecker.isNewerVersion("1.2.1", "1.2.0"))
        assertFalse(UpdateChecker.isNewerVersion("2.0.0", "1.9.9"))
        assertFalse(UpdateChecker.isNewerVersion("1.2.0", ""))
    }
}
