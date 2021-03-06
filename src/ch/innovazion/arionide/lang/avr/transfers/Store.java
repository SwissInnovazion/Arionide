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
package ch.innovazion.arionide.lang.avr.transfers;

import java.util.List;

import ch.innovazion.arionide.lang.ApplicationMemory;
import ch.innovazion.arionide.lang.Environment;
import ch.innovazion.arionide.lang.EvaluationException;
import ch.innovazion.arionide.lang.Instruction;
import ch.innovazion.arionide.lang.Skeleton;
import ch.innovazion.arionide.lang.avr.AVREnums;
import ch.innovazion.arionide.lang.avr.device.AVRSRAM;
import ch.innovazion.arionide.lang.symbols.Bit;
import ch.innovazion.arionide.lang.symbols.Enumeration;
import ch.innovazion.arionide.lang.symbols.Node;
import ch.innovazion.arionide.lang.symbols.Numeric;
import ch.innovazion.arionide.lang.symbols.Parameter;
import ch.innovazion.arionide.lang.symbols.Specification;
import ch.innovazion.arionide.lang.symbols.SymbolResolutionException;
import ch.innovazion.arionide.project.StructureModel;
import ch.innovazion.arionide.project.StructureModelFactory;

public class Store extends Instruction {
	
	public void validate(Specification spec, List<String> validationErrors) {
		;
	}

	public void evaluate(Environment env, Specification spec, ApplicationMemory programMemory) throws EvaluationException {		
		Numeric r = (Numeric) ((Enumeration) getConstant(spec, 0)).getValue();
		Node pointerInfo = (Node) ((Enumeration) getConstant(spec, 1)).getValue();

		int rPtr = (int) Bit.toInteger(r.getRawStream());
		
		try {
			AVRSRAM sram = env.getPeripheral("sram");

			Numeric register = (Numeric) pointerInfo.resolve("register");
			Numeric increment = (Numeric) pointerInfo.resolve("increment");
			
			int registerID = (int) Bit.toInteger(register.getRawStream());
			int incrementValue = (int) Bit.toInteger(increment.getRawStream());
			
			int address = sram.getWord(registerID);
			
			if(incrementValue < 0) { // Pre-decrement
				address += incrementValue;
				sram.setWord(registerID, address);	
			}
			
			if(spec.getParameters().size() > 2) {
				Numeric q = (Numeric) getConstant(spec, 2);
				address += (int) Bit.toInteger(q.getRawStream());
			}
			
			int value = sram.getRegister(rPtr);
			
			sram.setData(address, value);
			
			if(incrementValue > 0) { // Post-increment
				address += incrementValue;
				sram.setWord(registerID, address);
			}
		} catch (SymbolResolutionException e) {
			throw new EvaluationException("Corrupted pointer enum. Check 'AVREnums.java'");
		}
		
		env.getProgramCounter().incrementAndGet();
		env.getClock().incrementAndGet();
		env.getClock().incrementAndGet();
	}

	public Node assemble(Specification spec, Skeleton skeleton, List<String> assemblyErrors) {
		return new Numeric(0).cast(16);
	}

	public StructureModel createStructureModel() {
		return StructureModelFactory
			.draft("st")
			.withColor(0.31f)
			.withComment("Stores a value from a register into the SRAM")
			.beginSignature("Standard addressing")
				.withParameter(new Parameter("Pointer").asConstant(AVREnums.POINTER))
				.withParameter(new Parameter("Source").asConstant(AVREnums.REGISTER))
			.endSignature()
			.beginSignature("Displacement addressing")
				.withParameter(new Parameter("Pointer").asConstant(AVREnums.DISP_POINTER))
				.withParameter(new Parameter("Displacement").asConstant(new Numeric(0).cast(6)))
				.withParameter(new Parameter("Source").asConstant(AVREnums.REGISTER))
			.endSignature()
			.build();
	}

	public int getLength() {
		return 2;
	}
}
