/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE used to conceive applications and algorithms in a three-dimensional environment. 
 * It is the work of Arion Zimmermann for his final high-school project at Calvin College (Geneva, Switzerland).
 * Copyright (C) 2016-2020 Innovazion. All rights reserved.
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
package ch.innovazion.arionide.menu;

import ch.innovazion.arionide.menu.code.CodeBrowser;
import ch.innovazion.arionide.menu.code.CodeEditor;
import ch.innovazion.arionide.menu.params.ParameterCreator;
import ch.innovazion.arionide.menu.params.ParameterEditor;
import ch.innovazion.arionide.menu.params.ParameterSelector;
import ch.innovazion.arionide.menu.params.assign.InformationAssigner;
import ch.innovazion.arionide.menu.params.assign.ReferenceAssigner;
import ch.innovazion.arionide.menu.params.assign.VariableAssigner;
import ch.innovazion.arionide.menu.structure.CommentEditor;
import ch.innovazion.arionide.menu.structure.LanguageSelector;
import ch.innovazion.arionide.menu.structure.StructureBrowser;
import ch.innovazion.arionide.menu.structure.StructureEditor;
import ch.innovazion.arionide.menu.structure.TintSelector;
import ch.innovazion.automaton.StateHierarchy;
import ch.innovazion.automaton.StateManager;

public class MenuHierarchy extends StateHierarchy {
	
	protected RootMenu root;
	protected StructureBrowser structureBrowser;
	protected CodeBrowser codeBrowser;

	protected void registerStates(StateManager mgr) {
		assert mgr instanceof MenuManager;
		
		MenuManager manager = (MenuManager) mgr;
		
		register("/", root = new RootMenu(manager));
		
		register("/structure", structureBrowser = new StructureBrowser(manager));
		register("/structure/edit", new StructureEditor(manager));
		register("/structure/edit/specify", new ParameterSelector(manager, true));
		register("/structure/edit/specify/create", new ParameterCreator(manager));
		register("/structure/edit/specify/edit", new ParameterEditor(manager));
		register("/structure/edit/comment", new CommentEditor(manager));
		register("/structure/edit/language", new LanguageSelector(manager));
		register("/structure/edit/tint", new TintSelector(manager));

		register("/code", codeBrowser = new CodeBrowser(manager));
		register("/code/append", new CodeEditor(manager));
		register("/code/specify", new ParameterSelector(manager, false));
		
		register("/assign/information", new InformationAssigner(manager));
		register("/assign/variable", new VariableAssigner(manager));
		register("/assign/reference", new ReferenceAssigner(manager));
	}
	
	protected Menu resolveCurrentState() {
		return (Menu) super.resolveCurrentState();
	}
}