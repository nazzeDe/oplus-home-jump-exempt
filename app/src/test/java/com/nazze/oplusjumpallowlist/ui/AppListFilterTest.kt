package com.nazze.oplusjumpallowlist.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class AppListFilterTest {

    private val alpha = AppListFilter.Row("a.pkg", "Alpha", isSystem = false)
    private val beta = AppListFilter.Row("b.pkg", "Beta", isSystem = false)
    private val system = AppListFilter.Row("c.sys", "SystemTool", isSystem = true)
    private val niagara = AppListFilter.Row("bitpit.launcher", "Niagara Launcher", isSystem = false)

    @Test
    fun filterAndSort_checkedPackages_pinToTop_thenAlphabetical() {
        val result = AppListFilter.filterAndSort(
            apps = listOf(alpha, beta, niagara),
            selectedPackages = setOf(niagara.packageName, beta.packageName),
            showSystem = false,
            query = "",
        )
        assertEquals(
            listOf(beta.packageName, niagara.packageName, alpha.packageName),
            result.map { it.packageName },
        )
    }

    @Test
    fun filterAndSort_hidesSystemUnlessEnabled() {
        val hidden = AppListFilter.filterAndSort(
            apps = listOf(alpha, system),
            selectedPackages = emptySet(),
            showSystem = false,
            query = "",
        )
        assertEquals(listOf(alpha), hidden)

        val shown = AppListFilter.filterAndSort(
            apps = listOf(alpha, system),
            selectedPackages = setOf(system.packageName),
            showSystem = true,
            query = "",
        )
        assertEquals(listOf(system, alpha), shown)
    }

    @Test
    fun filterAndSort_matchesLabelOrPackage_caseInsensitive() {
        val byLabel = AppListFilter.filterAndSort(
            apps = listOf(alpha, niagara),
            selectedPackages = emptySet(),
            showSystem = false,
            query = "niagara",
        )
        assertEquals(listOf(niagara), byLabel)

        val byPackage = AppListFilter.filterAndSort(
            apps = listOf(alpha, niagara),
            selectedPackages = emptySet(),
            showSystem = false,
            query = "BITPIT",
        )
        assertEquals(listOf(niagara), byPackage)
    }
}
