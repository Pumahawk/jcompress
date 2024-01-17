package com.github.pumahawk.jcompress.outputsolvers;

import org.apache.commons.compress.archivers.ArchiveEntry;

public class ExtractionEntry {
	private String name;
	private final ArchiveEntry entry;
	
	public ExtractionEntry(ArchiveEntry entry) {
		this.entry = entry;
		this.name = entry.getName();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public ArchiveEntry getEntry() {
		return entry;
	}
	
	public String getName() {
		return name;
	}
}
