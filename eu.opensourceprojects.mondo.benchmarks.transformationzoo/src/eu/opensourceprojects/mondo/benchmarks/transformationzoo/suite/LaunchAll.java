/*******************************************************************************
 * Copyright (c) 2007, 2012 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - ATL tester
 *******************************************************************************/
package eu.opensourceprojects.mondo.benchmarks.transformationzoo.suite;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import eu.opensourceprojects.mondo.benchmarks.transformationzoo.unit.atlvm.TestNonRegressionEMFVM;
import eu.opensourceprojects.mondo.benchmarks.transformationzoo.unit.atlvm.TestNonRegressionVM;

/**
 * Launches all the JUnit tests for ATL.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public class LaunchAll extends TestCase implements IApplication {

	/**
	 * Launches the test with the given arguments.
	 * 
	 * @param args
	 *            Arguments of the testCase.
	 */
	public static void main(String[] args) {
		TestRunner.run(suite());
	}

	/**
	 * Creates the {@link junit.framework.TestSuite TestSuite} for all the test.
	 * 
	 * @return The testsuite containing all the tests
	 */
	public static Test suite() {
		final TestSuite suite = new TestSuite("ATL test suite"); //$NON-NLS-1$	
		suite.addTestSuite(TestNonRegressionEMFVM.class);
		suite.addTestSuite(TestNonRegressionVM.class);

		return suite;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
		TestRunner.run(suite());
		return Arrays.asList(new String[] {"Please see raw test suite output for details."}); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		// implements org.eclipse.equinox.app.IApplication#stop(). No action.
	}

}
