package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.pumahawk.jcompress.solvers.ArchiveSolver;

import picocli.CommandLine;

@ExtendWith(SpringExtension.class)
@Import({
	ListArchiveAction.class,
})
@MockBean({
	IOService.class,
})
@ComponentScan(basePackageClasses = ArchiveSolver.class)
public class ListArchiveActionTests {
	
	@Autowired
	private IOService ioService;
	
	@Autowired
	private ListArchiveAction listArchiveAction;
	
	@Test
	public void loadContext() {
	}

	@Test
	public void listTests(@TempDir() File td) throws IOException, URISyntaxException, ArchiveException {
		var archive = Path.of(td.getAbsolutePath(), "archive.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(listArchiveAction).execute("-f", archive.toString());

		Scanner sc = new Scanner(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("message.txt", sc.nextLine());
		assertEquals("message-2.txt", sc.nextLine());
		assertFalse(sc.hasNext());
		sc.close();
	}

	@Test
	public void listTests_WithFilter(@TempDir() File td) throws IOException, URISyntaxException, ArchiveException {
		var archive = Path.of(td.getAbsolutePath(), "archive.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(listArchiveAction).execute(
				"-f", archive.toString(),
				"--match", ".*2.*"
			);

		Scanner sc = new Scanner(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("message-2.txt", sc.nextLine());
		assertFalse(sc.hasNext());
		sc.close();
	}

	@Test
	public void listTests_WithReplace(@TempDir() File td) throws IOException, URISyntaxException, ArchiveException {
		var archive = Path.of(td.getAbsolutePath(), "archive.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(listArchiveAction).execute(
				"-f", archive.toString(),
				"--rewrite", "2:1"
			);

		Scanner sc = new Scanner(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("message.txt", sc.nextLine());
		assertEquals("message-1.txt", sc.nextLine());
		assertFalse(sc.hasNext());
		sc.close();
	}

	@Test
	public void listTests_TarArchive(@TempDir() File td) throws IOException, URISyntaxException, ArchiveException {
		var archive = Path.of(td.getAbsolutePath(), "archive.tar");
		Files.copy(Path.of(getClass().getResource("/archives/archive.tar").toURI()), archive);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(listArchiveAction).execute(
				"-f", archive.toString()
			);

		Scanner sc = new Scanner(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("message.txt", sc.nextLine());
		assertEquals("src/test/resources/archives/message-2.txt", sc.nextLine());
		assertFalse(sc.hasNext());
		sc.close();
	}

	@Test
	public void listTests_StreamarArchive(@TempDir() File td) throws IOException, URISyntaxException, ArchiveException {
		var archive = Path.of(td.getAbsolutePath(), "archive.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(listArchiveAction).execute(
				"-f", archive.toString(),
				"--type", "stream"
			);

		//listArchiveAction.list(archive.toFile(), Optional.of("stream"), Optional.empty(), Optional.empty());

		Scanner sc = new Scanner(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("message.txt", sc.nextLine());
		assertEquals("message-2.txt", sc.nextLine());
		assertFalse(sc.hasNext());
		sc.close();
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
		var r = listArchiveAction.rexKey(regex);
		assertEquals(v1, r[0]);
		assertEquals(v2, r[1]);
	}

}
