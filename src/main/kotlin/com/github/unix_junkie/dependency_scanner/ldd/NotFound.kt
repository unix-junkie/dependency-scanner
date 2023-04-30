package com.github.unix_junkie.dependency_scanner.ldd

import com.github.unix_junkie.dependency_scanner.io.Path

data class NotFound(override val fileName: Path) : SharedLibrary {
	override fun toString(): String =
		"$fileName => not found"
}
