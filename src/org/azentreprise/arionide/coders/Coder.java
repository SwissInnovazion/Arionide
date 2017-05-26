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
 * The copy of the GNU General Public License can be found in the 'LICENSE.txt' file inside the JAR archive or in your personal directory as 'Arionide/LICENSE.txt'.
 *******************************************************************************/
package org.azentreprise.arionide.coders;

import java.nio.charset.Charset;

public interface Coder<T> {
	
	public static final Charset charset = Charset.forName("utf8");
	
	
	public static final Encoder<String> stringEncoder = new StringEncoder();
	public static final Decoder<String> stringDecoder = new StringDecoder();

	public static final Encoder<Long> integerEncoder = new IntegerEncoder();
	public static final Decoder<Long> integerDecoder = new IntegerDecoder();
	
	/* We may change this if the encoding takes more than one byte for a character */
	public static final byte sectionStart = new String("{").getBytes(Coder.charset)[0];
	public static final byte sectionEnd = new String("}").getBytes(Coder.charset)[0];
	public static final byte separator = new String(":").getBytes(Coder.charset)[0];
	
	public static final String whitespaceRegex = "\\s";
	
	public static final String empty = new String();
	public static final byte space = new String(" ").getBytes(Coder.charset)[0];
	public static final byte tab = new String("\t").getBytes(Coder.charset)[0];
	public static final byte newline = new String("\n").getBytes(Coder.charset)[0];

	
	public int getVersionUID();
	public int getBackwardCompatibileVersionUID();
}