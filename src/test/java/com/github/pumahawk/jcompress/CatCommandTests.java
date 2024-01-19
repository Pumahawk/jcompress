package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.pumahawk.jcompress.solvers.ArchiveSolver;

@ExtendWith(SpringExtension.class)
@Import(CatCommandTests.Conf.class)
@ComponentScan(basePackageClasses = ArchiveSolver.class)
public class CatCommandTests extends CommandBaseTest<CatCommand> {
	
	@Test
	public void loadContext() {
	}
	
	@Override
	public Class<CatCommand> commandType() {
		return CatCommand.class;
	}
	
	@Test
	public void catContentZipTest() throws IOException, URISyntaxException {
		var archive = getFile("archives/archive.zip");

		run(archive.toString());

		var out = getStdoutLines().iterator();
		assertEquals("Hello World!", out.next());
		assertEquals("Message 2", out.next());
		assertFalse(out.hasNext());
	}
	
	@Test
	public void catContentZipTest_WithDirectory() throws IOException, URISyntaxException {
		var archive = createArchive(ArchiveType.ZIP, ar -> ar
				.put("dir1/message-1.txt", "M1\n")
				.put("dir1/message-2.txt", "M2\n") 
				.put("dir2/message-4.txt", "M3"));

		run(archive.toString());

		var out = getStdoutLines().iterator();
		assertEquals("M1", out.next());
		assertEquals("M2", out.next());
		assertEquals("M3", out.next());
		assertFalse(out.hasNext());
	}
	
	@Test
	public void catContentZipTest_WithGrep() throws IOException, URISyntaxException {
		var archive = getFile("archives/archive.zip");

		run(
				"--grep", "-2",
				archive.toString());

		var out = getStdoutLines().iterator();
		assertEquals("Message 2", out.next());
		assertFalse(out.hasNext());
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
