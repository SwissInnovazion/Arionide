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
package ch.innovazion.arionide.lang.natives.instructions;

import java.util.List;

import ch.innovazion.arionide.lang.Data;
import ch.innovazion.arionide.lang.natives.NativeDataCommunicator;
import ch.innovazion.arionide.lang.natives.NativeTypes;
import ch.innovazion.arionide.lang.symbols.Reference;
import ch.innovazion.arionide.lang.symbols.SpecificationElement;

public class Object implements NativeInstruction {

	private final Data result;
	private final Data structure;
	private final Call constructor;
	
	public Object(Data result, Data structure, Reference constructor) {
		this.result = result;
		this.structure = structure;
		this.constructor = new Call(constructor);
	}
	
	public boolean execute(NativeDataCommunicator communicator, List<Integer> references) {
		if(this.result.getDisplayValue().contains(SpecificationElement.VAR)) {
			String variable = this.result.getDisplayValue().substring(4);
			
			String structure = this.structure.getDisplayValue();
			
			if(structure.startsWith(SpecificationElement.VAR)) {
				structure = communicator.getVariable(structure.substring(4)).getDisplayValue();
			}
			
			String identifier = communicator.allocObject(structure);
			
			communicator.setVariable(variable, true, new Data(variable, identifier, NativeTypes.TEXT));
			
			communicator.bindObject(identifier);
			this.constructor.execute(communicator, references);
			communicator.unbindObject();
			
			if(communicator.getObject(identifier).isConsistent()) {
				return true;
			} else {
				communicator.exception("Inconsitent object");
				return false;
			}
		} else {
			communicator.exception("You can't use a direct value for an object instance");
			return false;
		}
	}
}