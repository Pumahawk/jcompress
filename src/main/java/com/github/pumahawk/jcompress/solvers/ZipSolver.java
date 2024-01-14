package com.github.pumahawk.jcompress.solvers;

import java.io.File;
import java.io.IOException;

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
		return new ArchiveFile(() -> zip.getEntries(), zip);
	}

}
