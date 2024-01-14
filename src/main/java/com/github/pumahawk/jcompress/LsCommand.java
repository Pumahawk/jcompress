package com.github.pumahawk.jcompress;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "ls")
public class LsCommand implements Callable<Integer> {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
	boolean usageHelpRequested;

	@Option(names = { "-f", "--archive" }, description = "Input archive")
	private File file;

	@Option(names = { "--match" }, description = "Regex file name filter")
	private Optional<String> match;

	@Option(names = { "--rewrite" }, description = "Rewrite output path")
	private Optional<String> rewrite;

	@Option(names = { "--type" }, description = "Archive type")
	private Optional<String> type;

	@Autowired
	private ListArchiveAction listArchiveAction;

	@Override
	public Integer call() throws Exception {
		listArchiveAction.list(file, type, match, rewrite);
		return 0;
	}

}
