@file:JvmName("Utils")

package com.github.unix_junkie.dependency_scanner

import com.github.unix_junkie.dependency_scanner.environment.Environment
import com.github.unix_junkie.dependency_scanner.io.div
import java.io.File
import java.io.File.pathSeparatorChar

private val DEFAULT_EXECUTABLE_EXTENSIONS: Array<out String> = arrayOf(
	".com",
	".exe",
	".bat",
	".cmd",
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

fun findInPath(executable: String): Sequence<File> =
	Environment()["PATH"].orEmpty()
		.split(pathSeparatorChar)
		.asSequence()
		.map(::File)
		.flatMap { pathEntry ->
			executable.toPlatformSpecificExecutableNames()
				.map { platformSpecificName ->
					pathEntry / platformSpecificName
				}
		}
		.filter(File::isFile)
		.filter(File::canExecute)
		.map(File::getAbsoluteFile)
		.map(File::normalize)
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
