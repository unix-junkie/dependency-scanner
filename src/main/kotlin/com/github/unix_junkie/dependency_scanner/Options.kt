package com.github.unix_junkie.dependency_scanner

/**
 * @param listInternalDependencies whether dependencies which are themselves
 *   internal with respect to the package root should also be listed.
 */
data class Options(
	val listInternalDependencies: Boolean,
	val mergeDependencies: Boolean,
)
