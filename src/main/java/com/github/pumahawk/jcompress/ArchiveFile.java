package com.github.pumahawk.jcompress;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.ArchiveEntry;

public interface ArchiveFile extends Closeable {
	public void close() throws IOException;
	public Enumeration<? extends ArchiveEntry> getEntries();
	public InputStream getInputStream(ArchiveEntry entry);

}
