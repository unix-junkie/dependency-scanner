package com.github.unix_junkie.dependency_scanner.ldd

import com.github.unix_junkie.dependency_scanner.io.MSysPathConverter
import com.github.unix_junkie.dependency_scanner.io.Path
import com.github.unix_junkie.dependency_scanner.io.normalizedAbsolutePath
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

/**
 * @see LddOutputParser
 */
class LddOutputParserTest {
	private val parser = LddOutputParser(arrayOf(MSysPathConverter()))

	@Test
	fun unparseable() {
		assertThat(parser.parse("foobar"))
			.isInstanceOf(UnparseableLddOutputLine::class.java)
	}

	@Test
	fun libraryAtAddress() {
		val parseResult = parser.parse("\tlibc.so.6 => /lib/libc.so.6 (0x7ffc4f4e0000)")
		assertThat(parseResult)
			.isInstanceOf(SharedLibraryWithAbsolutePath::class.java)

		parseResult as SharedLibraryWithAbsolutePath
		assertThat(parseResult.fileName)
			.isRelative
			.isEqualTo(Path("libc.so.6"))
		assertThat(parseResult.absolutePath)
			.isEqualTo(Path("/lib/libc.so.6").normalizedAbsolutePath)
	}

	@Test
	fun libraryAtUnknownAddress() {
		val parseResult = parser.parse("\tlibc.so.6 => /lib/libc.so.6 (?)")
		assertThat(parseResult)
			.isInstanceOf(SharedLibraryWithAbsolutePath::class.java)

		parseResult as SharedLibraryWithAbsolutePath
		assertThat(parseResult.fileName)
			.isRelative
			.isEqualTo(Path("libc.so.6"))
		assertThat(parseResult.absolutePath)
			.isEqualTo(Path("/lib/libc.so.6").normalizedAbsolutePath)
	}

	@Test
	fun unparseableAddress() {
		assertThat(parser.parse("\tlibc.so.6 => /lib/libc.so.6 (unparseable address)"))
			.isInstanceOf(UnparseableLddOutputLine::class.java)
	}

	@Test
	fun notFound() {
		val parseResult = parser.parse("\tlibc.so.6 => not found")
		assertThat(parseResult)
			.isInstanceOf(NotFound::class.java)

		parseResult as NotFound
		assertThat(parseResult.fileName)
			.isRelative
			.isEqualTo(Path("libc.so.6"))
	}

	@Test
	fun virtualSharedLibrary() {
		val parseResult = parser.parse("\tlinux-vdso.so.1 (0x7ffc4f4e0000)")
		assertThat(parseResult)
			.isInstanceOf(VirtualSharedLibrary::class.java)

		parseResult as VirtualSharedLibrary
		assertThat(parseResult.fileName)
			.isRelative
			.isEqualTo(Path("linux-vdso.so.1"))
	}

	@Test
	fun libraryInterpreter() {
		val parseResult = parser.parse("\t/lib64/ld-linux-x86-64.so.2 (0x7ffc4f4e0000)")
		assertThat(parseResult)
			.isInstanceOf(LibraryInterpreter::class.java)

		parseResult as LibraryInterpreter
		assertThat(parseResult.fileName)
			.isEqualTo(Path("/lib64/ld-linux-x86-64.so.2"))
	}
}
