package com.github.pumahawk.jcompress;

import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "ls")
public class LsCommand extends BasicCommand implements Callable<Integer> {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
	boolean usageHelpRequested;

	@Parameters(description = "Input archive")
	private List<File> files = new ArrayList<>();

	@Option(names = { "--grep" }, description = "Regex file name filter")
	private Optional<String> match;

	@Option(names = { "--rewrite" , "--rw" }, description = "Rewrite output path")
	private Optional<String> rewrite;

	@Option(names = { "--type" }, description = "Archive type")
	private Optional<String> type;
	
	@Option(names = {"-l", "--file-name"}, description = "Show file name")
	private boolean fileName;
	
	@Autowired
	private IOService ioService;

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

}
