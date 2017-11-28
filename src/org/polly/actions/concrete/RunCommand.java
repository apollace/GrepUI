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

import java.io.File;
import java.io.FileInputStream;
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

import org.polly.actions.Action;
import org.polly.persistency.Option;

public class RunCommand implements Action {
	public static final String command = "command";
	public static final String homePath = "home";
	public static final String outFilename = "outFilename";
	public static final String maxFileLineToRead = "maxFileLineToRead";
	private static final String variableRegEx = "\\$\\{(.*?)\\}";
	private static final Pattern p = Pattern.compile(variableRegEx);

	private JTextArea output;
	private Collection<Option> options;
	private StringBuilder sb = new StringBuilder();

	private Option getOption(String key) {
		for (Option option : options) {
			if (option.getKey().equals(key)) {
				return option;
			}
		}

		return null;
	}

	public RunCommand(JTextArea output, Collection<Option> options) {
		this.output = output;
		this.options = options;
	}

	@Override
	public void execute() {
		try {
			String outputPath = getOption(homePath).getLastValue();
			outputPath += outputPath.endsWith(File.separator) ? "" : File.separator;
			outputPath += getOption(outFilename).getLastValue();

			String localCommand = getOption(command).getLastValue();
			Matcher m = p.matcher(localCommand);

			Map<String, String> replacements = new TreeMap<String, String>();

			int startFrom = 0;
			while (m.find(startFrom)) {
				startFrom = m.end();

				String key = m.group(1);
				String value = getOption(key).getLastValue();
				replacements.put(m.group(), value);
			}

			for (Entry<String, String> entry : replacements.entrySet()) {
				localCommand = localCommand.replace(entry.getKey(), entry.getValue());
			}

			output.setText(localCommand);

			List<String> vsArrays = new ArrayList<String>();
			vsArrays.add("/bin/sh");
			vsArrays.add("-c");
			vsArrays.add(localCommand);
			
			ProcessBuilder builder = new ProcessBuilder(vsArrays);
			builder.redirectOutput(new File(outputPath));
			builder.redirectError(new File(outputPath));
			Process p = builder.start(); // may throw IOException
			p.waitFor();

			LineNumberReader reader = new LineNumberReader(
					new InputStreamReader(new FileInputStream(outputPath), "UTF-8"));

			int maxFileLineToRead = Integer.valueOf(getOption(RunCommand.maxFileLineToRead).getLastValue());
			try {
				// Clean the builder instead of allocate a new one
				sb.setLength(0);
				sb.append("Executed command: ");
				sb.append(localCommand).append(" ");
				sb.append(localCommand).append("\n");
				sb.append("--\n\n");
				String line;
				while (((line = reader.readLine()) != null) && reader.getLineNumber() <= maxFileLineToRead) {
					sb.append(line).append("\n");
				}
				output.setText(sb.toString());
			} finally {
				reader.close();
			}

		} catch (Throwable e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			output.setText(sw.toString());
		}
	}
}
