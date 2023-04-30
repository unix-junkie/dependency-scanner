package com.github.unix_junkie.dependency_scanner.ldd

import com.github.unix_junkie.dependency_scanner.io.Path

/**
 * `/lib64/ld-linux-x86-64.so.2`
 */
data class LibraryInterpreter(override val fileName: Path) : SharedLibrary {
	override fun toString(): String =
		"$fileName (library interpreter)"
}
