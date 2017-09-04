/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE whose purpose is to build a language from scratch. It is the work of Arion Zimmermann in context of his TM.
 * Copyright (C) 2017 AZEntreprise Corporation. All rights reserved.
 *
 * Arionide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Arionide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Arionide.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The copy of the GNU General Public License can be found in the 'LICENSE.txt' file inside the src directory or inside the JAR archive.
 *******************************************************************************/
package org.azentreprise.arionide.ui.menu.edition;

import javax.swing.JOptionPane;

import org.azentreprise.arionide.events.MessageEvent;
import org.azentreprise.arionide.events.MessageType;
import org.azentreprise.arionide.project.Project;
import org.azentreprise.arionide.ui.AppManager;
import org.azentreprise.arionide.ui.core.CoreRenderer;
import org.azentreprise.arionide.ui.core.opengl.OpenGLCoreRenderer;
import org.azentreprise.arionide.ui.menu.ConfirmMenu;
import org.azentreprise.arionide.ui.menu.MainMenus;
import org.azentreprise.arionide.ui.menu.SpecificMenu;

public class StructureEdition extends SpecificMenu {
		
	private static final String go = "Go";
	private static final String inheritance = "Inherit";
	private static final String name = "Name";
	private static final String color = "Color";
	private static final String delete = "Delete";
	private static final String close = "Close";

	private final Inheritance inheritanceMenu;
	private final Coloring coloring;
	private final ConfirmMenu confirmDelete;

	public StructureEdition(AppManager manager) {
		super(manager, go, inheritance, name, color, delete, close);
		this.coloring = new Coloring(manager);
		this.confirmDelete = new ConfirmMenu(manager, this, this::delete, "Are you sure you want to delete the structure '$name'?");
		this.inheritanceMenu = new Inheritance(manager);
	}
	
	public Coloring getColoring() {
		return this.coloring;
	}

	protected void onClick(String element) {
		assert this.getCurrent() != null;
		
		this.setCurrentID(0);
		
		switch(element) {
			case go:
				this.go();
				break;
			case inheritance:
				this.inheritanceMenu.setCurrent(this.getCurrent());
				this.show(this.inheritanceMenu);
				break;
			case name:
				new Thread(() -> {
					String name = JOptionPane.showInputDialog(null, "Please enter the new name of the structure", "New name", JOptionPane.PLAIN_MESSAGE);
					
					if(name != null) {
						Project project = this.getManager().getWorkspace().getCurrentProject();
						MessageEvent message = project.getDataManager().setName(this.getCurrent().getID(), name, this.getManager().getCoreRenderer().getInside());
						this.getManager().getCoreRenderer().loadProject(this.getManager().getWorkspace().getCurrentProject());
						this.getManager().getEventDispatcher().fire(message);
					}
				}).start();
				break;
			case color:
				this.coloring.setCurrent(this.getCurrent());
				this.show(this.coloring);
				break;
			case delete:
				this.confirmDelete.setCurrent(this.getCurrent());
				this.show(this.confirmDelete);
				break;
			case close:
				this.show(MainMenus.STRUCT_LIST);
		}
	}
	
	private void delete() {
		Project project = this.getManager().getWorkspace().getCurrentProject();
		MessageEvent message = project.getDataManager().deleteStructure(this.getCurrent().getID(), this.getManager().getCoreRenderer().getInside());
		this.getManager().getCoreRenderer().loadProject(project);
		this.getManager().getEventDispatcher().fire(message);
	}
	
	private void go() {
		CoreRenderer renderer = this.getManager().getCoreRenderer();
		
		if(renderer instanceof OpenGLCoreRenderer) {
			((OpenGLCoreRenderer) renderer).teleport(this.getCurrent().getCenter());
		} else {
			this.getManager().getEventDispatcher().fire(new MessageEvent("This GUI implementation doesn't support teleporting.", MessageType.ERROR));
		}
	}
}