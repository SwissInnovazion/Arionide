/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE used to conceive applications and algorithms in a three-dimensional environment. 
 * It is the work of Arion Zimmermann for his final high-school project at Calvin College (Geneva, Switzerland).
 * Copyright (C) 2016-2019 Innovazion. All rights reserved.
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
package ch.innovazion.arionide.lang.natives;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.innovazion.arionide.lang.Data;
import ch.innovazion.arionide.lang.Reference;
import ch.innovazion.arionide.lang.Runtime;
import ch.innovazion.arionide.lang.Specification;
import ch.innovazion.arionide.lang.SpecificationElement;
import ch.innovazion.arionide.lang.Validator;
import ch.innovazion.arionide.lang.natives.instructions.Add;
import ch.innovazion.arionide.lang.natives.instructions.Call;
import ch.innovazion.arionide.lang.natives.instructions.Compare;
import ch.innovazion.arionide.lang.natives.instructions.Define;
import ch.innovazion.arionide.lang.natives.instructions.If;
import ch.innovazion.arionide.lang.natives.instructions.Init;
import ch.innovazion.arionide.lang.natives.instructions.Iterate;
import ch.innovazion.arionide.lang.natives.instructions.Load;
import ch.innovazion.arionide.lang.natives.instructions.Merge;
import ch.innovazion.arionide.lang.natives.instructions.NativeInstruction;
import ch.innovazion.arionide.lang.natives.instructions.Object;
import ch.innovazion.arionide.lang.natives.instructions.Print;
import ch.innovazion.arionide.lang.natives.instructions.Redo;
import ch.innovazion.arionide.lang.natives.instructions.Size;
import ch.innovazion.arionide.lang.natives.instructions.Write;
import ch.innovazion.arionide.project.CodeChain;
import ch.innovazion.arionide.project.HierarchyElement;
import ch.innovazion.arionide.project.Project;
import ch.innovazion.arionide.project.Storage;
import ch.innovazion.arionide.project.StructureMeta;

public class NativeRuntime extends Runtime {
	
	private final NativeDataCommunicator ndc;
	private final List<List<NativeInstruction>> code = new ArrayList<>();
	private final List<String> symbols = new ArrayList<>();
	private final List<Integer> references = new ArrayList<>(); // ID in code list --> Real ID
	
	public NativeRuntime(Project project) {
		super(project);
		
		this.ndc = new NativeDataCommunicator(this, this::info);
	}

	public void run(int id) {
		this.code.clear();
		this.symbols.clear();
		this.references.clear();
		
		this.info("Compiling sources...", 0xFFFF00);
		
		Storage storage = this.getProject().getStorage();
		
		int realID = storage.getHierarchy().get(id).getID();
		
		if(this.compile(realID, "root", storage)) {
			this.info("Compilation succeed", 0x00FF00);
						
			this.info("Running program...", 0xFFAA00);
			
			if(this.code.size() > 0) {
				this.ndc.getStack().push(realID);
				this.ndc.initVariablePool();

				if(this.exec(0)) {
					this.info("Program execution finished with no error", 0x00FF00);
				} else {
					this.info("Program execution finished because of a runtime error", 0xFF0000);
				}
				
				this.ndc.clearVariablePool();
				this.ndc.getStack().pop();
			} else {
				this.info("Nothing to run", 0xFFAA00);	
			}
		} else {
			this.info("Compilation failed", 0xFF0000);	
		}
	}
	
	public boolean exec(int structureID) {
		for(NativeInstruction instruction : this.code.get(structureID)) {
			if(!instruction.execute(this.ndc, this.references)) {
				return false;
			}
		}
		
		return true;
	}
	
	private boolean compile(int realID, String name, Storage storage) {								
		CodeChain elements = storage.getCode().get(realID);
		Map<Integer, StructureMeta> metaData = storage.getStructureMeta();
		List<NativeInstruction> structure = new ArrayList<>();
		List<Integer> nextElements = new ArrayList<>();
		
		StructureMeta structureMeta = metaData.get(realID);
		
		boolean state = true;

		if(structureMeta != null) {
			name += "." + structureMeta.getName();
						
			this.symbols.add(name);
			this.references.add(realID);
									
			for(HierarchyElement element : elements) {
				StructureMeta meta = metaData.get(element.getID());
				
				if(meta != null) {
					Specification spec = meta.getSpecification();					
					NativeInstruction compiled = this.compileInstruction(this.symbols.size(), meta.getName(), spec, nextElements);
					
					if(compiled != null) {
						structure.add(compiled);
					} else {
						this.info("Instruction compilation failed in " + name + " (" + realID + ":" + element.getID() + ")", 0xFF6000);
						state = false;
					}
				} else {
					this.info("Object in " + name + " (" + realID + ":" + element.getID() + ") is not an instruction", 0xFF6000);
					state = false;
				}
			}
		} else {
			this.info("Invalid structure ID: (" + realID + ":?)", 0xFF6000);
			state = false;
		}
		
		this.code.add(structure);
		
		for(Integer next : nextElements) {
			if(!this.compile(next, name, storage)) {
				state = false;
			}
		}
		
		return state;
	}
	
	private NativeInstruction compileInstruction(int symID, String instruction, Specification spec, List<Integer> nextElements) {
		for(SpecificationElement element : spec.getElements()) {
			if(element instanceof Data) {
				Validator validator = this.getProject().getLanguage().getTypes().getValidator(((Data) element).getType());
				
				if(validator == null || !validator.validate(element.getValue())) {
					this.info("Couldn't validate '" + element.getValue() + "'", 0xFF6000);
					return null;
				}
			} else if(element instanceof Reference) {
				try {
					nextElements.add(Integer.parseInt(element.getValue()));
				} catch(NumberFormatException e) {
					this.info("Invalid reference", 0xFF6000);
					return null;
				}
			} else {
				this.info("Strange object detected: " + element.toString(), 0xFF6000);
				return null;
			}
		}
		
		switch(instruction) {
			case "init":
				return new Init();
			case "print":
				return new Print((Data) spec.getElements().get(0));
			case "call":
				return new Call((Reference) spec.getElements().get(0));
			case "defineText":
			case "defineInteger":
			case "defineStructure":
				return new Define((Data) spec.getElements().get(0), (Data) spec.getElements().get(1), (Data) spec.getElements().get(2));
			case "redo":
				return new Redo((Reference) spec.getElements().get(0));
			case "if":
				return new If((Reference) spec.getElements().get(0), (Reference) spec.getElements().get(1), (Reference) spec.getElements().get(2));
			case "compareText":
			case "compareInteger":
			case "compareStructure":
				return new Compare((Data) spec.getElements().get(0), (Data) spec.getElements().get(1), (Data) spec.getElements().get(2));
			case "object":
				return new Object((Data) spec.getElements().get(0), (Data) spec.getElements().get(1), (Reference) spec.getElements().get(2));
			case "addInteger":
			case "addComplex":
				return new Add((Data) spec.getElements().get(0));
			case "write":
				return new Write((Data) spec.getElements().get(0), (Data) spec.getElements().get(1));
			case "iterate":
				return new Iterate((Data) spec.getElements().get(0), (Data) spec.getElements().get(1), (Reference) spec.getElements().get(2), (Data) spec.getElements().get(3));
			case "size":
				return new Size((Data) spec.getElements().get(0), (Data) spec.getElements().get(1));
			case "merge":
				return new Merge((Data) spec.getElements().get(0), (Data) spec.getElements().get(1), (Data) spec.getElements().get(2));
			case "load":
				return new Load((Data) spec.getElements().get(0), (Data) spec.getElements().get(1));
			default:
				this.info("Instruction " + instruction + " is not compilable", 0xFF6000);
				return null;
		}
	}
	
	protected List<Entry<String, Specification>> getCode(int objectID) {
		Storage storage = this.getProject().getStorage();
				
		CodeChain elements = storage.getCode().get(objectID);
		Map<Integer, StructureMeta> metaData = storage.getStructureMeta();
		List<Entry<String, Specification>> code = new ArrayList<>();
		
		for(HierarchyElement element : elements) {
			StructureMeta meta = metaData.get(element.getID());	
			code.add(new SimpleEntry<String, Specification>(meta.getName(), meta.getSpecification()));
		}
		
		return code;
	}
	
	protected void info(String message, int color) {
		int index = 0;
				
		while((index = message.indexOf("@{", index) + 2) > 1) {
			int end = message.indexOf("}", index);
						
			String symbol = message.substring(index, end);
			
			if(Integer.parseInt(symbol) < this.symbols.size()) {
				message = message.replace("@{" + symbol + "}", this.symbols.get(Integer.parseInt(symbol)));
			}
		}
		
		super.info(message, color);
	}
}