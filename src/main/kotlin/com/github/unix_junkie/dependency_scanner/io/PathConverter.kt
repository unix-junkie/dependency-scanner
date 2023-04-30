package com.github.unix_junkie.dependency_scanner.io

interface PathConverter {
	val isEnabled: Boolean

	fun convert(rawPath: String): Path
}
