package com.github.pumahawk.jcompress.solvers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarFile;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.ArchiveFile;

@Component
public class TarSolver implements ArchiveSolver {

	@Override
	public boolean support(String type) {
		return "tar".equals(type);
	}

	@Override
	public ArchiveFile createArchiveFile(File file) throws IOException {
		TarFile tar = new TarFile(file);
		return new TarArchiveFile(tar);
	}
	
	public class TarArchiveFile implements ArchiveFile {
		private final TarFile tf;
		
		public TarArchiveFile(TarFile tf) {
			this.tf = tf;
		}

		@Override
		public void close() throws IOException {
			tf.close();
		}

		@Override
		public Enumeration<? extends ArchiveEntry> getEntries() {
			return Collections.enumeration(tf.getEntries());
		}

		@Override
		public InputStream getInputStream(ArchiveEntry entry) {
			try {
				return tf.getInputStream((TarArchiveEntry) entry);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

}
