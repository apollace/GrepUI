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

import javax.swing.JPanel;
import java.awt.BorderLayout;
import javax.swing.JToolBar;
import javax.swing.JTextPane;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Color;
import javax.swing.border.LineBorder;

import org.polly.actions.Action;
import org.polly.actions.ActionManager;
import org.polly.actions.concrete.RunCommand;
import org.polly.actions.concrete.ShowSearchWindow;
import org.polly.actions.concrete.TextAreaAddHighlight;
import org.polly.actions.concrete.TextAreaClearHighlights;
import org.polly.actions.concrete.TextAreaGoToNextHighlight;
import org.polly.persistency.Option;

import javax.swing.JPopupMenu;
import javax.swing.JPopupMenu.Separator;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import javax.swing.JComboBox;
import javax.swing.JButton;

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

		defaultOptions.add(new Option(RunCommand.command, "Command", "grep ${pattern} ${additionalOption} ${filename} ",
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

	/**
	 * Create the panel.
	 * 
	 * @throws IOException
	 */
	public GrepUIPanel(JMenuBar menuBar, Collection<HighlightColor> highlights) {
		initUI(menuBar, highlights);
	}

	private void initUI(JMenuBar menuBar, Collection<HighlightColor> highlights) {
		setLayout(new BorderLayout(0, 0));

		actionManager = new ActionManager();

		this.menuBar = menuBar;
		editMenu = new JMenu("Edit");
		this.menuBar.add(editMenu);

		highlightsMenu = new JMenu("Highlights");
		this.menuBar.add(highlightsMenu);

		grepPanel = new JPanel();
		add(grepPanel, BorderLayout.NORTH);
		grepPanel.setLayout(new BorderLayout(0, 0));

		grepOptionPanel = new OptionPanel(defaultOptions);
		grepPanel.add(grepOptionPanel, BorderLayout.SOUTH);

		logScrollPane = new JScrollPane();
		add(logScrollPane, BorderLayout.CENTER);

		logArea = new JTextArea();
		logArea.setEditable(false);
		logScrollPane.setViewportView(logArea);

		Action executeCommand = new RunCommand(logArea, grepOptionPanel.getOptions());
		KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0);
		actionManager.addAction("Run", executeCommand, editMenu, logArea, keyStroke);

		Action showSearchWindow = new ShowSearchWindow(logArea);
		keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		actionManager.addAction("Find", showSearchWindow, editMenu, logArea, keyStroke);
		
		actionManager.addSeparator();
		
		Iterator<Integer> keyIt = availableKeysForShorcut.iterator();
		highlighters = new ArrayList<Highlighter>();
		for (HighlightColor highlightColor : highlights) {
			// Build highlighter
			Highlighter highlighter = new Highlighter(logArea, highlightColor.getColor());
			highlighters.add(highlighter);

			// Add related menu items
			keyStroke = null;
			int currentShortcutKey = keyIt.next();

			Action highlight = new TextAreaAddHighlight(logArea, highlighter);
			keyStroke = KeyStroke.getKeyStroke(currentShortcutKey,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
			actionManager.addAction("Highlight wth " + highlightColor.getName(), highlight, highlightsMenu, logArea,
					keyStroke);

			Action clearHighlight = new TextAreaClearHighlights(highlighter);
			keyStroke = KeyStroke.getKeyStroke(currentShortcutKey,
					Toolkit.getDefaultToolkit().getMenuShortcutKeyMask() + InputEvent.SHIFT_MASK);
			actionManager.addAction("Clear " + highlightColor.getName(), clearHighlight, highlightsMenu, logArea,
					keyStroke);

			Action gotoNextHighlight = new TextAreaGoToNextHighlight(highlighter);
			keyStroke = KeyStroke.getKeyStroke(currentShortcutKey, 0);
			actionManager.addAction("Go to next " + highlightColor.getName(), gotoNextHighlight, highlightsMenu,
					logArea, keyStroke);

			actionManager.addSeparator();
		}
	}
}
