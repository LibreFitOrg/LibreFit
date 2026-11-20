/*
 * SPDX-License-Identifier: GPL-3.0-or-later
 * Copyright (c) 2026. The LibreFit Contributors
 *
 * LibreFit is subject to additional terms covering author attribution and trademark usage;
 * see the ADDITIONAL_TERMS.md and TRADEMARK_POLICY.md files in the project root.
 */

package org.librefit.models

import org.librefit.enums.SetMode
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RoutineCatalogTest {

    @Test
    fun `catalog is not empty`() {
        assertTrue(RoutineCatalog.all.isNotEmpty())
    }

    @Test
    fun `template ids are unique`() {
        val ids = RoutineCatalog.all.map { it.id }

        assertEquals(ids.size, ids.distinct().size, "duplicate template ids in the catalog")
    }

    @Test
    fun `templates declare at least one exercise`() {
        val empty = RoutineCatalog.all.filter { it.exercises.isEmpty() }

        assertTrue(empty.isEmpty(), "templates without exercises: ${empty.map { it.id }}")
    }

    @Test
    fun `exercise ids are not blank`() {
        val blank = RoutineCatalog.all
            .flatMap { it.exercises }
            .filter { it.idExerciseDC.isBlank() }

        assertTrue(blank.isEmpty())
    }

    @Test
    fun `every template uses positive set counts`() {
        val invalid = RoutineCatalog.all
            .flatMap { template -> template.exercises.map { template.id to it } }
            .filter { it.second.sets <= 0 }

        assertTrue(invalid.isEmpty(), "non-positive set counts in: ${invalid.map { it.first }}")
    }

    @Test
    fun `duration exercises declare elapsed time instead of reps`() {
        val invalid = RoutineCatalog.all
            .flatMap { template -> template.exercises.map { template.id to it } }
            .filter {
                (it.second.setMode == SetMode.DURATION && it.second.elapsedTime <= 0) ||
                    (it.second.setMode != SetMode.DURATION && it.second.reps <= 0)
            }

        assertTrue(invalid.isEmpty(), "inconsistent reps/duration in: ${invalid.map { it.first }}")
    }

    @Test
    fun `every declared category contributes templates`() {
        val categoriesInCatalogOrder = RoutineCatalog.all.map { it.category }.distinct()

        assertEquals(
            categoriesInCatalogOrder.size,
            categoriesInCatalogOrder.distinct().size,
            "duplicate category in the catalog"
        )
        assertContentEquals(RoutineCategory.entries, categoriesInCatalogOrder)
    }

    @Test
    fun `ofCategory returns the templates of that category only`() {
        RoutineCategory.entries.forEach { category ->
            val templates = RoutineCatalog.ofCategory(category)

            assertContentEquals(listOf(category), templates.map { it.category }.distinct())
        }
    }

    @Test
    fun `findById resolves known ids and rejects unknown ones`() {
        val template = RoutineCatalog.all.first()

        assertEquals(template, RoutineCatalog.findById(template.id))
        assertNull(RoutineCatalog.findById("does_not_exist"))
    }
}