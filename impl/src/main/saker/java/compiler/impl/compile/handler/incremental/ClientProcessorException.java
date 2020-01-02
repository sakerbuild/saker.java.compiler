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
package saker.java.compiler.impl.compile.handler.incremental;

import java.util.Objects;

import saker.java.compiler.impl.compile.handler.invoker.ProcessorDetails;

public class ClientProcessorException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	private ProcessorDetails processor;

	public ClientProcessorException(Throwable cause, ProcessorDetails processor) {
		super(cause);
		Objects.requireNonNull(processor, "processor");
		this.processor = processor;
	}

	public ClientProcessorException(String message, Throwable cause, ProcessorDetails processor) {
		super(message, cause);
		Objects.requireNonNull(processor, "processor");
		this.processor = processor;
	}

	public ProcessorDetails getProcessor() {
		return processor;
	}

	@Override
	public String getMessage() {
		return "(" + processor.getProcessorName() + "): " + super.getMessage();
	}
}
