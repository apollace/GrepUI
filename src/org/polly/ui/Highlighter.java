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
	private final JTextArea textArea;
	private final Color color;
	private final Collection<String> wordsToHighlight;
	private final Collection<Object> highlightTags;
	private final Collection<Integer> highlightBookmarks;

	Highlighter(JTextArea textArea, Color color) {
		this.textArea = textArea;
		this.color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);
		this.wordsToHighlight = new ArrayList<String>();
		this.highlightTags = new ArrayList<Object>();
		this.highlightBookmarks = new ArrayList<Integer>();

		textArea.getDocument().addDocumentListener(this);
	}

	public void add(String word) throws Exception {
		if (word == null || word.length() == 0 || this.wordsToHighlight.contains(word)) {
			return;
		}

		this.wordsToHighlight.add(word);
		this.highlight(word);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		this.refreshHighlights();
	}

	public void clear() {
		this.wordsToHighlight.clear();
		this.clearHighlights();
		this.highlightBookmarks.clear();
	}

	private void clearHighlights() {
		for (final Object tag : this.highlightTags) {
			this.textArea.getHighlighter().removeHighlight(tag);
		}
		this.highlightTags.clear();
	}

	public void gotoNextHighlight() {
		if (this.highlightBookmarks.isEmpty()) {
			return;
		}

		final int currentPosition = this.textArea.getSelectionEnd();

		int firstBookmarkPosition = -1;
		int lastBookmarkPosition = 0;
		final Iterator<Integer> currentBookmark = this.highlightBookmarks.iterator();
		while (currentBookmark.hasNext()) {
			final int currentBookmarkPosition = currentBookmark.next();

			if (firstBookmarkPosition < 0) {
				firstBookmarkPosition = currentBookmarkPosition;
			}

			lastBookmarkPosition = currentBookmarkPosition;
			if (currentBookmarkPosition > currentPosition) {
				this.selectCurrentLine(currentBookmarkPosition);
				break;
			}
		}

		if (lastBookmarkPosition <= currentPosition) {
			this.selectCurrentLine(firstBookmarkPosition);
		}
	}

	private void highlight(String word) throws BadLocationException {
		final String text = this.textArea.getText();
		final javax.swing.text.Highlighter highlighter = this.textArea.getHighlighter();
		final HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(this.color);

		int start = 0;
		while ((start = text.indexOf(word, start)) != -1) {
			final int end = start + word.length();
			final Object highlightTag = highlighter.addHighlight(start, end, painter);
			this.highlightTags.add(highlightTag);
			this.highlightBookmarks.add(start);
			start++;
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		this.refreshHighlights();
	}

	private void refreshHighlights() {
		this.clearHighlights();
		for (final String word : this.wordsToHighlight) {
			try {
				this.highlight(word);
			} catch (final BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		this.refreshHighlights();
	}

	private void selectCurrentLine(int currentBookmarkPosition) {
		int lineStart = currentBookmarkPosition;
		while (lineStart > 0 && this.textArea.getText().charAt(lineStart) != '\n') {
			lineStart--;
		}

		int lineEnd = currentBookmarkPosition;
		while (lineEnd < this.textArea.getText().length() && this.textArea.getText().charAt(lineEnd) != '\n') {
			lineEnd++;
		}

		this.textArea.select(lineStart, lineEnd);
	}

}
