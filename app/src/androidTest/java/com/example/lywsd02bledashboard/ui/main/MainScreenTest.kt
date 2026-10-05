package com.example.lywsd02bledashboard.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.lywsd02bledashboard.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
    composeTestRule.setContent {
      MainScreen()
    }
  }

  @Test
  fun dashboardHeader_exists() {
    composeTestRule.onNodeWithText("LYWSD02 BLE").assertExists()
    composeTestRule.onNodeWithText("기기 검색").assertExists()
  }
}
