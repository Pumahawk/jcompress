package com.github.pumahawk.jcompress;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.solvers.ArchiveSolver;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "ls")
public class LsCommand implements Callable<Integer> {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
	boolean usageHelpRequested;

	@Parameters(description = "Input archive")
	private List<File> files = new ArrayList<>();

	@Option(names = { "--grep" }, description = "Regex file name filter")
	private Optional<String> match;

	@Option(names = { "--rewrite" }, description = "Rewrite output path")
	private Optional<String> rewrite;

	@Option(names = { "--type" }, description = "Archive type")
	private Optional<String> type;
	
	@Option(names = {"-l", "--file-name"}, description = "Show file name")
	private boolean fileName;
	
	@Autowired
	private IOService ioService;
	
	@Autowired
	private List<ArchiveSolver> archiveSolvers;

	@SuppressWarnings("resource")
	@Override
	public Integer call() throws Exception {
		for (File file : files) {
			try (ArchiveFile ar = getArchive(type, file)) {
				Enumeration<? extends ArchiveEntry> archive = ar.getEntries();
				Stream.generate(() -> archive)
				.takeWhile(v -> v.hasMoreElements())
				.map(a -> a.nextElement())
				.map(entry -> entry.getName())
				.filter(name -> match.map(rx -> grepMatch(rx, name)).orElse(true))
				.map(name -> rewrite.map(this::rexKey).map(rxc -> name.replaceAll(
						rxc[0],
						rxc[1])).orElse(name))
				.map(name -> !fileName ? name : file.getPath() + ":" + name)
				.forEach(ioService.getSystemOutputStream()::println);
			}
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
	
	public boolean grepMatch(String regex, String input) {
		return Pattern.compile(regex).matcher(input).find();
	}

}
