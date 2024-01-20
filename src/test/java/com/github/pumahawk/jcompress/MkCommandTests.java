package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
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
	@EnabledOnOs({OS.WINDOWS})
	public void creationTest_ZipSupport() throws URISyntaxException, IOException {

		var td = mkdir("input");
		var out = mkdir("output");

		
		FSUtils.createFile(td, "message-1", "Message 1");
		FSUtils.createFile(td, "message-2", "Message 2");
		FSUtils.createFile(td, "dir/message-3", "Message 3");
		
		run(
			"--target", Path.of(out.getAbsolutePath(), "out.zip").toString(),
			"-o", "zip",
			td.getAbsolutePath());
		
		try (var ar = readArchive(getFile("output/out.zip"))) {
			var it = ar.iterator();
			assertEquals(unixAbsolutePath(td) + "/", it.next().getName());
			assertEquals(unixAbsolutePath(td) + "/dir/", it.next().getName());
			assertEquals(unixAbsolutePath(td) + "/dir/message-3", it.next().getName());
			assertEquals(unixAbsolutePath(td) + "/message-1", it.next().getName());
			assertEquals("Message 1", ar.contentAsString());
			assertEquals(unixAbsolutePath(td) + "/message-2", it.next().getName());
			assertEquals("Message 2", ar.contentAsString());
		}
		
	}
	
	@Test
	@EnabledOnOs({OS.WINDOWS})
	public void creationTest_TarOuput() throws URISyntaxException, IOException {

		var td = mkdir("input");
		var out = mkdir("output");

		FSUtils.createFile(td, "message-1", "Message 1");
		FSUtils.createFile(td, "message-2", "Message 2");
		
		run(
			"--target", Path.of(out.getAbsolutePath(), "out.tar").toString(),
			"-o", "tar",
			td.getAbsolutePath());
		
		try (var zf = readArchive("output/out.tar")) {
			var it = zf.iterator();
			assertEquals(unixAbsolutePath(td).substring(3) + "/", it.next().getName());
			assertEquals(unixAbsolutePath(td).substring(3) + "/message-1", it.next().getName());
			assertEquals("Message 1", zf.contentAsString());
			assertEquals(unixAbsolutePath(td).substring(3) + "/message-2", it.next().getName());
			assertEquals("Message 2", zf.contentAsString());
		}
		
	}
	
	@Test
	@EnabledOnOs({OS.WINDOWS})
	public void creationTest_7zOuput() throws URISyntaxException, IOException {

		var td = mkdir("input");
		var out = mkdir("output");

		FSUtils.createFile(td, "message-1", "Message 1");
		FSUtils.createFile(td, "message-2", "Message 2");
		
		run(
			"--target", Path.of(out.getAbsolutePath(), "out").toString(),
			"-o", "cpio",
			td.getAbsolutePath());
		
		try (var zf = readArchive("output/out")) {
			var it = zf.iterator();
			assertEquals(td + "", it.next().getName());
			assertEquals(td + "\\message-1", it.next().getName());
			assertEquals("Message 1", zf.contentAsString());
			assertEquals(td + "\\message-2", it.next().getName());
			assertEquals("Message 2", zf.contentAsString());
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
