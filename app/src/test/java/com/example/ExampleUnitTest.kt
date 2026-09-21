package com.example

import com.example.ui.viewmodel.CateringEstimate
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.math.ceil

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun cateringCalculation_standardCase() {
    val confirmedCount = 42
    val bufferPercent = 10
    val perPlateCost = 350.0

    val bufferPlates = if (confirmedCount > 0) ceil(confirmedCount * bufferPercent / 100.0).toInt() else 0
    val recommendedPlates = confirmedCount + bufferPlates
    val estimatedCost = recommendedPlates * perPlateCost

    // 42 * 10 / 100 = 4.2 -> ceil is 5 -> 47 plates
    assertEquals(5, bufferPlates)
    assertEquals(47, recommendedPlates)
    assertEquals(16450.0, estimatedCost, 0.001)

    val estimate = CateringEstimate(
      confirmedGuestCount = confirmedCount,
      recommendedPlates = recommendedPlates,
      estimatedCost = estimatedCost,
      perPlateCost = perPlateCost,
      bufferPercent = bufferPercent
    )
    assertEquals(47, estimate.recommendedPlates)
    assertEquals(16450.0, estimate.estimatedCost, 0.001)
  }

  @Test
  fun cateringCalculation_zeroGuests() {
    val confirmedCount = 0
    val bufferPercent = 10
    val perPlateCost = 500.0

    val bufferPlates = if (confirmedCount > 0) ceil(confirmedCount * bufferPercent / 100.0).toInt() else 0
    val recommendedPlates = confirmedCount + bufferPlates
    val estimatedCost = recommendedPlates * perPlateCost

    assertEquals(0, bufferPlates)
    assertEquals(0, recommendedPlates)
    assertEquals(0.0, estimatedCost, 0.001)
  }

  @Test
  fun cateringCalculation_singleGuestBufferCeil() {
    val confirmedCount = 1
    val bufferPercent = 10
    val perPlateCost = 250.0

    // 1 * 10 / 100 = 0.1 -> ceil is 1 -> 2 plates
    val bufferPlates = if (confirmedCount > 0) ceil(confirmedCount * bufferPercent / 100.0).toInt() else 0
    val recommendedPlates = confirmedCount + bufferPlates
    val estimatedCost = recommendedPlates * perPlateCost

    assertEquals(1, bufferPlates)
    assertEquals(2, recommendedPlates)
    assertEquals(500.0, estimatedCost, 0.001)
  }
}
