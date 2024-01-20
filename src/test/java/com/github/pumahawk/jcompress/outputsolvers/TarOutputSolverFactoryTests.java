package com.github.pumahawk.jcompress.outputsolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.github.pumahawk.jcompress.FSUtils;
import com.github.pumahawk.jcompress.SimpleArchiveBaseTest;

@ExtendWith(SpringExtension.class)
@Import(TarOutputSolverFactory.class)
public class TarOutputSolverFactoryTests extends SimpleArchiveBaseTest {

	@Autowired
	private TarOutputSolverFactory tarOutputSolverFactory;

	@Test
	public void longNameTar_over100() throws IOException {
		// TODO check process using file
		td = Files.createTempDirectory("_junittests").toFile();
		var out = getFile("out.tar");
		var bs = new StringBuilder();
		IntStream.range(0, 100).forEach(n -> bs.append("a"));
		var in = FSUtils.createFile(td, bs + "/message.txt", "1");
		try (var solver = tarOutputSolverFactory.solve(null, out)) {
			var entry = solver.createEntry(in);
			solver.writeEntry(in, new ExtractionEntry(entry));		
		}
		
		try (var ar = readArchive(out)) {
			var it = ar.iterator();
			assertEquals(unixAbsolutePath(in).replaceAll("^C:/", ""), it.next().getName());
			assertEquals("1", ar.contentAsString());
		}
	}
	
}
