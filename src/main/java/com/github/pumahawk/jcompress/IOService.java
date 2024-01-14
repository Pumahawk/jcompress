package com.github.pumahawk.jcompress;

import java.io.InputStream;
import java.io.PrintStream;

import org.springframework.stereotype.Component;

@Component
public class IOService {
	public PrintStream getSystemOutputStream() {
		return System.out;
	}
	public PrintStream getSystemErrorStream() {
		return System.err;
	}
	public InputStream getSystemInputStream() {
		return System.in;
	}

}
