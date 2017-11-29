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
package org.polly.persistency;

public class Option {
	public enum VisibilityMode {
		VISIBLE_ON_DEMAND, VISIBLE_ALWAYS, HIDDEN
	}

	private final String key;
	private final String nameToShow;
	private final String defaultValue;
	private final String description;
	private String lastValue = "";

	VisibilityMode visibilityMode;

	public Option(String key, String nameToShow, String defaultValue) {
		this(key, nameToShow, defaultValue, "");
	}

	public Option(String key, String nameToShow, String defaultValue, String description) {
		this(key, nameToShow, defaultValue, description, VisibilityMode.VISIBLE_ON_DEMAND);
	};

	public Option(String key, String nameToShow, String defaultValue, String description,
			VisibilityMode visibilityMode) {
		this.key = key;
		this.nameToShow = nameToShow;
		this.defaultValue = defaultValue;
		this.lastValue = defaultValue;
		this.description = description;
		this.visibilityMode = visibilityMode;
	}

	public Option(String key, String nameToShow, String defaultValue, VisibilityMode visibilityMode) {
		this(key, nameToShow, defaultValue, "", visibilityMode);
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public String getDescription() {
		return this.description;
	}

	public String getKey() {
		return this.key;
	}

	public String getLastValue() {
		return this.lastValue;
	}

	public String getNameToShow() {
		return this.nameToShow;
	}

	public VisibilityMode getVisibilityMode() {
		return this.visibilityMode;
	}

	public void setLastValue(String lastValue) {
		this.lastValue = lastValue;
	}
}