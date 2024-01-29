package com.github.pumahawk.jcompress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.concurrent.Callable;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "cmp")
public class CmpCommand extends BasicCommand implements Callable<Integer> {

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
	boolean usageHelpRequested;

	@Option(names = { "--type", "--it" }, description = "Archive type")
	private Optional<String> type;

	@Option(names = { "--output-type", "--ot", "--out-type", "-o" }, description = "Output type")
	private Optional<String> outputType;
	
	@Option(names = { "--input", "--in" }, description = "Input file")
	private Optional<File> inputFile;
	
	@Option(names = { "--output", "--out" }, description = "Output file")
	private Optional<File> outputFile;

	@Option(names = { "--decompress-unit-eof", "--due" }, description = "If true, decompress until the end of the input; if false, stopafter the first stream and leave the input position to pointto the next byte after the stream. This setting applies to thegzip, bzip2 and XZ formats only.")
	private boolean decompressUnitEOF;

	@Option(required = false, names = { "--memory-limit", "--ml" }, description = "Some streams require allocation of potentially significantbyte arrays/tables, and they can offer checks to prevent OOMson corrupt files. Set the maximum allowed memory allocation in KBs")
	private int memoryLimitInKb = 0;

	@Override
	public Integer call() throws Exception {

		var in = type.map(type -> (InputStream) createCompressorInputStream(type, getIn())).orElseGet(() -> getIn());
		var out = outputType.map(type -> (OutputStream) createCompressorOutputStream(type, getOut())).orElseGet(() -> getOut());
		
		IOUtils.copy(in, out);
		out.flush();
		in.close();
		out.close();
		
		return 0;
	}
	
	private OutputStream getOut() {
		return outputFile.map(this::wfo).orElseGet(() -> System.out);
	}

	private InputStream getIn() {
		return inputFile.map(this::wf).orElseGet(() -> System.in);
	}
	
	private CompressorOutputStream createCompressorOutputStream(String type, OutputStream out) {
		try {
			return new CompressorStreamFactory(decompressUnitEOF, memoryLimitInKb).createCompressorOutputStream(type, out);
		} catch (CompressorException e) {
			throw new RuntimeException(e);
		}
	}
	
	private InputStream wf(File in) {
		try {
			return new FileInputStream(in);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private OutputStream wfo(File out) {
		try {
			return new FileOutputStream(out);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private CompressorInputStream createCompressorInputStream(String type, InputStream in) {
		try {
			return new CompressorStreamFactory(decompressUnitEOF, memoryLimitInKb)
					.createCompressorInputStream(type, in);
		} catch (CompressorException e) {
			throw new RuntimeException(e);
		}
	}
	

}
