@file:JvmName("Paths")

package com.github.unix_junkie.dependency_scanner.io

import java.io.File
import java.io.IOException

typealias Path = File

private val Path.isRoot: Boolean
	get() =
		isAbsolute && parentFile == null

/**
 * Returns the name of the file or directory denoted by this path as a [Path]
 * object. The file name is the _farthest_ element from the root in the
 * directory hierarchy.
 *
 * @return a path representing the name of the file or directory, or `null` if
 *   this path has zero elements.
 */
val Path.fileName: Path?
	get() =
		when {
			isRoot -> null
			else -> Path(name)
		}

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

fun Path.isChildOf(other: Path): Boolean {
	require(isAbsolute) {
		"Not absolute: $this"
	}
	require(other.isAbsolute) {
		"Not absolute: $other"
	}

	val parent: Path? = parentFile

	return parent != null
			&& (parent.safeIsSameFileAs(other)
			|| parent.isChildOf(other))
}
