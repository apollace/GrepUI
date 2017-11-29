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
package org.polly.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;

public class ActionManager {
	private final Map<JComponent, JPopupMenu> menus = new HashMap<JComponent, JPopupMenu>();
	private boolean isSeparatorToAdd = false;

	public void addAction(String nameToShow, Action action, JComponent component) {
		final JMenuItem menuItem = new JMenuItem(nameToShow);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.execute();
			}
		});

		JPopupMenu popup = this.menus.get(component);
		if (popup == null) {
			popup = new JPopupMenu();

			component.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.isPopupTrigger()) {
						this.showMenu(e);
					}
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					if (e.isPopupTrigger()) {
						this.showMenu(e);
					}
				}

				private void showMenu(MouseEvent e) {
					final JPopupMenu popup = ActionManager.this.menus.get(component);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			});

			this.menus.put(component, popup);
		}

		if (this.isSeparatorToAdd) {
			popup.add(new JSeparator());
		}
		this.isSeparatorToAdd = false;

		popup.add(menuItem);
	}

	public void addAction(String nameToShow, Action action, JMenu menu) {
		this.addAction(nameToShow, action, menu, (KeyStroke) null);
	}

	public void addAction(String nameToShow, Action action, JMenu menu, JComponent component) {
		this.addAction(nameToShow, action, menu, component, null);
	}

	public void addAction(String nameToShow, Action action, JMenu menu, JComponent component, KeyStroke keyStroke) {
		final boolean isSeparatorToAdd = this.isSeparatorToAdd;
		this.addAction(nameToShow, action, menu, keyStroke);

		this.isSeparatorToAdd = isSeparatorToAdd;
		this.addAction(nameToShow, action, component);
	}

	public void addAction(String nameToShow, Action action, JMenu menu, KeyStroke keyStroke) {
		final JMenuItem menuItem = new JMenuItem(nameToShow);
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				action.execute();
			}
		});

		if (this.isSeparatorToAdd) {
			menu.add(new JSeparator());
		}
		this.isSeparatorToAdd = false;

		if (keyStroke != null) {
			menuItem.setAccelerator(keyStroke);
		}
		menu.add(menuItem);
	}

	public void addSeparator() {
		this.isSeparatorToAdd = true;
	}

}
