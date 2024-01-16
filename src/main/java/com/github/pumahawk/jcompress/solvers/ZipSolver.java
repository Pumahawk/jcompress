package com.github.pumahawk.jcompress.solvers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.ArchiveFile;

@Component
public class ZipSolver implements ArchiveSolver {

	@Override
	public boolean support(String type) {
		return "zip".equals(type);
	}

	@Override
	public ArchiveFile createArchiveFile(File file) throws IOException {
		ZipFile zip = new ZipFile(file);
		return new ZipArchiveFile(zip);
	}
	
	public class ZipArchiveFile implements ArchiveFile {
		private final ZipFile zf;
		
		public ZipArchiveFile(ZipFile zf) {
			this.zf = zf;
		}

		@Override
		public void close() throws IOException {
			zf.close();
		}

		@Override
		public Enumeration<? extends ArchiveEntry> getEntries() {
			return zf.getEntries();
		}

		@Override
		public InputStream getInputStream(ArchiveEntry entry) {
			try {
				return zf.getInputStream((ZipArchiveEntry) entry);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
