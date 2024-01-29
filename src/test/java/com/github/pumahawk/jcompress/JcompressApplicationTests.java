package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JcompressApplicationTests {
	
	public static Stream<Arguments> commandsHelpTests() {
		return Stream.of(
			arr("--help"),
			arr("ls", "--help"),
			arr("cat", "--help"),
			arr("ex", "--help"),
			arr("mk", "--help"),
			arr("cmp", "--help")
		);
	}
	
	@ParameterizedTest
	@MethodSource
	public void commandsHelpTests(List<String> args) {
		runAndTest(args.toArray(n -> new String[n]));
	}
	
	public static Arguments arr(String... elements) {
		return Arguments.of(Arrays.asList(elements));
	}
	
	public int simpleRun(String args) {
		return run(args.split(" "));
	}
	
	public int simpleRunAndTest(String args) {
		return runAndTest(args.split(" "));
	}
	
	private int runAndTest(String[] args) {
		int code = run(args);
		assertEquals(0, code, "Exit code not 0");
		return code;
	}

	public int run(String... args) {
		return JcompressApplication.run(args);
	}

}
