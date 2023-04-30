@file:JvmName("Paths")

package com.github.unix_junkie.dependency_scanner.io

import java.io.File
import java.io.IOException

typealias Path = File

val Path.safeCanonicalPath: Path
	get() =
		when {
			exists() -> try {
				canonicalFile
			} catch (_: IOException) {
				normalizedAbsolutePath
			}

			else -> normalizedAbsolutePath
		}

val Path.normalizedAbsolutePath: Path
	get() =
		absoluteFile.normalize()

/**
 * Adds [relative] name to this, considering this as a directory.
 * If [relative] has a root, [relative] is returned back.
 * For instance, `Path("/foo/bar").resolve("gav")` is `Path("/foo/bar/gav")`.
 *
 * @return concatenated this and [relative] paths, or just [relative] if it's
 *   absolute.
 */
operator fun Path.div(relative: String): Path =
	resolve(relative)

fun Path.safeIsSameFileAs(other: Path): Boolean =
	normalizedAbsolutePath == other.normalizedAbsolutePath
			|| safeCanonicalPath == other.safeCanonicalPath
