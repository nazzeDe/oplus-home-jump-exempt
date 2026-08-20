package com.nazze.oplusjumpallowlist.ui

/**
 * Pure filter + sort for the settings app list.
 * Checked callers sort above unchecked; within each group, label then packageName.
 */
object AppListFilter {
    data class Row(
        val packageName: String,
        val label: String,
        val isSystem: Boolean,
    )

    fun filterAndSort(
        apps: List<Row>,
        selectedPackages: Set<String>,
        showSystem: Boolean,
        query: String,
    ): List<Row> {
        val needle = query.trim()
        return apps.asSequence()
            .filter { showSystem || !it.isSystem }
            .filter { row ->
                needle.isEmpty() ||
                    row.label.contains(needle, ignoreCase = true) ||
                    row.packageName.contains(needle, ignoreCase = true)
            }
            .sortedWith(
                compareByDescending<Row> { it.packageName in selectedPackages }
                    .thenBy { it.label.lowercase() }
                    .thenBy { it.packageName },
            )
            .toList()
    }
}
