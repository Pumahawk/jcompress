package com.github.pumahawk.jcompress.solvers;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

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
		return new ArchiveFile(() -> Collections.enumeration(tar.getEntries()), tar);
	}

}
