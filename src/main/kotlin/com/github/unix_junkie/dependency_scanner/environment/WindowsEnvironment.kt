package com.github.unix_junkie.dependency_scanner.environment

internal object WindowsEnvironment : Environment() {
	override fun get(key: String) = entries.firstOrNull { (entryKey) ->
		entryKey.equals(key, ignoreCase = true)
	}?.value

	override fun containsKey(key: String) =
		this[key] != null
}
