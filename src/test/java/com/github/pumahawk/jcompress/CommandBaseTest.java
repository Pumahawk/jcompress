package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Scanner;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationContext;

import com.github.pumahawk.jcompress.CommandBaseTest.Archive.Entry;

import picocli.CommandLine;

@SpyBean({
	IOService.class,
})
public abstract class CommandBaseTest<T> {

	@Autowired
	private ApplicationContext ac;
	
	protected Supplier<T> command = () -> ac.getBean(commandType());
	
	@Autowired
	private IOService ioService;
	
	private ByteArrayOutputStream ous;
	
	@TempDir()
	protected File td;
	
	public abstract Class<T> commandType();	

	@BeforeEach
	public void mockIoService() throws FileNotFoundException {
		ous = new ByteArrayOutputStream();
		doReturn(new PrintStream(ous)).when(ioService).getSystemOutputStream();
	}

	@BeforeEach
	public void prepareTempDirectory() {

		var tdArchive = Path.of(td.getPath(), "archives").toFile();
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
	
	public File getFile(String path) {
		var f = Path.of(td.getAbsolutePath(), path).toFile();
		f.getParentFile().mkdirs();
		return f;
	}
	
	public Integer run(String... args) {
		var code = new CommandLine(command.get()).execute(args);
		assertEquals(0, code);
		return code;
	}
	
	public File mkdir(String path) {
		return FSUtils.createDir(td, path);
	}

	public Stream<File> ls(File root) {
		return Stream.of(root.listFiles());
	}
	public Stream<File> lsr(File root) {
		return ls(root).flatMap(this::allFileRecursive);
	}

	private Stream<File> allFileRecursive(File in) {
		return Stream.concat(Stream.of(in), 
				in.isFile() ? Stream.empty() : Stream.of(in.listFiles()).flatMap(f -> allFileRecursive(f)));
	}
	
	public Archive readArchive(String file) {
		return readArchive(getFile(file));
	}
	
	public Archive readArchive(File file) {
		return new Archive(file);
	}
	
	public ByteArrayOutputStream getStdOut() {
		return ous;
	}
	
	public Stream<String> getStdoutLines() {
		@SuppressWarnings("resource")
		Scanner scanner = new Scanner(new ByteArrayInputStream(getStdOut().toByteArray()));
		return Stream.generate(() -> scanner).takeWhile(sc -> sc.hasNextLine()).map(sc -> sc.nextLine());
	}
	
	public static class Archive implements Closeable, Iterable<Entry> {
		
		private final InputStream in;
		private final ArchiveInputStream<ArchiveEntry> stream;
		
		public Archive(File archive) {
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
		
		public static class Entry {
			private final ArchiveEntry entry;
			
			public Entry(ArchiveEntry entry) {
				this.entry = entry;
			}
			
			public ArchiveEntry getEntry() {
				return entry;
			}
		}

		@Override
		public Iterator<Entry> iterator() {
			return new Iterator<Entry>() {

				private boolean hasPark = false;
				private Entry park;

				@Override
				public Entry next() {
					if (hasPark) {
						hasPark = false;
						return park;
					} else {
						try {
							return new Entry(stream.getNextEntry());
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
							park =  new Entry(stream.getNextEntry());
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
