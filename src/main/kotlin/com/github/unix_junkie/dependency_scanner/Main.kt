@file:JvmName("Main")

package com.github.unix_junkie.dependency_scanner

import com.github.unix_junkie.dependency_scanner.ExitCode.ILLEGAL_ARGS
import com.github.unix_junkie.dependency_scanner.ExitCode.PACKAGE_ROOT_NONEXISTENT
import com.github.unix_junkie.dependency_scanner.io.MSysPathConverter
import com.github.unix_junkie.dependency_scanner.io.Path
import com.github.unix_junkie.dependency_scanner.io.fileName
import com.github.unix_junkie.dependency_scanner.io.isChildOf
import com.github.unix_junkie.dependency_scanner.io.safeIsSameFileAs
import com.github.unix_junkie.dependency_scanner.ldd.LddOutputParser
import com.github.unix_junkie.dependency_scanner.ldd.LibraryInterpreter
import com.github.unix_junkie.dependency_scanner.ldd.NotFound
import com.github.unix_junkie.dependency_scanner.ldd.SharedLibraryWithAbsolutePath
import com.github.unix_junkie.dependency_scanner.ldd.UnparseableLddOutputLine
import com.github.unix_junkie.dependency_scanner.ldd.VirtualSharedLibrary
import java.nio.charset.Charset
import kotlin.system.exitProcess

private val CLASS_NAME = {}.javaClass.enclosingClass.name

private val PROCESS_STREAM_CHARSET = Charset.defaultCharset()

fun main(vararg args: String) {
	if (args.size != 1) {
		usage()
		exitProcess(ILLEGAL_ARGS)
	}

	val packageRoot = Path(args[0])

	when {
		packageRoot.isDirectory -> scanPackage(
			packageRoot,
			Options(
				listInternalDependencies = false,
			),
		)

		else -> {
			println("Not a directory: $packageRoot")
			exitProcess(PACKAGE_ROOT_NONEXISTENT)
		}
	}
}

private fun scanPackage(
	packageRoot: Path,
	options: Options,
) {
	withContentDetector {
		val files = scanDirectory(packageRoot).filter { file ->
			file.isExecutable || file.isLibrary
		}.toList()

		val ctx = Context(
			packageRoot,
			fileNames = files.asSequence()
				.map(Path::fileName)
				.filterNotNull()
				.toSet(),
		)

		files.forEach { file ->
			val type = contentDetector.detect(file)
			println("$file -> $type")
			listDependencies(
				file,
				ctx,
				options,
			)
		}
	}
}

/**
 * @return the sequence of regular files which are children of [directory].
 */
private fun scanDirectory(directory: Path): Sequence<Path> {
	check(directory.isDirectory)

	val children = directory.listFiles()

	check(children != null)

	return sequence {
		yieldAll(children.asSequence()
				.filter(Path::isDirectory)
				.sortedBy(Path::getName)
				.flatMap(::scanDirectory))

		yieldAll(children.asSequence()
				.filter(Path::isFile)
				.sortedBy(Path::getName))
	}
}

private fun listDependencies(
	file: Path,
	ctx: Context,
	options: Options,
) {
	val ldd = findInPath("ldd").firstOrNull()
		?: return

	val parser = LddOutputParser(arrayOf(MSysPathConverter()))

	// TODO: clear `LD_PRELOAD` when running `ldd`.
	val lddProcess = ProcessBuilder(ldd.toString(), file.toString()).start()
	lddProcess.outputStream.close()
	lddProcess.inputStream
		.bufferedReader(PROCESS_STREAM_CHARSET)
		.use { stdout ->
			val dependencies = stdout
				.lineSequence()
				.map(parser::parse)
				.filterNot { dependency ->
					dependency is VirtualSharedLibrary
				}
				.filterNot { dependency ->
					dependency is LibraryInterpreter
				}
				.filterNot { dependency ->
					/*
					 * Exclude dependencies pointing to self.
					 */
					dependency is SharedLibraryWithAbsolutePath
							&& dependency.absolutePath.safeIsSameFileAs(file)
				}
				.filterNot { dependency ->
					!options.listInternalDependencies &&
							dependency is SharedLibraryWithAbsolutePath
							&& dependency.absolutePath.isChildOf(ctx.packageRoot)
				}
				.filterNot { dependency ->
					!options.listInternalDependencies
							&& dependency is NotFound
							&& dependency.fileName in ctx.fileNames
				}
				.distinct()
			dependencies.forEach { dependency ->
				when (dependency) {
					is UnparseableLddOutputLine -> System.err.println(
						"Unexpected ldd output for $file: ${dependency.line}"
					)

					else -> println("\t" + dependency)
				}
			}
		}
	val errorOutput = lddProcess.errorStream
		.bufferedReader(PROCESS_STREAM_CHARSET)
		.use { stderr ->
			stderr.readText()
		}
	if (errorOutput.isNotEmpty()) {
		System.err.println(errorOutput)
	}
	val exitCode = lddProcess.waitFor()
	if (exitCode != 0) {
		println("ldd exited with code $exitCode for file $file")
	}
}

private fun usage() {
	println("Usage: $CLASS_NAME [DIRECTORY]")
}

private fun exitProcess(exitCode: ExitCode): Nothing =
	exitProcess(exitCode.ordinal)
