package com.github.unix_junkie.dependency_scanner.ldd

import com.github.unix_junkie.dependency_scanner.io.Path

data class SharedLibraryWithAbsolutePath(
	override val fileName: Path,
	val absolutePath: Path,
) : SharedLibrary {
	override fun toString(): String =
		"$fileName => $absolutePath"
}
