/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.java.compiler.jdk.impl.parser.signature;

import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util9.impl.parser.signature.CompilationUnitSignatureParser9;

public class CompilationUnitSignatureParser extends CompilationUnitSignatureParser9 {

	public CompilationUnitSignatureParser(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

}
