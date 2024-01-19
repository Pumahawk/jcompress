package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.pumahawk.jcompress.solvers.ArchiveSolver;

@ExtendWith(SpringExtension.class)
@Import(LsCommandTests.Conf.class)
@ComponentScan(basePackageClasses = ArchiveSolver.class)
public class LsCommandTests extends CommandBaseTest<LsCommand> {
	
	@Override
	public Class<LsCommand> commandType() {
		return LsCommand.class;
	}
	
	@Test
	public void loadContext() {
	}
	
	@Test
	public void listTests() throws IOException, URISyntaxException, ArchiveException {
		var archive = getFile("archives/archive.zip");

		run(archive.toString());

		var out = getStdoutLines().iterator();
		assertEquals("message.txt", out.next());
		assertEquals("message-2.txt", out.next());
		assertFalse(out.hasNext());
	}

	@Test
	public void listTests_MultipleFileInput() throws IOException, URISyntaxException, ArchiveException {
		var archive1 = Path.of(td.getAbsolutePath(), "/archives/archive-1.zip");
		var archive2 = Path.of(td.getAbsolutePath(), "/archives/archive-2.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive1);
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive2);

		run(archive1.toString(), archive2.toString());

		var out = getStdoutLines().iterator();
		assertEquals("message.txt", out.next());
		assertEquals("message-2.txt", out.next());
		assertEquals("message.txt", out.next());
		assertEquals("message-2.txt", out.next());
		assertFalse(out.hasNext());
	}

	@Test
	public void listTests_MultipleFileInputWithFileName() throws IOException, URISyntaxException, ArchiveException {

		var archive1 = copyFile("archives/archive.zip", "out/archive-1.zip");
		var archive2 = copyFile("archives/archive.zip", "out/archive-2.zip");

		run("--file-name", archive1.toString(), archive2.toString());

		var out = getStdoutLines().iterator();
		assertEquals(archive1.toString() + ":message.txt", out.next());
		assertEquals(archive1.toString() + ":message-2.txt", out.next());
		assertEquals(archive2.toString() + ":message.txt", out.next());
		assertEquals(archive2.toString() + ":message-2.txt", out.next());
		assertFalse(out.hasNext());
	}

	@Test
	public void listTests_WithFilter() throws IOException, URISyntaxException, ArchiveException {
		var archive = getFile("archives/archive.zip");

		run(
				"--grep", "-2",
				archive.toString()
			);

		var out = getStdoutLines().iterator();
		assertEquals("message-2.txt", out.next());
		assertFalse(out.hasNext());
	}

	@Test
	public void listTests_WithReplace() throws IOException, URISyntaxException, ArchiveException {
		var archive = getFile("archives/archive.zip");

		run(
				"--rewrite", "2:1",
				archive.toString()
			);

		var out = getStdoutLines().iterator();
		assertEquals("message.txt", out.next());
		assertEquals("message-1.txt", out.next());
		assertFalse(out.hasNext());
	}

	@Test
	public void listTests_TarArchive() throws IOException, URISyntaxException, ArchiveException {
		var archive = getFile("archives/archive.tar");

		run(
				archive.toString()
			);

		var out = getStdoutLines().iterator();
		assertEquals("message.txt", out.next());
		assertEquals("src/test/resources/archives/message-2.txt", out.next());
		assertFalse(out.hasNext());
	}

	@Test
	public void listTests_StreamarArchive() throws IOException, URISyntaxException, ArchiveException {
		var archive = getFile("archives/archive.zip");

		run(
				"--type", "stream",
				archive.toString()
			);

		//listArchiveAction.list(archive.toFile(), Optional.of("stream"), Optional.empty(), Optional.empty());

		var out = getStdoutLines().iterator();
		assertEquals("message.txt", out.next());
		assertEquals("message-2.txt", out.next());
		assertFalse(out.hasNext());
	}
	
	public static Stream<Arguments> processRegexInputTests() {
		return Stream.of(
			Arguments.of("2:1", "2", "1"),
			Arguments.of("/temp:1", "/temp", "1"),
			Arguments.of("/temp\\:echo:word", "/temp:echo", "word"),
			Arguments.of("/temp\\:echo\\:2:word\\:word2", "/temp:echo:2", "word:word2")
		);
	}
	
	@ParameterizedTest
	@MethodSource
	public void processRegexInputTests(String regex, String v1, String v2) {
		var r = command.get().rexKey(regex);
		assertEquals(v1, r[0]);
		assertEquals(v2, r[1]);
	}

	@Configuration
	public static class Conf {
		@Bean
		@Scope("prototype")
		public LsCommand lsCommand() {
			return new LsCommand();
		}
	}
}
