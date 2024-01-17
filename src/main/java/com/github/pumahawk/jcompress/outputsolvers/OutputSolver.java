package com.github.pumahawk.jcompress.outputsolvers;

import java.io.Closeable;
import java.io.File;

import org.apache.commons.compress.archivers.ArchiveEntry;

import com.github.pumahawk.jcompress.ArchiveFile;

public interface OutputSolver extends Closeable {
	public ArchiveEntry createEntry(String name);
	public ArchiveEntry createEntry(File file);
	public void writeEntry(ArchiveFile ar, ExtractionEntry entry);
	void writeEntry(File file, ExtractionEntry exentry);
}
