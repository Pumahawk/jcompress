package com.github.pumahawk.jcompress;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import picocli.CommandLine;

public abstract class CommandBaseTest<T> extends SimpleArchiveBaseTest {

	@Autowired
	private ApplicationContext ac;
	
	protected Supplier<T> command = () -> ac.getBean(commandType());
	
	public abstract Class<T> commandType();
	
	public Integer run(int code, String... args) {
		var c = new CommandLine(command.get()).execute(args);
		assertEquals(code, c);
		return c;
	}
	
	public Integer run(String... args ) {
		return run(0, args);
	}

}
