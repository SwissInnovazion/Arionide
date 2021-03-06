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
package ch.innovazion.arionide.menu.structure;

import java.util.List;

import ch.innovazion.arionide.events.GeometryInvalidateEvent;
import ch.innovazion.arionide.lang.Language;
import ch.innovazion.arionide.lang.LanguageManager;
import ch.innovazion.arionide.menu.Menu;
import ch.innovazion.arionide.menu.MenuDescription;
import ch.innovazion.arionide.menu.MenuManager;
import ch.innovazion.arionide.project.mutables.MutableStructure;
import ch.innovazion.arionide.ui.overlay.View;
import ch.innovazion.arionide.ui.overlay.Views;
import ch.innovazion.automaton.Inherit;

public class LanguageSelector extends Menu {

	@Inherit
	protected MutableStructure target;
	
	public LanguageSelector(MenuManager manager) {
		super(manager, "None");
	}
	
	protected void onEnter() {
		super.onEnter();
		
		List<String> languages = LanguageManager.getAvailableLanguages();
				
		setDynamicElements(languages.toArray(new String[0]));
		updateCursor(1 + languages.indexOf(target.getLanguage()));
	}
	
	protected void updateCursor(int cursor) {
		super.updateCursor(cursor);
		
		Language lang = LanguageManager.get(selection);
		
		if(id > 0) {
			if(lang != null) {
				this.description = new MenuDescription(lang.getVendorUID());
			} else {
				this.description = new MenuDescription("Invalid language");
			}
		} else {
			this.description = new MenuDescription("Abstractify this structure");
		}
	}

	public void onAction(String action) {
		if(id == 0) {
			Views.confirm.setPrimaryText("Are you sure you want to abstractify this structure?")
						 .setSecondaryText("All the code inside the structure will be deleted")
					     .setButtons("Yes", "Cancel")
					     .react("Yes", this::abstractify)
					     .react("Cancel", View::discard)
					     .stackOnto(Views.code);
		} else {
			setLanguage(action);
		}
	}
	
	private void abstractify(View view) {
		view.discard();
		setLanguage(null);
	}
	
	private void setLanguage(String language) {
		dispatch(project.getStructureManager().setLanguage(target.getIdentifier(), language));
		dispatch(new GeometryInvalidateEvent(2));
		go("..");
	}
}
