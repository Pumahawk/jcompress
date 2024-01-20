package com.github.pumahawk.jcompress;

import static org.mockito.Mockito.doReturn;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.cpio.CpioArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.BeforeEach;

public interface ArchiveBaseTest {

	public File getTD();
	public IOService getIOService();

	public void setSTDOut(ByteArrayOutputStream out);
	public ByteArrayOutputStream getSTDOut();


	@BeforeEach
	public default void mockIoService() throws FileNotFoundException {
		setSTDOut(new ByteArrayOutputStream());
		doReturn(new PrintStream(getSTDOut())).when(getIOService()).getSystemOutputStream();
	}

	@BeforeEach
	public default void prepareTempDirectory() {

		var tdArchive = Path.of(getTD().getPath(), "archives").toFile();
		tdArchive.mkdir();
		
		try {
			var archives = Path.of(getClass().getResource("/archives").toURI()).toFile().listFiles();
			for (File archive : archives) {
				try (var fout = new FileOutputStream(Paths.get(tdArchive.getAbsolutePath(), archive.getName()).toFile())) {
					IOUtils.copy(archive, fout);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Unable to prepare temp folder", e);
		}
		
	}
	
	public default File getFile(String path) {
		var f = Path.of(getTD().getAbsolutePath(), path).toFile();
		f.getParentFile().mkdirs();
		return f;
	}
	
	public default File copyFile(String source, String destination) {
		var f1 = getFile(source);
		var f2 = getFile(destination);
		try {
			FileUtils.copyFile(f1, f2);
			return f2;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	public default File mkdir(String path) {
		return FSUtils.createDir(getTD(), path);
	}

	public default Stream<File> ls(File root) {
		return Stream.of(root.listFiles());
	}
	public default Stream<File> lsr(File root) {
		return ls(root).flatMap(this::allFileRecursive);
	}

	public default Stream<File> allFileRecursive(File in) {
		return Stream.concat(Stream.of(in), 
				in.isFile() ? Stream.empty() : Stream.of(in.listFiles()).flatMap(f -> allFileRecursive(f)));
	}
	
	public default ArchiveIN readArchive(String file) {
		return readArchive(getFile(file));
	}
	
	public default ArchiveIN readArchive(File file) {
		return new ArchiveIN(file);
	}
	
	public default Stream<String> getStdoutLines() {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(new ByteArrayInputStream(getSTDOut().toByteArray()));
		return Stream.generate(() -> scanner).takeWhile(sc -> sc.hasNextLine()).map(sc -> sc.nextLine());
	}

	public default File createArchive(ArchiveType type, Consumer<ArchiveOUT> farchive) {
		File file = Stream.iterate(0, n -> n+1).map(n -> getFile("tmp-archive-" + n + "." + type.getType())).filter(f-> !f.exists()).findAny().get();
		try {
			var sout = new FileOutputStream(file);
			ArchiveOutputStream<ArchiveEntry> sa = new ArchiveStreamFactory().createArchiveOutputStream(type.getType(), new FileOutputStream(file));
			try (var arout = new ArchiveOUT(sa, sout)) {
				farchive.accept(arout);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return file;
	}
	
	public default String unixAbsolutePath(File f) {
		return FilenameUtils.separatorsToUnix(f.getAbsolutePath()).replace("^C:/", "");
	}
	
	public default String unixPath(File f) {
		return FilenameUtils.separatorsToUnix(f.getPath()).replace("^C:/", "");
	}
	
	public enum ArchiveType {
		ZIP("zip"),
		TAR("tar"),
		CPIO("cpio"),
		;
		
		private final String type;
		
		private ArchiveType(String type) {
			this.type = type;
		}
		
		public String getType() {
			return type;
		}
	}
	
	public static class ArchiveOUT implements Closeable {
		
		final private ArchiveOutputStream<ArchiveEntry> out;
		final private OutputStream outs;
		
		public ArchiveOUT(ArchiveOutputStream<ArchiveEntry> out, OutputStream outs) {
			this.out = out;
			this.outs = outs;
		}

		public ArchiveOUT put(String name) {
			return put(name, null);
		}
		
		public ArchiveOUT put(String name, String content) {
			Optional<byte[]> body = Optional.ofNullable(content).map(String::getBytes);
			ArchiveEntry entry = switch(out.getClass().getName()) {
				case "org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream" -> new ZipArchiveEntry(name) {{
					body.map(b -> b.length).ifPresent(this::setSize);
				}};
				case "org.apache.commons.compress.archivers.tar.TarArchiveOutputStream" -> new TarArchiveEntry(name);
				case "org.apache.commons.compress.archivers.cpio.CpioArchiveOutputStream" -> new CpioArchiveEntry(name) {{
					body.map(b -> b.length).ifPresent(this::setSize);
				}};
				default -> throw new RuntimeException("Unsupported type");
			};
			try {
				out.putArchiveEntry(entry);
				if (body.isPresent()) {
					out.write(body.get());
				}
				out.closeArchiveEntry();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return this;
		}

		@Override
		public void close() throws IOException {
			out.close();
			outs.close();
		}
	}
	
	public static class ArchiveIN implements Closeable, Iterable<ArchiveEntry> {
		
		private final InputStream in;
		private final ArchiveInputStream<ArchiveEntry> stream;
		
		public ArchiveIN(File archive) {
			try {
				this.in = new BufferedInputStream(new FileInputStream(archive));
				this.stream = new ArchiveStreamFactory().createArchiveInputStream(in);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		public InputStream getInputStream() {
			 return stream;
		}
		
		public byte[] content() {
			try {
				return IOUtils.toByteArray(stream);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		
		public String contentAsString() {
			return new String(content());
		}
		
		@Override
		public Iterator<ArchiveEntry> iterator() {
			return new Iterator<ArchiveEntry>() {

				private boolean hasPark = false;
				private ArchiveEntry park;

				@Override
				public ArchiveEntry next() {
					if (hasPark) {
						hasPark = false;
						return park;
					} else {
						try {
							return stream.getNextEntry();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				}
				
				@Override
				public boolean hasNext() {
					if (hasPark) {
						return park != null;
					} else {
						hasPark = true;
						try {
							park =  stream.getNextEntry();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					return false;
				}
			};
		}

		@Override
		public void close() throws IOException {
			this.in.close();
		}
		
	}
}
