package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Collections;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.pumahawk.jcompress.outputsolvers.OutputSolver;
import com.github.pumahawk.jcompress.solvers.ArchiveSolver;

@ExtendWith(SpringExtension.class)
@Import(MkCommandTests.Conf.class)
@ComponentScan(basePackageClasses = {ArchiveSolver.class, OutputSolver.class})
public class MkCommandTests extends CommandBaseTest<MkCommand> {
	
	@Test
	public void loadContext() {
	}
	
	@Override
	public Class<MkCommand> commandType() {
		return MkCommand.class;
	}
	
	@Test
	public void creationTest_ZipSupport() throws URISyntaxException, IOException {

		var td = mkdir("input");
		var out = mkdir("output");

		
		FSUtils.createFile(td, "message-1", "Message 1");
		FSUtils.createFile(td, "message-2", "Message 2");
		
		run(
			"--target", Path.of(out.getAbsolutePath(), "out.zip").toString(),
			"-o", "zip",
			td.getAbsolutePath());
		
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
	public void creationTest_TarOuput() throws URISyntaxException, IOException {

		var td = mkdir("input");
		var out = mkdir("output");

		FSUtils.createFile(td, "message-1", "Message 1");
		FSUtils.createFile(td, "message-2", "Message 2");
		
		run(
			"--target", Path.of(out.getAbsolutePath(), "out.tar").toString(),
			"-o", "tar",
			td.getAbsolutePath());
		
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
