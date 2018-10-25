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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import org.polly.actions.Action;
import org.polly.actions.ActionManager;
import org.polly.actions.concrete.KillLastRunnedCommand;
import org.polly.actions.concrete.RunCommand;
import org.polly.actions.concrete.ShowSearchWindow;
import org.polly.actions.concrete.TextAreaAddHighlight;
import org.polly.actions.concrete.TextAreaClearHighlights;
import org.polly.actions.concrete.TextAreaGoToNextHighlight;
import org.polly.persistency.Option;

public class GrepUIPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private static final Collection<Option> defaultOptions;
	private static final Collection<Integer> availableKeysForShorcut;
	private static final Collection<HighlightColor> defaultHighlights;
	static {
		defaultHighlights = new Vector<HighlightColor>(3);
		defaultHighlights.add(new HighlightColor("Yelow", Color.YELLOW));
		defaultHighlights.add(new HighlightColor("Green", Color.GREEN));
		defaultHighlights.add(new HighlightColor("Red", Color.RED));
		defaultHighlights.add(new HighlightColor("Cyan", Color.CYAN));

		availableKeysForShorcut = new Vector<Integer>();
		availableKeysForShorcut.add(KeyEvent.VK_F1);
		availableKeysForShorcut.add(KeyEvent.VK_F2);
		availableKeysForShorcut.add(KeyEvent.VK_F3);
		availableKeysForShorcut.add(KeyEvent.VK_F4);

		defaultOptions = new Vector<Option>();
		defaultOptions.add(new Option("pattern", "Pattern", "", "The patter to use into the grep command",
				Option.VisibilityMode.VISIBLE_ALWAYS));
		defaultOptions.add(new Option("filename", "Filename", "",
				"The file/s to grep, it accept the wildcard * in order to select multiple files",
				Option.VisibilityMode.VISIBLE_ALWAYS));
		defaultOptions.add(new Option("additionalOption", "Additional option", "", "Grep additional option",
				Option.VisibilityMode.VISIBLE_ON_DEMAND));

		defaultOptions.add(new Option(RunCommand.command, "Command", "grep \"${pattern}\" ${additionalOption} ${filename} ",
				Option.VisibilityMode.VISIBLE_ON_DEMAND));

		defaultOptions.add(new Option(RunCommand.homePath, "Home", System.getProperty("user.home"),
				Option.VisibilityMode.VISIBLE_ON_DEMAND));

		defaultOptions.add(new Option(RunCommand.outFilename, "Output file", "grepui.out",
				Option.VisibilityMode.VISIBLE_ON_DEMAND));

		defaultOptions.add(new Option(RunCommand.maxFileLineToRead, "Max line", "5000",
				"Maximum number of line to reads from output", Option.VisibilityMode.VISIBLE_ON_DEMAND));

	}

	private Collection<Highlighter> highlighters;

	private ActionManager actionManager;
	private JScrollPane logScrollPane;
	private JTextArea logArea;
	private JPanel grepPanel;
	private OptionPanel grepOptionPanel;
	private JMenuBar menuBar;
	private JMenu highlightsMenu;
	private JMenu editMenu;

	public GrepUIPanel(JMenuBar menuBar) {
		this(menuBar, defaultHighlights);
	}

	public GrepUIPanel(JMenuBar menuBar, Collection<HighlightColor> highlights) {
		this.initUI(menuBar, highlights);
	}

	private void initUI(JMenuBar menuBar, Collection<HighlightColor> highlights) {
		this.setLayout(new BorderLayout(0, 0));

		this.actionManager = new ActionManager();

		this.menuBar = menuBar;
		this.editMenu = new JMenu("Edit");
		this.menuBar.add(this.editMenu);

		this.highlightsMenu = new JMenu("Highlights");
		this.menuBar.add(this.highlightsMenu);

		this.grepPanel = new JPanel();
		this.add(this.grepPanel, BorderLayout.NORTH);
		this.grepPanel.setLayout(new BorderLayout(0, 0));

		this.grepOptionPanel = new OptionPanel(defaultOptions);
		this.grepPanel.add(this.grepOptionPanel, BorderLayout.SOUTH);

		this.logScrollPane = new JScrollPane();
		this.add(this.logScrollPane, BorderLayout.CENTER);

		this.logArea = new JTextArea();
		this.logArea.setEditable(false);
		this.logScrollPane.setViewportView(this.logArea);

		final Action executeCommand = new RunCommand(this.logArea, this.grepOptionPanel.getOptions());
		KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		this.actionManager.addAction("Run", executeCommand, this.editMenu, this.logArea, keyStroke);

		final Action killCommand = new KillLastRunnedCommand();
		this.actionManager.addAction("Kill last run", killCommand, this.editMenu, this.logArea);

		final Action showSearchWindow = new ShowSearchWindow(this.logArea);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		this.actionManager.addAction("Find", showSearchWindow, this.editMenu, this.logArea, keyStroke);

		this.actionManager.addSeparator();

		final Iterator<Integer> keyIt = availableKeysForShorcut.iterator();
		this.highlighters = new ArrayList<Highlighter>();
		for (final HighlightColor highlightColor : highlights) {
			// Build highlighter
			final Highlighter highlighter = new Highlighter(this.logArea, highlightColor.getColor());
			this.highlighters.add(highlighter);

			// Add related menu items
			keyStroke = null;
			final int currentShortcutKey = keyIt.next();

			final Action highlight = new TextAreaAddHighlight(this.logArea, highlighter);
			keyStroke = KeyStroke.getKeyStroke(currentShortcutKey,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
			this.actionManager.addAction("Highlight wth " + highlightColor.getName(), highlight, this.highlightsMenu,
					this.logArea, keyStroke);

			final Action clearHighlight = new TextAreaClearHighlights(highlighter);
			keyStroke = KeyStroke.getKeyStroke(currentShortcutKey,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + InputEvent.SHIFT_MASK);
			this.actionManager.addAction("Clear " + highlightColor.getName(), clearHighlight, this.highlightsMenu,
					this.logArea, keyStroke);

			final Action gotoNextHighlight = new TextAreaGoToNextHighlight(highlighter);
			keyStroke = KeyStroke.getKeyStroke(currentShortcutKey, 0);
			this.actionManager.addAction("Go to next " + highlightColor.getName(), gotoNextHighlight,
					this.highlightsMenu, this.logArea, keyStroke);

			this.actionManager.addSeparator();
		}
	}
}
