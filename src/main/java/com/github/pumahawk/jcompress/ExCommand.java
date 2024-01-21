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

import com.github.pumahawk.jcompress.outputsolvers.ExtractionEntry;
import com.github.pumahawk.jcompress.outputsolvers.OutputSolverFactory;
import com.github.pumahawk.jcompress.outputsolvers.SupportOutputSolverFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "ex")
public class ExCommand  extends BasicCommand implements Callable<Integer> {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
	boolean usageHelpRequested;

	@Parameters(description = "Input archive")
	private List<File> files = new ArrayList<>();

	@Option(names = { "--grep" }, description = "Regex file name filter")
	private Optional<String> match;

	@Option(names = { "--rewrite", "--rw" }, description = "Rewrite output path")
	private Optional<String> rewrite;

	@Option(names = { "--type" }, description = "Archive type")
	private Optional<String> type;

	@Option(names = { "--output-type", "--ot", "--out-type", "-o" }, description = "Output type", defaultValue = "dir")
	private String outputType;
	
	@Option(names = {"--output-directory", "--target", "-t"})
	private File output;

	@Autowired
	private List<OutputSolverFactory> outputs;
	
	@Autowired
	private SupportOutputSolverFactory outputDefault;

	@SuppressWarnings("resource")
	@Override
	public Integer call() throws Exception {
		try (var os = outputs
				.stream()
				.filter(s -> s.support(outputType))
				.findAny()
				.orElseGet(() -> outputDefault)
				.solve(outputType, output);
				) {
			for (File file : files) {
				try (ArchiveFile ar = getArchive(type, file)) {
					Enumeration<? extends ArchiveEntry> archive = ar.getEntries();
					Stream.generate(() -> archive)
					.takeWhile(v -> v.hasMoreElements())
					.map(a -> a.nextElement())
					.filter(entry -> match.map(rx -> grepMatch(rx, entry.getName())).orElse(true))
					.map(ExtractionEntry::new)
					.peek(entry -> rewrite.map(this::rexKey).map(rxc -> entry.getName().replaceAll(
							rxc[0],
							rxc[1])).ifPresent(entry::setName))
					.forEach(entry -> os.writeEntry(ar, entry));
				}
			}	
		}
		
		return 0;
	}
	
}
