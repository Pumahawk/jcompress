package com.github.pumahawk.jcompress.outputsolvers;

import java.io.Closeable;

import org.apache.commons.compress.archivers.ArchiveEntry;

import com.github.pumahawk.jcompress.ArchiveFile;

public interface OutputSolver extends Closeable {
	public void writeEntry(ArchiveFile ar, ArchiveEntry entry);
}
