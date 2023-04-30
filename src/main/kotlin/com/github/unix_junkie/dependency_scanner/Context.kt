package com.github.unix_junkie.dependency_scanner

import com.github.unix_junkie.dependency_scanner.io.Path

data class Context(
	val packageRoot: Path,
	val fileNames: Set<Path>,
)
