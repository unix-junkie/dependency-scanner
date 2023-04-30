package com.github.unix_junkie.dependency_scanner.ldd

import com.github.unix_junkie.dependency_scanner.io.Path

sealed interface SharedLibrary : LddOutputLine {
	/**
	 * The file name of the library. This path is always relative, except
	 * for the case of a [LibraryInterpreter].
	 */
	val fileName: Path
}
