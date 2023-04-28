package com.github.unix_junkie.dependency_scanner.environment

import com.github.unix_junkie.dependency_scanner.isWindows

open class Environment protected constructor() : Map<String, String> by System.getenv() {
	companion object {
		private val INSTANCE = Environment()

		operator fun invoke(): Environment {
			return when {
				isWindows -> WindowsEnvironment
				else -> INSTANCE
			}
		}
	}
}
