/*******************************************************************************
 * Copyright (c) 2015 agomez
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * agomez - initial API and implementation
 *******************************************************************************/

package fr.inria.instantiator;


/**
 * Generation Exception
 * @author agomez
 *
 */
public class GenerationException extends Exception {

	private static final long serialVersionUID = 1L;

	public GenerationException(String message) {
		super(message);
	}

	public GenerationException() {
		super();
	}

	public GenerationException(Exception e) {
		super(e.getMessage(), e.getCause());
	}

}
