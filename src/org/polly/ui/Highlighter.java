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
package org.polly.ui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class Highlighter implements DocumentListener {
	private JTextArea textArea;
	private Color color;
	private Collection<String> wordsToHighlight;
	private Collection<Object> highlightTags;
	private Collection<Integer> highlightBookmarks;

	Highlighter(JTextArea textArea, Color color) {
		this.textArea = textArea;
		this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);
		wordsToHighlight = new ArrayList<String>();
		highlightTags = new ArrayList<Object>();
		highlightBookmarks = new ArrayList<Integer>();

		textArea.getDocument().addDocumentListener(this);
	}

	public void add(String word) throws Exception {
		if (word == null || word.length() == 0 || wordsToHighlight.contains(word)) {
			return;
		}

		wordsToHighlight.add(word);
		highlight(word);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		refreshHighlights();
	}

	public void clear() {
		wordsToHighlight.clear();
		clearHighlights();
		highlightBookmarks.clear();
	}

	private void clearHighlights() {
		for (Object tag : highlightTags) {
			textArea.getHighlighter().removeHighlight(tag);
		}
		highlightTags.clear();
	}

	public void gotoNextHighlight() {
		if (highlightBookmarks.isEmpty()) {
			return;
		}

		int currentPosition = textArea.getSelectionEnd();

		int firstBookmarkPosition = -1;
		int lastBookmarkPosition = 0;
		Iterator<Integer> currentBookmark = highlightBookmarks.iterator();
		while (currentBookmark.hasNext()) {
			int currentBookmarkPosition = currentBookmark.next();

			if (firstBookmarkPosition < 0) {
				firstBookmarkPosition = currentBookmarkPosition;
			}

			lastBookmarkPosition = currentBookmarkPosition;
			if (currentBookmarkPosition > currentPosition) {
				selectCurrentLine(currentBookmarkPosition);
				break;
			}
		}

		if (lastBookmarkPosition <= currentPosition) {
			selectCurrentLine(firstBookmarkPosition);
		}
	}

	private void selectCurrentLine(int currentBookmarkPosition) {
		int lineStart = currentBookmarkPosition;
		while (lineStart > 0 && textArea.getText().charAt(lineStart) != '\n') {
			lineStart--;
		}

		int lineEnd = currentBookmarkPosition;
		while (lineEnd < textArea.getText().length() && textArea.getText().charAt(lineEnd) != '\n') {
			lineEnd++;
		}

		textArea.select(lineStart, lineEnd);
	}

	private void highlight(String word) throws BadLocationException {
		String text = textArea.getText();
		javax.swing.text.Highlighter highlighter = textArea.getHighlighter();
		HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(color);

		int start = 0;
		while ((start = text.indexOf(word, start)) != -1) {
			int end = start + word.length();
			Object highlightTag = highlighter.addHighlight(start, end, painter);
			highlightTags.add(highlightTag);
			highlightBookmarks.add(start);
			start++;
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		refreshHighlights();
	}

	private void refreshHighlights() {
		clearHighlights();
		for (String word : wordsToHighlight) {
			try {
				highlight(word);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		refreshHighlights();
	}

}
