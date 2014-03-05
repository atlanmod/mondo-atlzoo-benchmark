package eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.exceptions;

import java.io.IOException;

public class GenerationException extends Exception {

	private static final long serialVersionUID = 1L;

	public GenerationException(String message) {
		super(message);
	}

	public GenerationException() {
		super();
	}

	public GenerationException(IOException e) {
		super(e.getMessage(), e.getCause());
	}
	
}
