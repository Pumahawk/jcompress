package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class JcompressApplicationTests {
	
	@Test
	public void helpTest() {
		simpleRun("--help");
	}
	
	@Test
	public void lsRun() {
		simpleRunAndTest("ls --help");
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
