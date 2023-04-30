package com.github.unix_junkie.dependency_scanner.ldd

import com.github.unix_junkie.dependency_scanner.io.Path

/**
 * `linux-vdso.so.1`
 */
data class VirtualSharedLibrary(override val fileName: Path) : SharedLibrary {
	override fun toString(): String =
		"$fileName (virtual dynamic shared object)"
}
