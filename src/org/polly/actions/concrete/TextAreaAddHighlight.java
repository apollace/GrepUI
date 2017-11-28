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

import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.polly.actions.Action;
import org.polly.ui.Highlighter;

public class TextAreaAddHighlight implements Action {

	private JTextArea textArea; 
	private Highlighter highlighter;
	
	public TextAreaAddHighlight(JTextArea textArea, Highlighter highlighter) {
		this.textArea = textArea;
		this.highlighter = highlighter;
	}

	@Override
	public void execute() {
		try {
			if (textArea.getSelectedText() != null && textArea.getSelectedText().length() > 0) {
				highlighter.add(textArea.getSelectedText());
			} else {
				String word = JOptionPane.showInputDialog(textArea, "Enter the string to highlight",
						"User input required", JOptionPane.QUESTION_MESSAGE);
				highlighter.add(word);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

}
