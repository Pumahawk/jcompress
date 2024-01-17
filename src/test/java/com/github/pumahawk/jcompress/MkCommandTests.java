package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.function.Supplier;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.pumahawk.jcompress.outputsolvers.OutputSolver;
import com.github.pumahawk.jcompress.solvers.ArchiveSolver;

import picocli.CommandLine;

@ExtendWith(SpringExtension.class)
@Import(MkCommandTests.Conf.class)
@ComponentScan(basePackageClasses = {ArchiveSolver.class, OutputSolver.class})
public class MkCommandTests {
	
	@Autowired
	private ApplicationContext ac;
	
	private Supplier<MkCommand> action = () -> ac.getBean(MkCommand.class);
	
	@Test
	public void loadContext() {
	}
	
	@Test
	public void creationTest_ZipSupport(@TempDir() File td, @TempDir() File out) throws URISyntaxException, IOException {
		createTmpFile(td, "message-1", "Message 1");
		createTmpFile(td, "message-2", "Message 2");
		
		Integer code = new CommandLine(action.get()).execute(
			"--target", Path.of(out.getAbsolutePath(), "out.zip").toString(),
			"-o", "zip",
			td.getAbsolutePath());
		
		assertEquals(0, code);

		try (var zf = new ZipFile(Path.of(out.getAbsolutePath(), "out.zip").toFile())) {
			var es = zf.getEntries();
			ZipArchiveEntry entry = es.nextElement();
			assertEquals(td.getAbsolutePath() + "/", entry.getName());
			entry = es.nextElement();
			assertEquals(td.getAbsolutePath().replace('\\', '/') + "/message-1", entry.getName());
			var content = new ByteArrayOutputStream();
			IOUtils.copy(zf.getInputStream(entry), content);
			assertEquals("Message 1", content.toString());
		}
		
	}
	
	@Test
	public void creationTest_TarOuput(@TempDir() File td, @TempDir() File out) throws URISyntaxException, IOException {
		createTmpFile(td, "message-1", "Message 1");
		createTmpFile(td, "message-2", "Message 2");
		
		Integer code = new CommandLine(action.get()).execute(
			"--target", Path.of(out.getAbsolutePath(), "out.tar").toString(),
			"-o", "tar",
			td.getAbsolutePath());
		
		assertEquals(0, code);

		try (var zf = new TarFile(Path.of(out.getAbsolutePath(), "out.tar").toFile())) {
			var es = Collections.enumeration(zf.getEntries());
			TarArchiveEntry entry = es.nextElement();
			assertEquals(td.getAbsolutePath().replace('\\', '/').substring(3) + "/", entry.getName());
			entry = es.nextElement();
			assertEquals(td.getAbsolutePath().replace('\\', '/').substring(3) + "/message-1", entry.getName());
			var content = new ByteArrayOutputStream();
			IOUtils.copy(zf.getInputStream(entry), content);
			assertEquals("Message 1", content.toString());
		}
		
	}
	
	private void createTmpFile(File parent, String name, String content) throws FileNotFoundException, IOException {
		try (var out = new PrintStream(Path.of(parent.getAbsolutePath(), name).toFile())) {
			out.print(content);
		}
	}

	@Configuration
	@Import({
		IOService.class,
	})
	public static class Conf {
		@Bean
		@Scope("prototype")
		public MkCommand mkCommand() {
			return new MkCommand();
		}
	}

}
