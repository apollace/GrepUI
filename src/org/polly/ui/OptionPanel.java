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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.polly.persistency.Option;

public class OptionPanel extends JPanel {
	public enum HideButtnPosition {
		TOP, BOTTOM, LEFT, RIGHT, HIDE
	}

	private static final long serialVersionUID = 1L;
	private static final String SHOW_MORE_TEXT = "Show more";

	private static final String SHOW_LESS_TEXT = "Show less";

	private static final Map<HideButtnPosition, String> hideButtonPositionMapping;
	static {
		hideButtonPositionMapping = new TreeMap<HideButtnPosition, String>();
		hideButtonPositionMapping.put(OptionPanel.HideButtnPosition.TOP, BorderLayout.NORTH);
		hideButtonPositionMapping.put(OptionPanel.HideButtnPosition.BOTTOM, BorderLayout.SOUTH);
		hideButtonPositionMapping.put(OptionPanel.HideButtnPosition.LEFT, BorderLayout.WEST);
		hideButtonPositionMapping.put(OptionPanel.HideButtnPosition.RIGHT, BorderLayout.EAST);
	}
	private boolean isOptionsVisible = false;
	private final Collection<Option> options;

	public OptionPanel(Collection<Option> options) {
		this(HideButtnPosition.BOTTOM, options);
	}

	/**
	 * Create the panel.
	 */
	public OptionPanel(HideButtnPosition buttonPosition, Collection<Option> options) {
		this.options = options;

		final JPanel moreOptionPanel = new JPanel();
		this.setLayout(new BorderLayout());

		final JPanel allOptions = new JPanel();
		allOptions.setLayout(new BorderLayout());
		this.add(allOptions, BorderLayout.CENTER);

		final JPanel alwaysVisibleOption = new JPanel();
		allOptions.add(alwaysVisibleOption, BorderLayout.CENTER);

		allOptions.add(moreOptionPanel, BorderLayout.SOUTH);
		moreOptionPanel.setVisible(this.isOptionsVisible);

		for (final Option option : options) {
			final JPanel localPanel = new JPanel();
			localPanel.setLayout(new BorderLayout());

			this.addOptionName(option, localPanel);
			this.addOptionTextField(option, localPanel);
			this.addOptionDescription(option, localPanel);
			this.addOptionToPanel(moreOptionPanel, alwaysVisibleOption, option, localPanel);
		}

		alwaysVisibleOption.setLayout(new GridLayout(alwaysVisibleOption.getComponentCount(), 1));
		moreOptionPanel.setLayout(new GridLayout(moreOptionPanel.getComponentCount(), 1));

		this.addHideShowButton(buttonPosition, moreOptionPanel);
	}

	private void addHideShowButton(HideButtnPosition buttonPosition, JPanel moreOptionPanel) {
		if (buttonPosition != HideButtnPosition.HIDE && moreOptionPanel.getComponentCount() > 0) {
			final JButton btnShowhidebutton = new JButton(SHOW_MORE_TEXT);

			final String position = hideButtonPositionMapping.get(buttonPosition);
			this.add(btnShowhidebutton, position);

			btnShowhidebutton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					OptionPanel.this.isOptionsVisible = !OptionPanel.this.isOptionsVisible;
					moreOptionPanel.setVisible(OptionPanel.this.isOptionsVisible);
					OptionPanel.this.repaint();

					if (OptionPanel.this.isOptionsVisible) {
						btnShowhidebutton.setText(SHOW_LESS_TEXT);
					} else {
						btnShowhidebutton.setText(SHOW_MORE_TEXT);
					}
				}
			});
		}
	}

	private void addOptionDescription(Option option, JPanel localPanel) {
		final String description = option.getDescription();

		// The <html> is used to enable the auto-wrapping
		final JLabel optionDescription = new JLabel("<html>Key(" + option.getKey() + ") " + description + "</html>");

		// Resize and change the description color in order to make the look
		// more fancy
		final Font optionDescriptionFont = optionDescription.getFont();
		optionDescription.setFont(
				new Font(optionDescriptionFont.getName(), Font.PLAIN, (int) (optionDescriptionFont.getSize() * 0.9)));
		optionDescription.setForeground(Color.DARK_GRAY);

		optionDescription.setMaximumSize(new Dimension(250, 0));
		optionDescription.setPreferredSize(new Dimension(250, 0));
		localPanel.add(optionDescription, BorderLayout.EAST);
	}

	private void addOptionName(Option option, JPanel localPanel) {
		final String nameToShow = option.getNameToShow();
		final JLabel optionName = new JLabel("<html>" + nameToShow + "</html>");
		optionName.setToolTipText(nameToShow);

		optionName.setMaximumSize(new Dimension(130, 0));
		optionName.setPreferredSize(new Dimension(130, 0));
		localPanel.add(optionName, BorderLayout.WEST);
	}

	private void addOptionTextField(Option option, JPanel localPanel) {
		final JTextField optionValue = new JTextField();
		optionValue.setText(option.getDefaultValue());
		localPanel.add(optionValue, BorderLayout.CENTER);

		optionValue.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {
				option.setLastValue(optionValue.getText());
			}

			@Override
			public void keyTyped(KeyEvent e) {

			}
		});
	}

	private void addOptionToPanel(JPanel moreOptionPanel, JPanel alwaysVisibleOption, Option option,
			JPanel localPanel) {
		if (option.getVisibilityMode() == Option.VisibilityMode.VISIBLE_ALWAYS) {
			alwaysVisibleOption.add(localPanel);
		} else if (option.getVisibilityMode() == Option.VisibilityMode.VISIBLE_ON_DEMAND) {
			moreOptionPanel.add(localPanel);
		}
	}

	public Collection<Option> getOptions() {
		return this.options;
	}

}
