@file:JvmName("Paths")

package com.github.unix_junkie.dependency_scanner.io

import java.io.File

typealias Path = File

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
