package org.azentreprise.arionide.lang.natives.instructions;

import java.util.List;

import org.azentreprise.arionide.lang.natives.NativeDataCommunicator;

public class Init implements NativeInstruction {
	public boolean execute(NativeDataCommunicator communicator, List<Integer> references) {
		return true;
	}
}