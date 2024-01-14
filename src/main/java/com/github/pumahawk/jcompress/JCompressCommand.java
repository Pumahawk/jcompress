package com.github.pumahawk.jcompress;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "jcompress")
public class JCompressCommand {

	@Option(names = {"-V", "--version"}, versionHelp = true, description = "display version info")
	private boolean versionInfoRequested;

	@Option(names = {"-h", "--help"}, usageHelp = true, description = "display this help message")
	private boolean usageHelpRequested;

}
