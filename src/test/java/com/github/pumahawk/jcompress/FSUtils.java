package com.github.pumahawk.jcompress;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;

import org.apache.commons.compress.utils.IOUtils;

public class FSUtils {
	public static File createFile(File parent, String path, InputStream in) {
		if (!parent.exists()) {
			throw new RuntimeException("Unable to create file for tests");
		}
		
		File f = Path.of(parent.getPath(), path).toFile();
		if (!f.getParentFile().exists()) {
			f.getParentFile().mkdirs();
		}
		try (var out = new FileOutputStream(f)) {
			IOUtils.copy(in, out);
			return f;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static File createFile(File parent, String path, byte[] in) {
		return createFile(parent, path, new ByteArrayInputStream(in));
	}
	
	public static File createFile(File parent, String path, String in) {
		return createFile(parent, path, new ByteArrayInputStream(in.getBytes()));
	}
	
	public static File createDir(File parent, String dir) {
		if (!parent.exists()) {
			throw new RuntimeException("Unable to create file for tests");
		}
		var f = Path.of(parent.getAbsolutePath(), dir).toFile();
		f.mkdir();
		return f;
	}
	

}
