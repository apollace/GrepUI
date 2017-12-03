package org.polly.actions.aggregated;

import java.io.File;
import java.util.List;

public class RunExternalProgramAggregatedAction {
	private static final RunExternalProgramAggregatedAction instance = new RunExternalProgramAggregatedAction();

	public static RunExternalProgramAggregatedAction getInstance() {
		return instance;
	}

	private Process process = null;

	private RunExternalProgramAggregatedAction() {

	}

	public boolean isAlive() {
		if (this.process == null) {
			return false;
		}

		final boolean isAlive = this.process.isAlive();
		if (!isAlive) {
			this.process = null;
		}

		return isAlive;
	}

	public boolean kill() {
		if (this.process == null) {
			return false;
		}

		this.process.destroy();
		this.process = null;

		return true;
	}

	public boolean run(List<String> vsArrays, String outputPath) throws Exception {
		if (this.process != null) {
			return false;
		}

		final ProcessBuilder builder = new ProcessBuilder(vsArrays);
		builder.redirectOutput(new File(outputPath));
		builder.redirectError(new File(outputPath));
		this.process = builder.start();
		return true;
	}

}
