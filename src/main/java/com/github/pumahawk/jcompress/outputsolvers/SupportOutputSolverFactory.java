package com.github.pumahawk.jcompress.outputsolvers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.ArchiveFile;

@Component
public class SupportOutputSolverFactory implements OutputSolverFactory {


	@Override
	public boolean support(String type) {
		return !Arrays.asList("dir")
			.contains(type);
	}

	
	 @Override
	public OutputSolver solve(String type, File output) {
		try {
			var outf = new FileOutputStream(output);
			ArchiveOutputStream<ArchiveEntry> out = new ArchiveStreamFactory().createArchiveOutputStream(type, outf);
			return new SupportOutputSolver(outf, out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public class SupportOutputSolver implements OutputSolver {

		private final FileOutputStream outf;
		private final ArchiveOutputStream<ArchiveEntry> out;

		public SupportOutputSolver(FileOutputStream outf, ArchiveOutputStream<ArchiveEntry> out) {
			this.outf = outf;
			this.out = out;
		}

		@Override
		public void close() throws IOException {
			if (outf != null) {
				out.finish();
				outf.close();
			}
			
		}

		@Override
		public void writeEntry(ArchiveFile ar, ArchiveEntry entry) {
			try {
				out.putArchiveEntry(entry);
				IOUtils.copy(ar.getInputStream(entry), out);
				out.closeArchiveEntry();
			} catch (IOException e) {
				throw new RuntimeException("Unable to write on new file", e);
			}
		}
	}
}

