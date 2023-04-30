package com.github.unix_junkie.dependency_scanner.ldd

import com.github.unix_junkie.dependency_scanner.io.Path
import com.github.unix_junkie.dependency_scanner.io.PathConverter
import com.github.unix_junkie.dependency_scanner.io.normalizedAbsolutePath
import com.github.unix_junkie.dependency_scanner.isLibrary

class LddOutputParser(private val converters: Array<out PathConverter>) {
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

						NotFound(fileName.toPath())
					}

					fileName.isAbsolute -> LibraryInterpreter(fileName.toPath())

					else -> when {
						fileName.isLibrary -> VirtualSharedLibrary(fileName.toPath())

						else -> UnparseableLddOutputLine(line)
					}
				}

				/*-
				 * We don't resolve symlinks here but rather
				 * just normalize the paths, so that the returned
				 * path looks like
				 *
				 * /usr/lib/libstdc++.so.6
				 *
				 * and not like
				 *
				 * /usr/lib/libstdc++.so.6.0.28
				 */
				else -> SharedLibraryWithAbsolutePath(
					fileName.toPath(),
					absolutePath.toPath().normalizedAbsolutePath,
				)
			}
		}
	}

	private fun String.toPath(): Path =
		converters.asSequence()
			.filter(PathConverter::isEnabled)
			.firstOrNull()
			?.convert(this)
			?: Path(this)
}

/**
 * @return `true` if this is an absolute UNIX path.
 */
private val String.isAbsolute: Boolean
	get() =
		startsWith('/')
