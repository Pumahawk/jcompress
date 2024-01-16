package com.github.pumahawk.jcompress.solvers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.ArchiveFile;

@Component
public class StreamSolver implements ArchiveSolver {

	@Override
	public boolean support(String type) {
		return "stream".equals(type);
	}

	@Override
	public ArchiveFile createArchiveFile(File file) throws ArchiveException, FileNotFoundException {
		ArchiveInputStream<? extends ArchiveEntry> s = new ArchiveStreamFactory().createArchiveInputStream(new BufferedInputStream(new FileInputStream(file)));
		return new StreamArchiveFile(s);
	}
	
	public class StreamArchiveFile implements ArchiveFile {
		
		private final ArchiveInputStream<? extends ArchiveEntry> ins;
		
		public StreamArchiveFile(ArchiveInputStream<? extends ArchiveEntry> ins) {
			this.ins = ins;
		}
		

		@Override
		public void close() throws IOException {
			ins.close();
		}

		@Override
		public Enumeration<? extends ArchiveEntry> getEntries() {
			return new Enumeration<ArchiveEntry>() {
				
				private ArchiveEntry next;

				@Override
				public boolean hasMoreElements() {
					try {
						next = ins.getNextEntry();
						return next != null;
					} catch (IOException e) {
						throw new RuntimeException();
					}
				}

				@Override
				public ArchiveEntry nextElement() {
					return next;
				}
			};
		}

		@Override
		public InputStream getInputStream(ArchiveEntry entry) {
			return ins;
		}
		
	}

}
