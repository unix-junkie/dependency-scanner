package com.github.unix_junkie.dependency_scanner.ldd

import com.github.unix_junkie.dependency_scanner.io.Path
import com.github.unix_junkie.dependency_scanner.isLibrary

object LddOutputParser {
	private val LDD_OUTPUT = Regex("""^\s*(\S+)\s*(?:=>\s*(?:(not found)|([^\s()].+?[^\s()])))?(?:\s+\(\s*(?:\?|0x[0-9A-Fa-f]+)\s*\))?\s*$""")

	fun parse(line: String): LddOutputLine {
		val result = LDD_OUTPUT.matchEntire(line)
			?: return UnparseableLddOutputLine(line)

		val groupValues = result.groupValues

		if (groupValues.size != 4) {
			return UnparseableLddOutputLine(line)
		}

		val fileName = groupValues[1]
		val notFound = groupValues[2]
		val absolutePath = groupValues[3]

		return when {
			fileName.isEmpty() -> UnparseableLddOutputLine(line)

			else -> when {
				absolutePath.isEmpty() -> when {
					notFound.isNotEmpty() -> {
						check(notFound == "not found") {
							line
						}

						NotFound(Path(fileName))
					}

					fileName.isAbsolute -> LibraryInterpreter(Path(fileName))

					else -> when {
						fileName.isLibrary -> VirtualSharedLibrary(Path(fileName))

						else -> UnparseableLddOutputLine(line)
					}
				}

				else -> SharedLibraryWithAbsolutePath(Path(fileName), absolutePath)
			}
		}
	}
}

sealed interface LddOutputLine

data class UnparseableLddOutputLine(val line: String) : LddOutputLine

sealed interface SharedLibrary : LddOutputLine {
	/**
	 * The file name of the library. This path is always relative, except
	 * for the case of a [LibraryInterpreter].
	 */
	val fileName: Path
}

data class NotFound(override val fileName: Path) : SharedLibrary {
	override fun toString(): String =
		"$fileName => not found"
}

/**
 * `linux-vdso.so.1`
 */
data class VirtualSharedLibrary(override val fileName: Path) : SharedLibrary {
	override fun toString(): String =
		"$fileName (virtual dynamic shared object)"
}

/**
 * `/lib64/ld-linux-x86-64.so.2`
 */
data class LibraryInterpreter(override val fileName: Path) : SharedLibrary {
	override fun toString(): String =
		"$fileName (library interpreter)"
}


data class SharedLibraryWithAbsolutePath(
	override val fileName: Path,
	val absolutePath: String,
) : SharedLibrary {
	override fun toString(): String =
		"$fileName => $absolutePath"
}

/**
 * @return `true` if this is an absolute UNIX path.
 */
private val String.isAbsolute: Boolean
	get() =
		startsWith('/')
