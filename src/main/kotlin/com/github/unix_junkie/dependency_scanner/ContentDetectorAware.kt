package com.github.unix_junkie.dependency_scanner

import com.github.unix_junkie.dependency_scanner.io.Path
import org.apache.tika.Tika

interface ContentDetectorAware {
	val contentDetector: Tika

	val Path.isExecutable: Boolean
		get() =
			isWindowsExecutable || isUnixExecutable

	val Path.isLibrary: Boolean
		get() =
			isWindowsLibrary || isUnixLibrary

	val Path.isWindowsExecutable: Boolean
		get() =
			name.isWindowsExecutable

	val Path.isWindowsLibrary: Boolean
		get() =
			name.isWindowsLibrary

	val Path.isUnixExecutable: Boolean
		get() =
			isUnixExecutableOrLibrary && !isUnixLibrary

	val Path.isUnixLibrary: Boolean
		get() =
			name.isUnixLibrary && isUnixExecutableOrLibrary

	val Path.isUnixExecutableOrLibrary: Boolean
		get() =
			contentDetector.detect(this) == "application/x-sharedlib"
}
