/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.util

import org.librefit.enums.userPreferences.UnitSystem
import org.librefit.models.Weight
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlateCalculatorTest {

    private val metricPlates = StandardPlatesMetric
    private val imperialPlates = StandardPlatesImperial

    @Test
    fun `calculatePlates uses greedy largest-first strategy`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(100.0),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )

        // 40 kg per side: greedy largest-first with a 15 kg plate available → 25 + 15
        assertEquals(
            mapOf(Weight.kilograms(25.0) to 1, Weight.kilograms(15.0) to 1),
            breakdown.platesPerSide
        )
        assertEquals(0.0, breakdown.remainder.inKilograms, absoluteTolerance = 1e-9)
    }

    @Test
    fun `calculatePlates repeats plates when needed`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(120.0),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )

        // 50 kg per side: 2x25
        assertEquals(mapOf(Weight.kilograms(25.0) to 2), breakdown.platesPerSide)
        assertEquals(0.0, breakdown.remainder.inKilograms, absoluteTolerance = 1e-9)
    }

    @Test
    fun `calculatePlates reports remainder when target cannot be met exactly`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(50.0),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates.filterNot { it.isSmallPlate(UnitSystem.METRIC) }
        )

        // 15 kg per side: the 15 kg plate fits exactly
        assertEquals(mapOf(Weight.kilograms(15.0) to 1), breakdown.platesPerSide)
        assertEquals(0.0, breakdown.remainder.inKilograms, absoluteTolerance = 1e-9)
    }

    @Test
    fun `calculatePlates remainder is rounded to two decimal digits`() {
        // 20.005 kg per side is not achievable; remainder must be the rounded leftover
        val breakdown = calculatePlates(
            target = Weight.kilograms(60.02),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )

        // 20.005 kg per side: 25 doesn't fit, 20 fits, remainder 0.005 rounds to 0.01
        assertEquals(mapOf(Weight.kilograms(20.0) to 1), breakdown.platesPerSide)
        assertEquals(0.01, breakdown.remainder.inKilograms, absoluteTolerance = 1e-9)
    }

    @Test
    fun `calculatePlates returns empty when target is less than or equal to bar`() {
        val belowBar = calculatePlates(
            target = Weight.kilograms(10.0),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )
        val equalToBar = calculatePlates(
            target = Weight.kilograms(20.0),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )

        assertTrue(belowBar.platesPerSide.isEmpty())
        assertEquals(0.0, belowBar.remainder.inKilograms, absoluteTolerance = 1e-9)
        assertTrue(equalToBar.platesPerSide.isEmpty())
        assertEquals(0.0, equalToBar.remainder.inKilograms, absoluteTolerance = 1e-9)
    }

    @Test
    fun `calculatePlates achieves exact fit with fractional plates`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(52.5),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )

        // 16.25 kg per side: 15 + 1.25
        assertEquals(
            mapOf(Weight.kilograms(15.0) to 1, Weight.kilograms(1.25) to 1),
            breakdown.platesPerSide
        )
        assertEquals(0.0, breakdown.remainder.inKilograms, absoluteTolerance = 1e-9)
    }

    @Test
    fun `filterSmallPlates excludes fractional plates when disabled`() {
        val withoutSmall = metricPlates.filterSmallPlates(
            includeSmallPlates = false,
            unitSystem = UnitSystem.METRIC
        )

        assertContentEquals(
            listOf(25.0, 20.0, 15.0, 10.0, 5.0, 2.5),
            withoutSmall.map { it.inKilograms }
        )
    }

    @Test
    fun `filterSmallPlates keeps all plates when enabled`() {
        val withSmall = metricPlates.filterSmallPlates(
            includeSmallPlates = true,
            unitSystem = UnitSystem.METRIC
        )

        assertEquals(metricPlates.size, withSmall.size)
    }

    @Test
    fun `calculatePlates with small plates excluded only uses large plates`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(52.5),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates.filterSmallPlates(false, UnitSystem.METRIC)
        )

        // 16.25 kg per side: 15 fits, remainder 1.25 has no plate (small plates excluded)
        assertEquals(mapOf(Weight.kilograms(15.0) to 1), breakdown.platesPerSide)
        assertEquals(1.25, breakdown.remainder.inKilograms, absoluteTolerance = 1e-9)
    }

    @Test
    fun `calculatePlates is unit-system equivalent between kg and lb inputs`() {
        val metricBreakdown = calculatePlates(
            target = Weight.kilograms(100.0),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )
        val imperialBreakdown = calculatePlates(
            target = Weight.auto(220.46, UnitSystem.IMPERIAL),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )

        assertEquals(metricBreakdown.platesPerSide, imperialBreakdown.platesPerSide)
        assertEquals(metricBreakdown.remainder, imperialBreakdown.remainder)
    }

    @Test
    fun `calculatePlates works with imperial plates`() {
        val breakdown = calculatePlates(
            target = Weight.auto(225.0, UnitSystem.IMPERIAL),
            bar = Weight.auto(45.0, UnitSystem.IMPERIAL),
            availablePlates = imperialPlates
        )

        // 90 lb per side: 2x45
        assertEquals(mapOf(Weight.pounds(45.0) to 2), breakdown.platesPerSide)
        assertEquals(0.0, breakdown.remainder.inKilograms, absoluteTolerance = 1e-9)
    }

    @Test
    fun `isSmallPlate classifies fractional plates per unit system`() {
        assertTrue(Weight.kilograms(1.25).isSmallPlate(UnitSystem.METRIC))
        assertTrue(Weight.kilograms(0.5).isSmallPlate(UnitSystem.METRIC))
        assertTrue(Weight.kilograms(0.25).isSmallPlate(UnitSystem.METRIC))
        assertFalse(Weight.kilograms(2.5).isSmallPlate(UnitSystem.METRIC))
        assertTrue(Weight.pounds(1.25).isSmallPlate(UnitSystem.IMPERIAL))
        assertFalse(Weight.pounds(2.5).isSmallPlate(UnitSystem.IMPERIAL))
    }

    @Test
    fun `closestAchievable equals bar plus twice the loaded plate mass`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(103.76),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )

        // 41.88 kg per side: 25 + 15 + 1.25 + 0.5 = 41.75 loaded, ~0.13 kg leftover
        assertEquals(0.13, breakdown.remainder.inKilograms, absoluteTolerance = 1e-9)

        // The achievable total must derive from the unrounded leftover (bar + loaded mass),
        // never from the rounded remainder (103.76 - 2 x 0.13 = 103.5 happens to agree here,
        // but the invariant below rules out any remainder-based derivation).
        assertEquals(103.5, breakdown.closestAchievable.inKilograms, absoluteTolerance = 1e-9)
        val loadedPerSideKg = breakdown.platesPerSide.entries
            .sumOf { (plate, count) -> plate.inKilograms * count }
        assertEquals(
            2 * loadedPerSideKg + 20.0,
            breakdown.closestAchievable.inKilograms,
            absoluteTolerance = 1e-9
        )
    }

    @Test
    fun `closestAchievable stays exact across the kg to lb round trip`() {
        val breakdown = calculatePlates(
            target = Weight.auto(225.0, UnitSystem.IMPERIAL),
            bar = Weight.auto(45.0, UnitSystem.IMPERIAL),
            availablePlates = imperialPlates
        )

        // 225 lb is exactly achievable (bar + 2 x 45 lb per side); the kg<->lb round trip
        // introduces ~1e-14 lb of float noise that must not surface in the result.
        assertEquals(0.0, breakdown.remainder.inKilograms, absoluteTolerance = 1e-9)
        assertEquals(225.0, breakdown.closestAchievable.inPounds, absoluteTolerance = 1e-9)
    }
}
