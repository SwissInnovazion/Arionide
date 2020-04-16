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
package ch.innovazion.arionide.lang.avr;

import ch.innovazion.arionide.lang.Environment;
import ch.innovazion.arionide.lang.EvaluationException;
import ch.innovazion.arionide.lang.Language;
import ch.innovazion.arionide.lang.avr.device.AVRSRAM;
import ch.innovazion.arionide.lang.peripherals.DigitalInput;
import ch.innovazion.arionide.lang.peripherals.DigitalOutput;
import ch.innovazion.arionide.lang.peripherals.SRAM;

public class AVREnvironment extends Environment {
	
	private static final int regBegin = 0x00;
	private static final int IOBegin = 0x20;
	private static final int extIOBegin = 0x60;
	private static final int intSRAMEnd = 0xF00; // exclusive
	private static final int SRAMEnd = 0x20000; // exclusive

	private final AVRLanguage lang;
	private final SRAM sram = new AVRSRAM(4096);
	
	protected AVREnvironment(AVRLanguage lang) {
		super(4000000000L);
		this.lang = lang;
				
		registerPeripheral(sram);
		registerPeripheral(new DigitalInput(sram, 8, IOBegin + 0x16));
		registerPeripheral(new DigitalOutput(sram, 8, IOBegin + 0x18));
	}
	
	public void init() {
		super.init();
		
		try {
			sram.set(IOBegin + 0x18, 0xFF);
		} catch (EvaluationException e) {
			e.printStackTrace();
		}
	}

	public Language getLanguage() {
		return lang;
	}
	
	protected void push(int value) throws EvaluationException {
		int sp = getWord(AVRSRAM.SP);
		sram.set(sp, value);
		setWord(AVRSRAM.SP, sp - 1);
	}
	
	protected int pop() throws EvaluationException {
		int sp = getWord(AVRSRAM.SP);
		setWord(AVRSRAM.SP, sp + 1);
		return sram.get(sp + 1);
	}
	
	protected void pushWord(int value) throws EvaluationException {
		int sp = getWord(AVRSRAM.SP);
		setWord(sp - 1, value);
		setWord(AVRSRAM.SP, sp - 2);
	}
	
	protected int popWord() throws EvaluationException {
		int sp = getWord(AVRSRAM.SP);
		setWord(AVRSRAM.SP, sp + 2);
		return getWord(sp + 1);
	}
	
	protected int getWord(int lowAddr) throws EvaluationException {
		int low = sram.get(lowAddr);
		int high = sram.get(lowAddr + 1);
		
		return (high << 8) | low;
	}
	
	protected void setWord(int lowAddr, int value) throws EvaluationException {
		sram.set(lowAddr, value & 0xFF);
		sram.set(lowAddr + 1, (value >> 8) & 0xFF);
	}
	
	protected void executeInterrupt(long address) throws EvaluationException {
		pushWord((int) getProgramCounter().get());
		super.executeInterrupt(address);
	}
	
	protected int getRegister(int id) throws EvaluationException {
		return sram.get(regBegin + id);
	}

	protected int getIO(int id) throws EvaluationException {
		return sram.get(IOBegin + id);
	}
	
	protected int getExtIO(int id) throws EvaluationException {
		return sram.get(extIOBegin + id);
	}
	
	protected int getData(int address) throws EvaluationException {
		return sram.get(address);
	}
	
	protected int getInternalRAMEnd() { // exclusive
		return intSRAMEnd;
	}
	
	protected int getExternalRAMEnd() { // exclusive
		return SRAMEnd;
	}
}