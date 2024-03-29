package com.github.pumahawk.jcompress.outputsolvers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
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
		return false;
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
		public void writeEntry(ArchiveFile ar, ExtractionEntry exentry) {
			// Unable to use edit entry
			ArchiveEntry entry = exentry.getEntry();
			try {
				out.putArchiveEntry(entry);
				IOUtils.copy(ar.getInputStream(entry), out);
				out.closeArchiveEntry();
			} catch (IOException e) {
				throw new RuntimeException("Unable to write on new file", e);
			}
		}

		@Override
		public void writeEntry(File file, ExtractionEntry exentry) {
			// Unable to use edit entry
			ArchiveEntry entry = exentry.getEntry();
			if (file.isDirectory()) {
				try  {
					out.putArchiveEntry(entry);
					out.closeArchiveEntry();
				} catch (IOException e) {
					throw new RuntimeException("Unable to write on new file", e);
				}
			} else {
				try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
					out.putArchiveEntry(entry);
					IOUtils.copy(in, out);
					out.closeArchiveEntry();
				} catch (IOException e) {
					throw new RuntimeException("Unable to write on new file", e);
				}
			}
		}
		
		@Override
		public ArchiveEntry createEntry(String name) {
			try {
				return out.createArchiveEntry(new File(name), name);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		@Override
		public ArchiveEntry createEntry(File file) {
			try {
				return out.createArchiveEntry(file, file.getPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}

