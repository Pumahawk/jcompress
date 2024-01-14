package com.github.pumahawk.jcompress;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@ComponentScan(basePackageClasses = JcompressApplication.class)
public class JcompressApplication  {
	
	public static void main(String[] args) {
		run(args);
	}
	
	public static int run(String[] args) {
		try (AnnotationConfigApplicationContext spring = new AnnotationConfigApplicationContext(JcompressApplication.class)) {
			return spring.getBeansWithAnnotation(Command.class)
				.entrySet()
				.stream()
				.map(e -> e.getValue())
				.filter(v -> v.getClass() != JCompressCommand.class)
				.collect(
						() -> new CommandLine(spring.getBean(JCompressCommand.class)),
						(c, com) -> c.addSubcommand(com),
						(a, b) -> b.getSubcommands()
							.values()
							.stream()
							.forEach(a::addSubcommand))
				.execute(args);
		}
		
	}

}
