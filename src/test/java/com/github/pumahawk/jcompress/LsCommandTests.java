package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.pumahawk.jcompress.solvers.ArchiveSolver;

import picocli.CommandLine;

@ExtendWith(SpringExtension.class)
@Import(LsCommandTests.Conf.class)
@MockBean({
	IOService.class,
})
@ComponentScan(basePackageClasses = ArchiveSolver.class)
public class LsCommandTests {
	
	@Autowired
	private IOService ioService;
	
	@Autowired
	private ApplicationContext ac;
	
	private Supplier<LsCommand> listArchiveAction = () -> ac.getBean(LsCommand.class);
	
	@Test
	public void loadContext() {
	}

	@BeforeEach
	public void mockIoService() throws FileNotFoundException {
		when(ioService.getFileInputStream(any())).thenCallRealMethod();
		when(ioService.getFileOutputStream(any())).thenCallRealMethod();
	}
	
	@Test
	public void listTests(@TempDir() File td) throws IOException, URISyntaxException, ArchiveException {
		var archive = Path.of(td.getAbsolutePath(), "archive.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(listArchiveAction.get()).execute(archive.toString());

		Scanner sc = new Scanner(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("message.txt", sc.nextLine());
		assertEquals("message-2.txt", sc.nextLine());
		assertFalse(sc.hasNext());
		sc.close();
	}

	@Test
	public void listTests_MultipleFileInput(@TempDir() File td) throws IOException, URISyntaxException, ArchiveException {
		var archive1 = Path.of(td.getAbsolutePath(), "archive-1.zip");
		var archive2 = Path.of(td.getAbsolutePath(), "archive-2.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive1);
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive2);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(listArchiveAction.get()).execute(archive1.toString(), archive2.toString());

		Scanner sc = new Scanner(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("message.txt", sc.nextLine());
		assertEquals("message-2.txt", sc.nextLine());
		assertEquals("message.txt", sc.nextLine());
		assertEquals("message-2.txt", sc.nextLine());
		assertFalse(sc.hasNext());
		sc.close();
	}

	@Test
	public void listTests_MultipleFileInputWithFileName(@TempDir() File td) throws IOException, URISyntaxException, ArchiveException {
		var archive1 = Path.of(td.getAbsolutePath(), "archive-1.zip");
		var archive2 = Path.of(td.getAbsolutePath(), "archive-2.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive1);
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive2);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(listArchiveAction.get()).execute("--file-name", archive1.toString(), archive2.toString());

		Scanner sc = new Scanner(new ByteArrayInputStream(out.toByteArray()));
		assertEquals(archive1.toString() + ":message.txt", sc.nextLine());
		assertEquals(archive1.toString() + ":message-2.txt", sc.nextLine());
		assertEquals(archive2.toString() + ":message.txt", sc.nextLine());
		assertEquals(archive2.toString() + ":message-2.txt", sc.nextLine());
		assertFalse(sc.hasNext());
		sc.close();
	}

	@Test
	public void listTests_WithFilter(@TempDir() File td) throws IOException, URISyntaxException, ArchiveException {
		var archive = Path.of(td.getAbsolutePath(), "archive.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(listArchiveAction.get()).execute(
				"--grep", "-2",
				archive.toString()
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

		new CommandLine(listArchiveAction.get()).execute(
				"--rewrite", "2:1",
				archive.toString()
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

		new CommandLine(listArchiveAction.get()).execute(
				archive.toString()
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

		new CommandLine(listArchiveAction.get()).execute(
				"--type", "stream",
				archive.toString()
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
		var r = listArchiveAction.get().rexKey(regex);
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
