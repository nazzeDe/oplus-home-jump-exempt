package com.nazze.oplusjumpallowlist.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CallerAllowlistTest {

    @Test
    fun encode_emptyCollection_returnsEmptyString() {
        assertEquals("", CallerAllowlist.encode(emptySet()))
    }

    @Test
    fun encode_trimsSortsAndDropsBlanks() {
        val encoded = CallerAllowlist.encode(
            listOf(" com.b ", "", "com.a", "  ", "com.a"),
        )
        assertEquals("com.a\ncom.b", encoded)
    }

    @Test
    fun decode_emptyString_returnsEmptySet() {
        assertEquals(emptySet<String>(), CallerAllowlist.decode(""))
    }

    @Test
    fun decode_splitsTrimmedPackages() {
        assertEquals(
            setOf("com.a", "com.b"),
            CallerAllowlist.decode(" com.a \n\ncom.b\n"),
        )
    }

    @Test
    fun encodeDecode_roundTrip() {
        val packages = setOf("bitpit.launcher", "com.example.app")
        assertEquals(packages, CallerAllowlist.decode(CallerAllowlist.encode(packages)))
    }

    @Test
    fun isCallerAllowed_nullEncoded_meansReadFailure_failClosed() {
        assertFalse(CallerAllowlist.isCallerAllowed("bitpit.launcher", null))
    }

    @Test
    fun isCallerAllowed_blankCaller_failClosed() {
        assertFalse(CallerAllowlist.isCallerAllowed(null, "bitpit.launcher"))
        assertFalse(CallerAllowlist.isCallerAllowed("", "bitpit.launcher"))
        assertFalse(CallerAllowlist.isCallerAllowed("  ", "bitpit.launcher"))
    }

    @Test
    fun isCallerAllowed_emptyAllowlist_rejectsAllCallers() {
        assertFalse(CallerAllowlist.isCallerAllowed("bitpit.launcher", ""))
    }

    @Test
    fun isCallerAllowed_hitAndMiss() {
        val encoded = CallerAllowlist.encode(setOf("bitpit.launcher"))
        assertTrue(CallerAllowlist.isCallerAllowed("bitpit.launcher", encoded))
        assertFalse(CallerAllowlist.isCallerAllowed("com.other", encoded))
    }
}
