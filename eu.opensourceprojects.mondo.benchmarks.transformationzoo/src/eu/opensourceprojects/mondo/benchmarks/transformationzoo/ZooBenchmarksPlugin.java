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
package eu.opensourceprojects.mondo.benchmarks.transformationzoo;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public class ZooBenchmarksPlugin extends Plugin {

	/** The plug-in ID. */
	public static final String PLUGIN_ID = "org.eclipse.m2m.atl.tests"; //$NON-NLS-1$

	/** The shared instance. */
	private static ZooBenchmarksPlugin plugin;

	/** the main resourceSet. */
	private static ResourceSet resourceSet = new ResourceSetImpl();

	/**
	 * Default constructor for the plugin.
	 */
	public ZooBenchmarksPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 * 
	 * @return the shared instance
	 */
	public static ZooBenchmarksPlugin getDefault() {
		return plugin;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
	}

	/**
	 * Returns the main ResourceSet.
	 * 
	 * @return the main ResourceSet
	 */
	public ResourceSet getResourceSet() {
		return resourceSet;
	}
}
