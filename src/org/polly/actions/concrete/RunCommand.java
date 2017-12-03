/**
 * This file belonging to GrepUi an open source tool to search and trace
 * information contained in your logs.
 * Copyright (C) 2017  Alessandro Pollace
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.polly.actions.concrete;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JTextArea;
import javax.swing.Timer;

import org.polly.actions.Action;
import org.polly.actions.aggregated.RunExternalProgramAggregatedAction;
import org.polly.persistency.Option;

public class RunCommand implements Action {
	public static final String command = "command";
	public static final String homePath = "home";
	public static final String outFilename = "outFilename";
	public static final String maxFileLineToRead = "maxFileLineToRead";
	private static final String variableRegEx = "\\$\\{(.*?)\\}";
	private static final Pattern p = Pattern.compile(variableRegEx);

	private final JTextArea output;
	private final Collection<Option> options;
	private final StringBuilder sb = new StringBuilder();

	public RunCommand(JTextArea output, Collection<Option> options) {
		this.output = output;
		this.options = options;
	}

	@Override
	public void execute() {
		try {
			String outputPath = this.getOption(homePath).getLastValue();
			outputPath += outputPath.endsWith(File.separator) ? "" : File.separator;
			outputPath += this.getOption(outFilename).getLastValue();

			String localCommand = this.getOption(command).getLastValue();
			final Matcher m = p.matcher(localCommand);

			final Map<String, String> replacements = new TreeMap<String, String>();

			int startFrom = 0;
			while (m.find(startFrom)) {
				startFrom = m.end();

				final String key = m.group(1);
				final String value = this.getOption(key).getLastValue();
				replacements.put(m.group(), value);
			}

			for (final Entry<String, String> entry : replacements.entrySet()) {
				localCommand = localCommand.replace(entry.getKey(), entry.getValue());
			}

			this.output.setText(localCommand);

			final List<String> vsArrays = new ArrayList<String>();
			vsArrays.add("/bin/sh");
			vsArrays.add("-c");
			vsArrays.add(localCommand);

			this.runAndFork(outputPath, localCommand, vsArrays);

		} catch (final Throwable e) {
			this.printExceptionToOutput(e);
		}
	}

	private Option getOption(String key) {
		for (final Option option : this.options) {
			if (option.getKey().equals(key)) {
				return option;
			}
		}

		return null;
	}

	private void printExceptionToOutput(final Throwable e) {
		final StringWriter sw = new StringWriter();
		final PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		this.output.setText(sw.toString());
	}

	private void runAndFork(String outputPath, String localCommand, final List<String> vsArrays) throws Exception {
		final RunExternalProgramAggregatedAction runner = RunExternalProgramAggregatedAction.getInstance();
		final boolean isRunning = runner.run(vsArrays, outputPath);
		if (!isRunning) {
			this.output.setText(
					"There is something wrong, probably you have already run something, please you it is stuck kill it");
			return;
		}

		final Timer t = new Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				LineNumberReader reader = null;
				try {
					if (runner.isAlive()) {
						// skip this time, retry later
						return;
					}

					reader = new LineNumberReader(new InputStreamReader(new FileInputStream(outputPath), "UTF-8"));

					final int maxFileLineToRead = Integer
							.valueOf(RunCommand.this.getOption(RunCommand.maxFileLineToRead).getLastValue());

					// Clean the builder instead of allocate a new one
					RunCommand.this.sb.setLength(0);
					RunCommand.this.sb.append("Executed command: ");
					RunCommand.this.sb.append(localCommand).append(" ");
					RunCommand.this.sb.append(localCommand).append("\n");
					RunCommand.this.sb.append("--\n\n");
					String line;
					while ((line = reader.readLine()) != null && reader.getLineNumber() <= maxFileLineToRead) {
						RunCommand.this.sb.append(line).append("\n");
					}
					RunCommand.this.output.setText(RunCommand.this.sb.toString());

				} catch (final Exception e) {
					RunCommand.this.printExceptionToOutput(e);
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (final IOException e) {
							RunCommand.this.printExceptionToOutput(e);
						}
					}
				}

				((Timer) event.getSource()).stop();
			}
		});
		t.setRepeats(true);
		t.start();
	}
}
