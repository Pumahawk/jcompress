package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
@Import(ExCommandTests.Conf.class)
@ComponentScan(basePackageClasses = {ArchiveSolver.class, OutputSolver.class})
public class ExCommandTests extends CommandBaseTest<ExCommand> {
	
	@Override
	public Class<ExCommand> commandType() {
		return ExCommand.class;
	}
	
	@Test
	public void loadContext() {
	}
	
	@Test
	public void extractionTest() throws URISyntaxException {

		var td = mkdir("dir");
		var archive = getFile("archives/archive.zip");

		run(
				"--output-directory",  td.getAbsolutePath(),
				archive.getAbsolutePath());

		var files = lsr(td).map(File::getName).sorted().toList();
		assertEquals("message-2.txt", files.get(0));
		assertEquals("message.txt", files.get(1));
		assertEquals(2, files.size());
	}
	
	@Test
	public void extractionTest_ZipOut() throws URISyntaxException, IOException {

		var archive = getFile("archives/archive.zip");
		var out = getFile("dir/out.zip");

		run(
				"--target", out.getAbsolutePath(),
				"--output-type", "zip",
				"--grep", "message-2",
				archive.getAbsolutePath());
		
		try (var zf = readArchive(out)) {
			var it = zf.iterator();
			assertEquals("message-2.txt", it.next().getName());
			assertEquals("Message 2\n", zf.contentAsString());
		}
	}
	
	@Test
	public void extractionTest_ConversionZipToTar() throws URISyntaxException, IOException {

		var td = mkdir("dir");
		var archive = getFile("archives/archive.tar");
		var out = getFile("dir/out.tar");

		run(
				"--target", out.getAbsolutePath(),
				"--output-type", "tar",
				"--grep", "message.txt",
				archive.getAbsolutePath());
		
		var files = lsr(td).map(File::getName).sorted().toList();
		assertEquals(1, files.size());
		assertEquals("out.tar", files.get(0));
		
		try (var ar = readArchive(out)) {
			var it = ar.iterator();

			var en = it.next();
			assertEquals("message.txt", en.getName());
			assertEquals("Hello World!\n", ar.contentAsString());
			assertFalse(it.hasNext());
		}
	}
	
	@Test
	public void extractionTest_ConversionZipToTarWithRewrite() throws URISyntaxException, IOException {

		var td = mkdir("dir");
		var archive = getFile("archives/archive.tar");
		var out = getFile("dir/out.tar");

		run(
				"--target", out.getAbsolutePath(),
				"--output-type", "tar",
				"--grep", "message.txt",
				"--rewrite", "^:dir/",
				archive.getAbsolutePath());
		
		var files = lsr(td).map(File::getName).sorted().toList();
		assertEquals(1, files.size());
		assertEquals("out.tar", files.get(0));
		
		try (var ar = readArchive(out)) {
			var it = ar.iterator();
			assertEquals("dir/message.txt", it.next().getName());
			assertEquals("Hello World!\n", ar.contentAsString());
			assertFalse(it.hasNext());
		}
		
	}
	
	@Test
	@EnabledOnOs({OS.WINDOWS})
	public void extractionTest_ArchiveWithMultipleDirectory() throws IOException {
		File archive = createArchive(ArchiveType.ZIP, ar -> ar
				.put("NomeFile-0.txt", "Content 0")
				.put("dir/")
				.put("dir/NomeFile-1.txt", "Content 1")
				.put("dir/dir2/dir3/")
				.put("dir/"));
		var out = mkdir("out");
		
		run(
				"--target", out.getAbsolutePath(),
				archive.getAbsolutePath()
		);
		
		
		var it = allFileRecursive(out).map(File::getPath).iterator();
		assertEquals("", min(out, it.next()));
		assertEquals("\\dir", min(out, it.next()));
		assertEquals("\\dir\\dir2", min(out, it.next()));
		assertEquals("\\dir\\dir2\\dir3", min(out, it.next()));
		assertEquals("\\dir\\NomeFile-1.txt", min(out, it.next()));
		assertEquals("\\NomeFile-0.txt", min(out, it.next()));
	}
	
	@Test
	public void extractionTest_IsADirectory() throws IOException {
		File archive = createArchive(ArchiveType.ZIP, ar -> ar
				.put("NomeFile-0.txt", "Content 0")
				.put("dir/")
				.put("dir/NomeFile-1.txt", "Content 1")
				.put("dir/dir2/dir3/")
				.put("dir/"));
		var out = FSUtils.createFile(td, "out", "Fake dir");
		
		run(1,
				"--target", out.getAbsolutePath(),
				archive.getAbsolutePath()
		);
		
	}
	
	@Test
	public void useDefaultOutput() throws IOException {
		var archive = createArchive(ArchiveType.CPIO, ar -> ar.put("first", "Message..."));
		var out = getFile("out/file.cpio");
		
		run(
				"--target", out.getAbsolutePath(),
				"--type", "stream",
				"--out-type", "cpio",
				archive.getAbsolutePath()
		);
		
		try(var ar = readArchive(out)) {
			var it = ar.iterator();
			assertEquals("first", it.next().getName());
		}
	}
	
	public String min(File root, String path) {
		return path.substring(root.getPath().length());
	}

	@Configuration
	public static class Conf {
		@Bean
		@Scope("prototype")
		public ExCommand exCommand() {
			return new ExCommand();
		}
	}

}
