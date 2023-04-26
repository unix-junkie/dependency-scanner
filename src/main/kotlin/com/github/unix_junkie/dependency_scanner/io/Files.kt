@file:JvmName("Files")

package com.github.unix_junkie.dependency_scanner.io

import java.io.File

operator fun File.div(relative: String): File =
		resolve(relative)
