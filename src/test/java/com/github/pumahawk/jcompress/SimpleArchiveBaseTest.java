package com.github.pumahawk.jcompress;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

@SpyBean({
	IOService.class,
})
public class SimpleArchiveBaseTest implements ArchiveBaseTest {

	@Autowired
	private IOService ioService;
	
	private ByteArrayOutputStream ous;
	
	@TempDir()
	protected File td;

	@Override
	public File getTD() {
		return td;
	}


	@Override
	public IOService getIOService() {
		return ioService;
	}


	@Override
	public void setSTDOut(ByteArrayOutputStream out) {
		this.ous = out;
	}


	@Override
	public ByteArrayOutputStream getSTDOut() {
		return ous;
	}

}
