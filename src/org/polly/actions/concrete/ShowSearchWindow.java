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

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.polly.actions.Action;
import org.polly.ui.SearchWindow;

public class ShowSearchWindow implements Action {
	private SearchWindow searchWindow = null;
	private final JTextArea textArea;

	public ShowSearchWindow(JTextArea textArea) {
		this.textArea = textArea;
	}

	@Override
	public void execute() {
		if (this.searchWindow == null) {
			this.searchWindow = new SearchWindow(
					(JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, this.textArea), this.textArea);
		}

		this.searchWindow.setVisible(true);
	}
}
