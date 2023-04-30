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
			name.endsWith(".exe", ignoreCase = true)

	val Path.isWindowsLibrary: Boolean
		get() =
			name.endsWith(".dll", ignoreCase = true)

	val Path.isUnixExecutable: Boolean
		get() =
			isUnixExecutableOrLibrary && !isUnixLibrary

	val Path.isUnixLibrary: Boolean
		get() {
			val name = name
			return name.startsWith(UNIX_LIBRARY_NAME_PREFIX)
					&& name.length > UNIX_LIBRARY_NAME_PREFIX.length + UNIX_LIBRARY_NAME_SUFFIX.length
					&& name.subSequence(UNIX_LIBRARY_NAME_PREFIX.length + 1, name.length).contains(UNIX_LIBRARY_NAME_SUFFIX)
					&& isUnixExecutableOrLibrary
		}

	val Path.isUnixExecutableOrLibrary: Boolean
		get() =
			contentDetector.detect(this) == "application/x-sharedlib"

	private companion object {
		private const val UNIX_LIBRARY_NAME_PREFIX = "lib"

		private const val UNIX_LIBRARY_NAME_SUFFIX = ".so"
	}
}
