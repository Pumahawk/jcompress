package com.github.pumahawk.jcompress;

import java.io.Closeable;
import java.io.IOException;
import java.util.Enumeration;
import java.util.function.Supplier;

import org.apache.commons.compress.archivers.ArchiveEntry;

public class ArchiveFile implements Closeable {
	
	private final Closeable archive;
	private final Supplier<Enumeration<? extends ArchiveEntry>> fentry;
	

	public ArchiveFile(Supplier<Enumeration<? extends ArchiveEntry>> fentry, Closeable archive) {
		this.archive = archive;
		this.fentry = fentry;
	}
	
	@Override
	public void close() throws IOException {
		archive.close();
	}
	
	public Enumeration<? extends ArchiveEntry> getEntries() {
		return fentry.get();
	}
	
}
