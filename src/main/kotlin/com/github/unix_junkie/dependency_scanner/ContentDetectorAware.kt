package com.github.unix_junkie.dependency_scanner

import org.apache.tika.Tika
import java.io.File

interface ContentDetectorAware {
	val contentDetector: Tika

	val File.isExecutable: Boolean
		get() =
			isWindowsExecutable || isUnixExecutable

	val File.isLibrary: Boolean
		get() =
			isWindowsLibrary || isUnixLibrary

	val File.isWindowsExecutable: Boolean
		get() =
			name.endsWith(".exe", ignoreCase = true)

	val File.isWindowsLibrary: Boolean
		get() =
			name.endsWith(".dll", ignoreCase = true)

	val File.isUnixExecutable: Boolean
		get() =
			isUnixExecutableOrLibrary && !isUnixLibrary

	val File.isUnixLibrary: Boolean
		get() {
			val name = name
			return name.startsWith(UNIX_LIBRARY_NAME_PREFIX)
					&& name.length > UNIX_LIBRARY_NAME_PREFIX.length + UNIX_LIBRARY_NAME_SUFFIX.length
					&& name.subSequence(UNIX_LIBRARY_NAME_PREFIX.length + 1, name.length).contains(UNIX_LIBRARY_NAME_SUFFIX)
					&& isUnixExecutableOrLibrary
		}

	val File.isUnixExecutableOrLibrary: Boolean
		get() =
			contentDetector.detect(this) == "application/x-sharedlib"

	private companion object {
		private const val UNIX_LIBRARY_NAME_PREFIX = "lib"

		private const val UNIX_LIBRARY_NAME_SUFFIX = ".so"
	}
}
