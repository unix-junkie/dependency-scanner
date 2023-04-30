@file:JvmName("ContentDetectorUtils")

package com.github.unix_junkie.dependency_scanner

import org.apache.tika.Tika
import org.apache.tika.config.TikaConfig

fun <T> withContentDetector(
	contentDetector: Tika = Tika(TikaConfig.getDefaultConfig()),
	action: ContentDetectorAware.() -> T
): T =
	object : ContentDetectorAware {
		override val contentDetector: Tika = contentDetector
	}.action()
