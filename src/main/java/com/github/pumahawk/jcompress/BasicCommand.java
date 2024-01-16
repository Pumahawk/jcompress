package com.github.pumahawk.jcompress;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.pumahawk.jcompress.solvers.ArchiveSolver;

public abstract class BasicCommand {
	
	@Autowired
	private List<ArchiveSolver> archiveSolvers;

	
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
	
	public boolean grepMatch(String regex, String input) {
		return Pattern.compile(regex).matcher(input).find();
	}
	
}
