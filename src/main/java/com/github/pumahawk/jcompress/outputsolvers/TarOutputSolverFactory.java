package com.github.pumahawk.jcompress.outputsolvers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.ArchiveFile;
import com.github.pumahawk.jcompress.IOService;

@Component
public class TarOutputSolverFactory implements OutputSolverFactory {

	@Autowired
	private IOService ioService;

	@Override
	public boolean support(String type) {
		return "tar".equals(type);
	}

	
	 @Override
	public TarOutputSolver solve(String type, File output) {
		try {
			var outf = ioService.getFileOutputStream(output);
			TarArchiveOutputStream out = new ArchiveStreamFactory().createArchiveOutputStream("tar", outf);
			out.setLongFileMode(TarArchiveOutputStream.LONGFILE_GNU);
			return new TarOutputSolver(outf, out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public class TarOutputSolver implements OutputSolver {

		private final OutputStream outf;
		private final TarArchiveOutputStream out;

		public TarOutputSolver(OutputStream outf, TarArchiveOutputStream out) {
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
			ArchiveEntry entry = exentry.getEntry();
			try {
				TarArchiveEntry en = new TarArchiveEntry(exentry.getName());
				en.setSize(entry.getSize());
				en.setLastModifiedTime(FileTime.from(entry.getLastModifiedDate().toInstant()));
				out.putArchiveEntry(en);
				IOUtils.copy(ar.getInputStream(entry), out);
				out.closeArchiveEntry();
			} catch (IOException e) {
				throw new RuntimeException("Unable to write on new file", e);
			}
		}
		
		@Override
		public ArchiveEntry createEntry(String name) {
			return new TarArchiveEntry(name);
		}
		
		@Override
		public TarArchiveEntry createEntry(File file) {
			return new TarArchiveEntry(file);
		}

		@Override
		public void writeEntry(File file, ExtractionEntry exentry) {
			
			if (file.isDirectory()) {
				
				TarArchiveEntry en = new TarArchiveEntry(file, exentry.getName());
				try {
					out.putArchiveEntry(en);
					out.closeArchiveEntry();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				try (var in = new BufferedInputStream(new FileInputStream(file))) {
					TarArchiveEntry en = new TarArchiveEntry(file, exentry.getName());
					out.putArchiveEntry(en);
					IOUtils.copy(in, out);
					out.closeArchiveEntry();
				} catch (IOException e) {
					throw new RuntimeException("Unable to write on new file", e);
				}
			}
		}
	}
}

