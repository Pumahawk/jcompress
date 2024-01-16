package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.compress.utils.IOUtils;
import org.assertj.core.util.Arrays;
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

import com.github.pumahawk.jcompress.outputsolvers.OutputSolver;
import com.github.pumahawk.jcompress.solvers.ArchiveSolver;

import picocli.CommandLine;

@ExtendWith(SpringExtension.class)
@Import(ExCommandTests.Conf.class)
@MockBean({
	IOService.class,
})
@ComponentScan(basePackageClasses = {ArchiveSolver.class, OutputSolver.class})
public class ExCommandTests {
	
	@Autowired
	private IOService ioService;
	
	@Autowired
	private ApplicationContext ac;
	
	private Supplier<ExCommand> action = () -> ac.getBean(ExCommand.class);
	
	@Test
	public void loadContext() {
	}
	
	@Test
	public void extractionTest(@TempDir() File td) throws URISyntaxException {

		var archive = Path.of(getClass().getResource("/archives/archive.zip").toURI()).toFile();

		new CommandLine(action.get()).execute(
				"--output-directory", td.getAbsolutePath(),
				archive.getAbsolutePath());

		var files = Arrays.asList(td.listFiles()).stream().map(f -> (File)f).map(File::getName).sorted().collect(Collectors.toList()).toArray(new String[2]);
		assertEquals("message-2.txt", files[0]);
		assertEquals("message.txt", files[1]);
		assertEquals(2, files.length);
	}
	
	@Test
	public void extractionTest_ZipOut(@TempDir() File td) throws URISyntaxException, IOException {

		var archive = Path.of(getClass().getResource("/archives/archive.zip").toURI()).toFile();
		var out = Path.of(td.getAbsolutePath(), "out.zip").toFile();

		Integer code = new CommandLine(action.get()).execute(
				"--target", out.getAbsolutePath(),
				"--output-type", "zip",
				"--grep", "message-2",
				archive.getAbsolutePath());
		assertEquals(0, code);
		
		var files = Arrays.asList(td.listFiles()).stream().map(f -> (File)f).map(File::getName).sorted().collect(Collectors.toList()).toArray(new String[1]);
		assertEquals("out.zip", files[0]);
		assertEquals(1, td.listFiles().length);
		
		try (ZipFile zf = new ZipFile(out)) {
			var ens =  zf.getEntries();
			ZipArchiveEntry en = ens.nextElement();
			var outs = new ByteArrayOutputStream();
			IOUtils.copy(zf.getInputStream(en), outs);
			assertEquals("Message 2\n", outs.toString());
			assertFalse(ens.hasMoreElements());
		}
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
