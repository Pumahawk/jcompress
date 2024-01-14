package com.github.pumahawk.jcompress;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.solvers.ArchiveSolver;

@Component
public class ListArchiveAction {
	
	@Autowired
	private IOService ioService;
	
	@Autowired
	private List<ArchiveSolver> archiveSolvers;

	@SuppressWarnings("resource")
	public Integer list(File inputFile, Optional<String> type, Optional<String> match, Optional<String> rewrite) throws ArchiveException, IOException {
		try (ArchiveFile ar = getArchive(type, inputFile)) {
			Enumeration<? extends ArchiveEntry> archive = ar.getEntries();
			Stream.generate(() -> archive)
			.takeWhile(v -> v.hasMoreElements())
			.map(a -> a.nextElement())
			.map(entry -> entry.getName())
			.filter(name -> !match.isPresent() || name.matches(match.get()))
			.map(name -> rewrite.map(this::rexKey).map(rxc -> name.replaceAll(
					rxc[0],
					rxc[1])).orElse(name))
			.forEach(ioService.getSystemOutputStream()::println);
		}
		
		return 0;
	}
	
	public ArchiveFile getArchive(Optional<String> type, File file) {
		String t = type.orElseGet(() -> FileNameUtils.getExtension(file.getName()));
		return archiveSolvers.stream()
			.filter(s -> s.support(t))
			.findAny()
			.map(s -> s.createArchiveFileNoThrow(file))
			.orElseThrow(() -> new RuntimeException("Unsupported archive type. Type: " + t));
	}
	
	public String[] rexKey(String rxc) {
		String[] rex = {
			rxc.replaceAll("(.*[^\\\\]):.*", "$1").replaceAll("\\\\:", ":"),
			rxc.replaceAll(".*[^\\\\]:", "").replaceAll("\\\\:", ":"),
		};
		return rex;
	}
}
