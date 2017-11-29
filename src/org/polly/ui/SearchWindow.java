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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class SearchWindow extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private final JCheckBox chckbxReverseSearch;
	private JTextField txtSearch;
	private JPanel searchBoxPanel;

	private final JTextArea searchArea;

	/**
	 * Create the dialog.
	 */
	public SearchWindow(JFrame owner, JTextArea textArea) {
		super(owner);
		this.setResizable(false);
		this.setTitle("Search");

		owner.addWindowListener(new WindowAdapter() {
			@Override
			public void windowGainedFocus(WindowEvent e) {
				owner.setAlwaysOnTop(true);
				super.windowGainedFocus(e);
			}

			@Override
			public void windowLostFocus(WindowEvent e) {
				owner.setAlwaysOnTop(false);
				super.windowLostFocus(e);
			}
		});

		this.searchArea = textArea;
		this.setBounds(100, 100, 450, 166);
		this.getContentPane().setLayout(new BorderLayout());
		this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.getContentPane().add(this.contentPanel, BorderLayout.CENTER);
		this.contentPanel.setLayout(new BorderLayout(0, 0));
		{
			final JLabel lblSearch = new JLabel("Search:");
			this.contentPanel.add(lblSearch, BorderLayout.NORTH);
		}
		{
			this.searchBoxPanel = new JPanel();
			this.searchBoxPanel.setLayout(new BorderLayout());
			this.txtSearch = new JTextField();
			this.searchBoxPanel.add(this.txtSearch, BorderLayout.NORTH);
			this.contentPanel.add(this.searchBoxPanel, BorderLayout.CENTER);

			this.chckbxReverseSearch = new JCheckBox("Reverse search");
			this.searchBoxPanel.add(this.chckbxReverseSearch, BorderLayout.SOUTH);
			this.chckbxReverseSearch.setToolTipText("Search from bottom to top");
		}
		{
			final JPanel panel = new JPanel();
			this.contentPanel.add(panel, BorderLayout.SOUTH);

		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			this.getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				final JButton okButton = new JButton("Search");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						SearchWindow.this.search();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				this.getRootPane().setDefaultButton(okButton);
			}
		}
	}

	private void search() {
		if (this.chckbxReverseSearch.isSelected()) {
			if (!this.searchUp()) {
				this.searchUp(this.searchArea.getText().length());
			}
		} else {
			if (!this.searchDown()) {
				this.searchDown(0);
			}
		}
	}

	private boolean searchDown() {
		return this.searchDown(this.searchArea.getSelectionEnd());
	}

	private boolean searchDown(int from) {
		final String toSearch = this.txtSearch.getText();
		final String text = this.searchArea.getText();

		final int found = text.indexOf(toSearch, from);
		if (found == -1) {
			return false;
		}

		this.searchArea.setSelectionStart(found);
		this.searchArea.setSelectionEnd(found + toSearch.length());
		return true;
	}

	private boolean searchUp() {
		final int pos = this.searchArea.getSelectionStart();
		return this.searchUp(pos);
	}

	private boolean searchUp(int from) {
		final String toSearch = this.txtSearch.getText();
		final String text = this.searchArea.getText();

		from--;
		if (from <= 0) {
			from = this.txtSearch.getText().length();
		}

		final int found = text.lastIndexOf(toSearch, from);
		if (found == -1) {
			return false;
		}

		this.searchArea.setSelectionStart(found);
		this.searchArea.setSelectionEnd(found + toSearch.length());
		return true;
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			if (this.searchArea.getSelectedText() != null && this.searchArea.getSelectedText().length() > 0) {
				this.txtSearch.setText(this.searchArea.getSelectedText());
			}
		}
		super.setVisible(b);
	}

	@Override
	public void show() {
		if (this.searchArea.getSelectedText() != null && this.searchArea.getSelectedText().length() > 0) {
			this.txtSearch.setText(this.searchArea.getSelectedText());
		}
		super.show();
	}
}
