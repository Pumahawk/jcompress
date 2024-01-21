package com.github.pumahawk.jcompress;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.pumahawk.jcompress.outputsolvers.ExtractionEntry;
import com.github.pumahawk.jcompress.outputsolvers.OutputSolverFactory;
import com.github.pumahawk.jcompress.outputsolvers.SupportOutputSolverFactory;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

record Tuple<A, B>(A key, B value){
	public <T> Tuple<A, T> map(Function<Tuple<A, B>, T> fn) {
		return new Tuple<>(key, fn.apply(this));
	}
	public <T> Tuple<A, T> map(T value) {
		return new Tuple<>(key, value);
	}
}

@Component
@Command(name = "mk")
public class MkCommand  extends BasicCommand implements Callable<Integer> {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
	boolean usageHelpRequested;

	@Parameters(description = "Input archive")
	private List<File> files = new ArrayList<>();

	@Option(names = { "--grep" }, description = "Regex file name filter")
	private Optional<String> match;

	@Option(names = { "--rewrite", "--rw" }, description = "Rewrite output path")
	private Optional<String> rewrite;

	@Option(names = { "--output-type", "--ot", "--out-type", "-o" }, description = "Output type", defaultValue = "dir")
	private String outputType;
	
	@Option(names = {"--target", "-t"})
	private File output;
	
	@Option(names = {"--no-absolute", "--nabs"})
	private boolean noAbsolute;

	@Autowired
	private List<OutputSolverFactory> outputs;
	
	@Autowired
	private SupportOutputSolverFactory outputDefault;
	
	@Override
	public Integer call() throws Exception {
		try (var os = outputs
				.stream()
				.filter(s -> s.support(outputType))
				.findAny()
				.orElseGet(() -> outputDefault)
				.solve(outputType, output);
				) {
			files
				.stream()
				.map(f -> new Tuple<>(f, f))
				.flatMap(fo -> this.allFileRecursive(fo.key())
						.map(f -> new Tuple<>(fo.key(), f)))
				.filter(f -> match.map(rx -> grepMatch(rx, f.value().getPath())).orElse(true))
				.map(f -> new Tuple<>(f.value(), new ExtractionEntry(noAbsolute ? f.value().getPath().substring(f.key().getPath().length()) : f.value().getPath(), os.createEntry(f.value()))))
				.peek(f -> f.value().setName(FilenameUtils.separatorsToUnix(f.value().getName())))
				.filter(f -> !f.value().getName().equals(""))
				.peek(f -> f.value().setName(f.value().getName().replaceFirst("^/", "")))
				.peek(f -> rewrite.map(this::rexKey).map(rxc -> f.value().getName().replaceAll(
						rxc[0],
						rxc[1])).ifPresent(name -> f.value().setName(name)))
				.forEach(f -> os.writeEntry(f.key(), f.value()));

		}
		
		return 0;
	}
	

	private Stream<File> allFileRecursive(File in) {
		return Stream.concat(Stream.of(in), 
				in.isFile() ? Stream.empty() : Stream.of(in.listFiles()).flatMap(f -> allFileRecursive(f)));
	}
	
}
