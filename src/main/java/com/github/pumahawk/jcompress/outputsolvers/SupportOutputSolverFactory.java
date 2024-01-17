package com.github.pumahawk.jcompress.outputsolvers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.ArchiveFile;
import com.github.pumahawk.jcompress.IOService;

@Component
public class SupportOutputSolverFactory implements OutputSolverFactory {

	@Autowired
	private IOService ioService;

	@Override
	public boolean support(String type) {
		return !Arrays.asList("dir")
			.contains(type);
	}

	
	 @Override
	public OutputSolver solve(String type, File output) {
		try {
			var outf = ioService.getFileOutputStream(output);
			ArchiveOutputStream<ArchiveEntry> out = new ArchiveStreamFactory().createArchiveOutputStream(type, outf);
			return new SupportOutputSolver(outf, out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public class SupportOutputSolver implements OutputSolver {

		private final OutputStream outf;
		private final ArchiveOutputStream<ArchiveEntry> out;

		public SupportOutputSolver(OutputStream outf, ArchiveOutputStream<ArchiveEntry> out) {
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

