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

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SearchWindow extends JDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private final JCheckBox chckbxReverseSearch = new JCheckBox("Reverse search");
	private JTextField txtSearch;

	private JTextArea searchArea;

	private boolean searchDown() {
		return searchDown(searchArea.getSelectionEnd());
	}

	private boolean searchDown(int from) {
		String toSearch = txtSearch.getText();
		String text = searchArea.getText();

		int found = text.indexOf(toSearch, from);
		if (found == -1) {
			return false;
		}

		searchArea.setSelectionStart(found);
		searchArea.setSelectionEnd(found + toSearch.length());
		return true;
	}

	private boolean searchUp() {
		int pos = searchArea.getSelectionStart();
		return searchUp(pos);
	}

	private boolean searchUp(int from) {
		String toSearch = txtSearch.getText();
		String text = searchArea.getText();

		from--;
		if (from <= 0) {
			from = txtSearch.getText().length();
		}
		
		int found = text.lastIndexOf(toSearch, from);
		if (found == -1) {
			return false;
		}

		searchArea.setSelectionStart(found);
		searchArea.setSelectionEnd(found + toSearch.length());
		return true;
	}

	private void search() {
		if (chckbxReverseSearch.isSelected()) {
			if (!searchUp()) {
				searchUp(searchArea.getText().length());
			}
		} else {
			if (!searchDown()) {
				searchDown(0);
			}
		}
	}

	/**
	 * Create the dialog.
	 */
	public SearchWindow(JTextArea textArea) {
		searchArea = textArea;
		setBounds(100, 100, 450, 166);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JLabel lblSearch = new JLabel("Search:");
			contentPanel.add(lblSearch, BorderLayout.NORTH);
		}
		{
			txtSearch = new JTextField();
			contentPanel.add(txtSearch, BorderLayout.CENTER);
			txtSearch.setColumns(10);
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.SOUTH);
			chckbxReverseSearch.setToolTipText("Search from bottom to top");
			panel.add(chckbxReverseSearch);

		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Search");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						search();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	@Override
	public void show() {
		if (searchArea.getSelectedText() != null && searchArea.getSelectedText().length() > 0) {
			txtSearch.setText(searchArea.getSelectedText());
		}
		super.show();
	}

	@Override
	public void setVisible(boolean b) {
		if (b) {
			if (searchArea.getSelectedText() != null && searchArea.getSelectedText().length() > 0) {
				txtSearch.setText(searchArea.getSelectedText());
			}
		}
		super.setVisible(b);
	}
}
