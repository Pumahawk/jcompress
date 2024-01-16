package com.github.pumahawk.jcompress.outputsolvers;

import java.io.File;

public interface OutputSolverFactory {
	public boolean support(String type);
	public OutputSolver solve(String type, File output);
}
