package com.nazze.oplusjumpallowlist.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files
import java.nio.file.attribute.PosixFilePermission

class PrefsAccessTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun ensureWorldReadable_missingFile_returnsFalse() {
        val missing = File(tempFolder.root, "shared_prefs/caller_allowlist.xml")
        assertFalse(PrefsAccess.ensureWorldReadable(missing))
        assertTrue(missing.parentFile!!.isDirectory)
    }

    @Test
    fun ensureWorldReadable_ownerOnlyFile_addsOthersRead() {
        assumeTrue("posix permissions required", posixSupported())

        val dir = tempFolder.newFolder("shared_prefs")
        val file = File(dir, "caller_allowlist.xml")
        file.writeText("""<map><string name="packages">bitpit.launcher</string></map>""")

        Files.setPosixFilePermissions(
            file.toPath(),
            setOf(
                PosixFilePermission.OWNER_READ,
                PosixFilePermission.OWNER_WRITE,
                PosixFilePermission.GROUP_READ,
                PosixFilePermission.GROUP_WRITE,
            ),
        )

        assertFalse(
            Files.getPosixFilePermissions(file.toPath())
                .contains(PosixFilePermission.OTHERS_READ),
        )

        assertTrue(PrefsAccess.ensureWorldReadable(file))

        val perms = Files.getPosixFilePermissions(file.toPath())
        assertTrue(
            "expected OTHERS_READ after ensureWorldReadable, got $perms",
            perms.contains(PosixFilePermission.OTHERS_READ),
        )
        val dirPerms = Files.getPosixFilePermissions(dir.toPath())
        assertTrue(dirPerms.contains(PosixFilePermission.OTHERS_READ))
        assertTrue(dirPerms.contains(PosixFilePermission.OTHERS_EXECUTE))
    }

    @Test
    fun conventionalPrefsFile_joinsSharedPrefsXml() {
        val dataDir = tempFolder.root
        assertEquals(
            File(File(dataDir, "shared_prefs"), "caller_allowlist.xml"),
            PrefsAccess.conventionalPrefsFile(dataDir, "caller_allowlist"),
        )
    }

    private fun posixSupported(): Boolean =
        try {
            Files.getPosixFilePermissions(tempFolder.root.toPath())
            true
        } catch (_: UnsupportedOperationException) {
            false
        }
}
