package com.github.pumahawk.jcompress.outputsolvers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.attribute.FileTime;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.ArchiveFile;
import com.github.pumahawk.jcompress.IOService;

@Component
public class ZipOutputSolverFactory implements OutputSolverFactory {

	@Autowired
	private IOService ioService;

	@Override
	public boolean support(String type) {
		return "zip".equals(type);
	}

	
	 @Override
	public OutputSolver solve(String type, File output) {
		try {
			var outf = ioService.getFileOutputStream(output);
			ZipArchiveOutputStream out = new ArchiveStreamFactory().createArchiveOutputStream("zip", outf);
			return new ZipOutputSolver(outf, out);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public class ZipOutputSolver implements OutputSolver {

		private final OutputStream outf;
		private final ZipArchiveOutputStream out;

		public ZipOutputSolver(OutputStream outf, ZipArchiveOutputStream out) {
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
				ZipArchiveEntry en = new ZipArchiveEntry(exentry.getName());
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
			return new ZipArchiveEntry(name);
		}

		@Override
		public ArchiveEntry createEntry(File file) {
			return new ZipArchiveEntry(file, file.getPath());
		}

		@Override
		public void writeEntry(File file, ExtractionEntry exentry) {
			if (file.isDirectory()) {
				ZipArchiveEntry en = new ZipArchiveEntry(file, exentry.getName());
				try {
					out.putArchiveEntry(en);
					out.closeArchiveEntry();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				try (var in = new BufferedInputStream(new FileInputStream(file))) {
					ZipArchiveEntry en = new ZipArchiveEntry(file, exentry.getName());
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

