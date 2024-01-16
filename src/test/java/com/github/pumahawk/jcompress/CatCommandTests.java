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
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
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
@Import(CatCommandTests.Conf.class)
@MockBean({
	IOService.class,
})
@ComponentScan(basePackageClasses = ArchiveSolver.class)
public class CatCommandTests {
	
	@Autowired
	private IOService ioService;
	
	@Autowired
	private ApplicationContext ac;
	
	private Supplier<CatCommand> catCommand = () -> ac.getBean(CatCommand.class);

	@Test
	public void loadContext() {
	}
	
	@Test
	public void catContentZipTest(@TempDir() File td) throws IOException, URISyntaxException {
		var archive = Path.of(td.getAbsolutePath(), "archive.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(catCommand.get()).execute(archive.toString());

		Scanner sc = new Scanner(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("Hello World!", sc.nextLine());
		assertEquals("Message 2", sc.nextLine());
		assertFalse(sc.hasNext());
		sc.close();
	}
	
	@Test
	public void catContentZipTest_WithGrep(@TempDir() File td) throws IOException, URISyntaxException {
		var archive = Path.of(td.getAbsolutePath(), "archive.zip");
		Files.copy(Path.of(getClass().getResource("/archives/archive.zip").toURI()), archive);
		var out = new ByteArrayOutputStream();
		when(ioService.getSystemOutputStream()).thenReturn(new PrintStream(out));

		new CommandLine(catCommand.get()).execute(
				"--grep", "-2",
				archive.toString());

		Scanner sc = new Scanner(new ByteArrayInputStream(out.toByteArray()));
		assertEquals("Message 2", sc.nextLine());
		assertFalse(sc.hasNext());
		sc.close();
	}
	
	@Configuration
	public static class Conf {
		@Bean
		@Scope("prototype")
		public CatCommand lsCommand() {
			return new CatCommand();
		}
	}
}
