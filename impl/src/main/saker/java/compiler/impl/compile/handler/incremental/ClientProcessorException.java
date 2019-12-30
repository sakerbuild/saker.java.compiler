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
