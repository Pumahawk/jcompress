package com.github.pumahawk.jcompress;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "cat")
public class CatCommand extends BasicCommand implements Callable<Integer> {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
	boolean usageHelpRequested;

	@Parameters(description = "Input archive")
	private List<File> files = new ArrayList<>();

	@Option(names = { "--grep" }, description = "Regex file name filter")
	private Optional<String> match;

	@Option(names = { "--type" }, description = "Archive type")
	private Optional<String> type;
	
	@Autowired
	private IOService ioService;

	@Override
	public Integer call() throws Exception {
		for (File file : files) {
			try (ArchiveFile ar = getArchive(type, file)) {
				Enumeration<? extends ArchiveEntry> archive = ar.getEntries();
				Stream.generate(() -> archive)
				.takeWhile(v -> v.hasMoreElements())
				.map(a -> a.nextElement())
				.filter(entry -> match.map(rx -> grepMatch(rx, entry.getName())).orElse(true))
				.map(entry -> ar.getInputStream(entry))
				.forEach(in -> copy(in, ioService.getSystemOutputStream()));
			}
		}
		
		return 0;
	}
	
	public void copy(InputStream in, OutputStream out) {
		try {
			IOUtils.copy(in, out);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
