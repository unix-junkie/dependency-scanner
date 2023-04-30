package com.github.unix_junkie.dependency_scanner.io

import com.github.unix_junkie.dependency_scanner.isWindows
import java.io.File.separatorChar

class MSysPathConverter : PathConverter {
	override val isEnabled: Boolean
		get() =
			isWindows

	override fun convert(rawPath: String): Path =
		when {
			rawPath.isMSysPath -> Path(
				rawPath.driveLetter.uppercase()
						+ ":"
						+ rawPath.substring(2).replace('/', separatorChar)
			)

			else -> Path(rawPath)
		}

	private val String.isMSysPath: Boolean
		get() =
			length >= 3
					&& this[0] == '/'
					&& driveLetter.isDriveLetter
					&& this[2] == '/'

	private val String.driveLetter: Char
		get() =
			this[1]

	private val Char.isDriveLetter: Boolean
		get() =
			this in 'A'..'Z' || this in 'a'..'z'
}
