package com.github.pumahawk.jcompress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
	public InputStream getFileInputStream(File file) throws FileNotFoundException {
		return file.getName().equals("-") ? getSystemInputStream() : new FileInputStream(file);
	}
	public OutputStream getFileOutputStream(File file) throws FileNotFoundException {
		return file.getName().equals("-") ? getSystemOutputStream() : new FileOutputStream(file);
	}

}
