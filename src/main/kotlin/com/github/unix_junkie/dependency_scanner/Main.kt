@file:JvmName("Main")

package com.github.unix_junkie.dependency_scanner

import com.github.unix_junkie.dependency_scanner.ExitCode.ILLEGAL_ARGS
import com.github.unix_junkie.dependency_scanner.ExitCode.PACKAGE_ROOT_NONEXISTENT
import org.apache.tika.Tika
import org.apache.tika.config.TikaConfig
import java.io.File
import java.nio.charset.Charset
import kotlin.system.exitProcess

private val className = {}.javaClass.enclosingClass.name

private val processStreamCharset = Charset.defaultCharset()

fun main(vararg args: String) {
	if (args.size != 1) {
		usage()
		exitProcess(ILLEGAL_ARGS)
	}

	val packageRoot = File(args[0])

	when {
		packageRoot.isDirectory -> scanPackage(packageRoot)

		else -> {
			println("Not a directory: $packageRoot")
			exitProcess(PACKAGE_ROOT_NONEXISTENT)
		}
	}
}

private fun scanPackage(packageRoot: File) {
	withContentDetector {
		scanDirectory(packageRoot).filter { file ->
			file.isExecutable || file.isLibrary
		}.forEach { file ->
			val type = contentDetector.detect(file)
			println("$file -> $type")
			listDependencies(file)
		}
	}
}

/**
 * @return the sequence of regular files which are children of [directory].
 */
private fun scanDirectory(directory: File): Sequence<File> {
	check(directory.isDirectory)

	val children = directory.listFiles()

	check(children != null)

	return sequence {
		yieldAll(children.asSequence()
				.filter(File::isDirectory)
				.sortedBy(File::getName)
				.flatMap(::scanDirectory))

		yieldAll(children.asSequence()
				.filter(File::isFile)
				.sortedBy(File::getName))
	}
}

private fun listDependencies(file: File) {
	val ldd = findInPath("ldd").firstOrNull()
		?: return

	// TODO: clear `LD_PRELOAD` when running `ldd`.
	val lddProcess = ProcessBuilder(ldd.toString(), file.toString()).start()
	lddProcess.outputStream.close()
	val dependencies = lddProcess.inputStream
		.bufferedReader(processStreamCharset)
		.readText()
	val errorOutput = lddProcess.errorStream
		.bufferedReader(processStreamCharset)
		.readText()
	println(dependencies)
	if (errorOutput.isNotEmpty()) {
		System.err.println(errorOutput)
	}
	val exitCode = lddProcess.waitFor()
	if (exitCode != 0) {
		println("ldd exited with code $exitCode for file $file")
	}
}

private fun usage() {
	println("Usage: $className [DIRECTORY]")
}

private fun exitProcess(exitCode: ExitCode): Nothing =
	exitProcess(exitCode.ordinal)

private fun <T> withContentDetector(
	contentDetector: Tika = Tika(TikaConfig.getDefaultConfig()),
	action: ContentDetectorAware.() -> T
): T =
	object : ContentDetectorAware {
		override val contentDetector: Tika = contentDetector
	}.action()
