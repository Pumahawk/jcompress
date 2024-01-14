package com.github.pumahawk.jcompress.solvers;

import java.io.File;

import com.github.pumahawk.jcompress.ArchiveFile;

public interface ArchiveSolver {
	public boolean support(String type);
	public ArchiveFile createArchiveFile(File archiveFile) throws Exception;
	
	default ArchiveFile createArchiveFileNoThrow(File archiveFile) {
		try {
			return createArchiveFile(archiveFile);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	};
}
