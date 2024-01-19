package com.github.pumahawk.jcompress.outputsolvers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.ArchiveFile;

@Component
public class DirOutputSolverFactory implements OutputSolverFactory {

	@Override
	public boolean support(String type) {
		return Arrays.asList("dir")
				.contains(type);
	}

	@Override
	public OutputSolver solve(String type, File output) {
		return new DirOutputSolver(output);
	}
	
	public class DirOutputSolver implements OutputSolver {
		
		private File output;
		
		public DirOutputSolver(File output) {
			this.output = output;
		}

		@Override
		public void close() throws IOException {
		}
		
		@Override
		public ArchiveEntry createEntry(String name) {
			// TODO Auto-generated method stub
			throw new RuntimeException("Unsupported");
		}
		
		@Override
		public ArchiveEntry createEntry(File file) {
			// TODO Auto-generated method stub
			throw new RuntimeException("Unsupported");
		}
		
		@Override
		public void writeEntry(File file, ExtractionEntry exentry) {
			// TODO Auto-generated method stub
			throw new RuntimeException("Unsupported");
		}

		@Override
		public void writeEntry(ArchiveFile ar, ExtractionEntry exentry) {
			ArchiveEntry entry = exentry.getEntry();
			if (output.isDirectory()) {
				File f = Path.of(output.getAbsolutePath(), exentry.getName()).toFile();
				if (!f.getParentFile().exists()) {
					f.getParentFile().mkdirs();
				}
				if (entry.isDirectory()) {
					if (!f.exists()) {
						f.mkdir();
					}
				} else {
					try (FileOutputStream fout = new FileOutputStream(f)) {
						IOUtils.copy(ar.getInputStream(entry), fout);
					} catch (Exception e) {
						throw new RuntimeException("Unable to get fileoutputstream", e);
					}
				}
			} else {
				throw new RuntimeException("invalid output directory " + output.getAbsolutePath());
			}
		}

	}

}
