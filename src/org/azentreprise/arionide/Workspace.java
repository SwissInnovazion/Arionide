/*******************************************************************************
 * This file is part of ArionIDE.
 *
 * ArionIDE is an IDE whose purpose is to build a language from assembly. It is the work of Arion Zimmermann in context of his TM.
 * Copyright (C) 2017 AZEntreprise Corporation. All rights reserved.
 *
 * ArionIDE is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ArionIDE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ArionIDE.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The copy of the GNU General Public License can be found in the 'LICENSE.txt' file inside the JAR archive.
 *******************************************************************************/
package org.azentreprise.arionide;

import java.io.IOException;
import java.util.List;

import org.azentreprise.arionide.debugging.IAm;

public interface Workspace extends Resource, MappedStructure, Saveable, Loadable {
	
	public List<? super Project> getProjectList();
	
	@IAm("getting the list of available projects")
	public Project getCurrentProject();
	
	
	@IAm("loading a project")
	public void loadProject(Project project);
	
	@IAm("closing a project")
	public void closeProject(Project project);
	
	
	@IAm("creating a project")
	public void createProject(String name) throws IOException;
	
	@IAm("deleting a project")
	public void deleteProject(Project project);
}
