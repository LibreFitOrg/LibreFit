/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.librefit.enums.userPreferences.UnitSystem
import org.librefit.models.Weight

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
        assertThat(breakdown.platesPerSide)
            .containsExactly(Weight.kilograms(25.0), 1, Weight.kilograms(15.0), 1)
            .inOrder()
        assertThat(breakdown.remainder.inKilograms).isWithin(1e-9).of(0.0)
    }

    @Test
    fun `calculatePlates repeats plates when needed`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(120.0),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )

        // 50 kg per side: 2x25
        assertThat(breakdown.platesPerSide).containsExactly(Weight.kilograms(25.0), 2)
        assertThat(breakdown.remainder.inKilograms).isWithin(1e-9).of(0.0)
    }

    @Test
    fun `calculatePlates reports remainder when target cannot be met exactly`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(50.0),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates.filterNot { it.isSmallPlate(UnitSystem.METRIC) }
        )

        // 15 kg per side: the 15 kg plate fits exactly
        assertThat(breakdown.platesPerSide).containsExactly(Weight.kilograms(15.0), 1)
        assertThat(breakdown.remainder.inKilograms).isWithin(1e-9).of(0.0)
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
        assertThat(breakdown.platesPerSide).containsExactly(Weight.kilograms(20.0), 1)
        assertThat(breakdown.remainder.inKilograms).isWithin(1e-9).of(0.01)
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

        assertThat(belowBar.platesPerSide).isEmpty()
        assertThat(belowBar.remainder.inKilograms).isWithin(1e-9).of(0.0)
        assertThat(equalToBar.platesPerSide).isEmpty()
        assertThat(equalToBar.remainder.inKilograms).isWithin(1e-9).of(0.0)
    }

    @Test
    fun `calculatePlates achieves exact fit with fractional plates`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(52.5),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )

        // 16.25 kg per side: 15 + 1.25
        assertThat(breakdown.platesPerSide)
            .containsExactly(Weight.kilograms(15.0), 1, Weight.kilograms(1.25), 1)
        assertThat(breakdown.remainder.inKilograms).isWithin(1e-9).of(0.0)
    }

    @Test
    fun `filterSmallPlates excludes fractional plates when disabled`() {
        val withoutSmall = metricPlates.filterSmallPlates(
            includeSmallPlates = false,
            unitSystem = UnitSystem.METRIC
        )

        assertThat(withoutSmall.map { it.inKilograms })
            .containsExactly(25.0, 20.0, 15.0, 10.0, 5.0, 2.5)
            .inOrder()
    }

    @Test
    fun `filterSmallPlates keeps all plates when enabled`() {
        val withSmall = metricPlates.filterSmallPlates(
            includeSmallPlates = true,
            unitSystem = UnitSystem.METRIC
        )

        assertThat(withSmall).hasSize(metricPlates.size)
    }

    @Test
    fun `calculatePlates with small plates excluded only uses large plates`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(52.5),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates.filterSmallPlates(false, UnitSystem.METRIC)
        )

        // 16.25 kg per side: 15 fits, remainder 1.25 has no plate (small plates excluded)
        assertThat(breakdown.platesPerSide).containsExactly(Weight.kilograms(15.0), 1)
        assertThat(breakdown.remainder.inKilograms).isWithin(1e-9).of(1.25)
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

        assertThat(imperialBreakdown.platesPerSide).isEqualTo(metricBreakdown.platesPerSide)
        assertThat(imperialBreakdown.remainder).isEqualTo(metricBreakdown.remainder)
    }

    @Test
    fun `calculatePlates works with imperial plates`() {
        val breakdown = calculatePlates(
            target = Weight.auto(225.0, UnitSystem.IMPERIAL),
            bar = Weight.auto(45.0, UnitSystem.IMPERIAL),
            availablePlates = imperialPlates
        )

        // 90 lb per side: 2x45
        assertThat(breakdown.platesPerSide).containsExactly(Weight.pounds(45.0), 2)
        assertThat(breakdown.remainder.inKilograms).isWithin(1e-9).of(0.0)
    }

    @Test
    fun `isSmallPlate classifies fractional plates per unit system`() {
        assertThat(Weight.kilograms(1.25).isSmallPlate(UnitSystem.METRIC)).isTrue()
        assertThat(Weight.kilograms(0.5).isSmallPlate(UnitSystem.METRIC)).isTrue()
        assertThat(Weight.kilograms(0.25).isSmallPlate(UnitSystem.METRIC)).isTrue()
        assertThat(Weight.kilograms(2.5).isSmallPlate(UnitSystem.METRIC)).isFalse()
        assertThat(Weight.pounds(1.25).isSmallPlate(UnitSystem.IMPERIAL)).isTrue()
        assertThat(Weight.pounds(2.5).isSmallPlate(UnitSystem.IMPERIAL)).isFalse()
    }

    @Test
    fun `closestAchievable equals bar plus twice the loaded plate mass`() {
        val breakdown = calculatePlates(
            target = Weight.kilograms(103.76),
            bar = Weight.kilograms(20.0),
            availablePlates = metricPlates
        )

        // 41.88 kg per side: 25 + 15 + 1.25 + 0.5 = 41.75 loaded, ~0.13 kg leftover
        assertThat(breakdown.remainder.inKilograms).isWithin(1e-9).of(0.13)

        // The achievable total must derive from the unrounded leftover (bar + loaded mass),
        // never from the rounded remainder (103.76 - 2 x 0.13 = 103.5 happens to agree here,
        // but the invariant below rules out any remainder-based derivation).
        assertThat(breakdown.closestAchievable.inKilograms).isWithin(1e-9).of(103.5)
        val loadedPerSideKg = breakdown.platesPerSide.entries
            .sumOf { (plate, count) -> plate.inKilograms * count }
        assertThat(breakdown.closestAchievable.inKilograms)
            .isWithin(1e-9)
            .of(2 * loadedPerSideKg + 20.0)
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
        assertThat(breakdown.remainder.inKilograms).isWithin(1e-9).of(0.0)
        assertThat(breakdown.closestAchievable.inPounds).isWithin(1e-9).of(225.0)
    }
}
