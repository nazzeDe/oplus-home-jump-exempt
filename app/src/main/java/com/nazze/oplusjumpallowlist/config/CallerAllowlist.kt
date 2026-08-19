package com.nazze.oplusjumpallowlist.config

/**
 * Pure caller-package allowlist encoding and membership checks.
 *
 * Hooks and settings share this contract. A null encoded snapshot means the
 * configuration could not be read and must fail closed (not exempt).
 */
object CallerAllowlist {
    private const val SEPARATOR = "\n"

    fun encode(packages: Collection<String>): String =
        packages.asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSortedSet()
            .joinToString(SEPARATOR)

    fun decode(encoded: String): Set<String> {
        if (encoded.isEmpty()) return emptySet()
        return encoded.split(SEPARATOR)
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toSet()
    }

    /**
     * @param encodedAllowlist encoded package snapshot, or null when config read failed.
     */
    fun isCallerAllowed(callerPackage: String?, encodedAllowlist: String?): Boolean {
        if (encodedAllowlist == null) return false
        if (callerPackage.isNullOrBlank()) return false
        return callerPackage in decode(encodedAllowlist)
    }
}
