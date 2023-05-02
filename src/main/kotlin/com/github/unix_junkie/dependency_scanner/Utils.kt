@file:JvmName("Utils")

package com.github.unix_junkie.dependency_scanner

import com.github.unix_junkie.dependency_scanner.environment.Environment
import com.github.unix_junkie.dependency_scanner.io.Path
import com.github.unix_junkie.dependency_scanner.io.div
import com.github.unix_junkie.dependency_scanner.io.safeCanonicalPath
import java.io.File.pathSeparatorChar

private const val UNIX_LIBRARY_NAME_PREFIX = "lib"

private const val UNIX_LIBRARY_NAME_SUFFIX = ".so"

private val DEFAULT_EXECUTABLE_EXTENSIONS: Array<out String> = arrayOf(
	".com",
	".exe",
	".bat",
	".cmd",
)

private val SPECIAL_UNIX_LIBRARY_PREFIXES: Array<out String> = arrayOf(
	"ld-linux",
	"linux-vdso",
	"linux-gate",
)

val executableExtensions: Array<out String> by lazy {
	when {
		isWindows -> {
			val extensions = Environment()["PATHEXT"].orEmpty()
				.split(pathSeparatorChar)
				.asSequence()
				.map(String::lowercase)
				.toMutableSet()
			extensions += DEFAULT_EXECUTABLE_EXTENSIONS
			extensions.toTypedArray()
		}

		else -> emptyArray()
	}
}

val isWindows: Boolean
	get() =
		System.getProperty("os.name").startsWith("Windows ")

val String.isLibrary: Boolean
	get() =
		isWindowsLibrary || isUnixLibrary

val String.isWindowsExecutable: Boolean
	get() =
		endsWith(".exe", ignoreCase = true)

val String.isWindowsLibrary: Boolean
	get() =
		endsWith(".dll", ignoreCase = true)

val String.isUnixLibrary: Boolean
	get() =
		isSpecialUnixLibrary
				|| (startsWith(UNIX_LIBRARY_NAME_PREFIX)
				&& length > UNIX_LIBRARY_NAME_PREFIX.length + UNIX_LIBRARY_NAME_SUFFIX.length
				&& subSequence(UNIX_LIBRARY_NAME_PREFIX.length + 1, length).contains(UNIX_LIBRARY_NAME_SUFFIX))

private val String.isSpecialUnixLibrary: Boolean
	get() =
		SPECIAL_UNIX_LIBRARY_PREFIXES.any(this::startsWith)

fun findInPath(executable: String): Sequence<Path> =
	Environment()["PATH"].orEmpty()
		.split(pathSeparatorChar)
		.asSequence()
		.map(::Path)
		.flatMap { pathEntry ->
			executable.toPlatformSpecificExecutableNames()
				.map { platformSpecificName ->
					pathEntry / platformSpecificName
				}
		}
		.filter(Path::isFile)
		.filter(Path::canExecute)
		.map(Path::safeCanonicalPath)
		.distinct()

private fun String.toPlatformSpecificExecutableNames(): Sequence<String> =
	when {
		isWindows -> sequence {
			/*
			 * For `command.com`, yield `command.com`.
			 */
			if (executableExtensions.any(this@toPlatformSpecificExecutableNames::endsWith)) {
				yield(this@toPlatformSpecificExecutableNames)
			}

			/*
			 * For `command.com`, yield `command.com.com` and `command.com.exe`.
			 */
			yieldAll(executableExtensions.asSequence().map { ext ->
				this@toPlatformSpecificExecutableNames + ext
			})
		}

		else -> sequenceOf(this)
	}
