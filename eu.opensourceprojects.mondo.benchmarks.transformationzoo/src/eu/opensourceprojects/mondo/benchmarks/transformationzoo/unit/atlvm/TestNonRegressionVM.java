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
package eu.opensourceprojects.mondo.benchmarks.transformationzoo.unit.atlvm;

import java.util.Properties;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.m2m.atl.core.service.CoreService;
import org.eclipse.m2m.atl.core.ui.vm.RegularVMLauncher;
import org.eclipse.m2m.atl.core.ui.vm.asm.ASMExtractor;
import org.eclipse.m2m.atl.core.ui.vm.asm.ASMFactory;
import org.eclipse.m2m.atl.core.ui.vm.asm.ASMInjector;
import org.eclipse.m2m.atl.drivers.emf4atl.AtlEMFModelHandler;
import org.eclipse.m2m.atl.engine.vm.AtlModelHandler;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.eclipse.uml2.uml.resource.UMLResource;

import eu.opensourceprojects.mondo.benchmarks.transformationzoo.unit.TestNonRegressionTransfo;

/**
 * Specifies TestNonRegressionTransfo for the vm.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public class TestNonRegressionVM extends TestNonRegressionTransfo {

	/**
	 * {@inheritDoc}
	 *
	 * @see eu.opensourceprojects.mondo.benchmarks.transformationzoo.unit.TestNonRegressionTransfo#getVMName()
	 */
	@Override
	protected String getVMName() {
		return "Regular VM"; //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see eu.opensourceprojects.mondo.benchmarks.transformationzoo.unit.TestNonRegressionTransfo#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		Properties properties = new Properties();
		properties.load(TestNonRegressionVM.class.getResourceAsStream("TestNonRegressionVM.properties")); //$NON-NLS-1$
		setProperties(properties);
		
		AtlModelHandler.registerDefaultHandler("EMF", new AtlEMFModelHandler()); //$NON-NLS-1$
		CoreService.registerLauncher("Regular VM", RegularVMLauncher.class); //$NON-NLS-1$
		CoreService.registerFactory("ASM", ASMFactory.class); //$NON-NLS-1$
		CoreService.registerExtractor("ASM", ASMExtractor.class); //$NON-NLS-1$
		CoreService.registerInjector("ASM", ASMInjector.class); //$NON-NLS-1$

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(UMLResource.FILE_EXTENSION,
				new UMLResourceFactoryImpl());
		EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put(UMLResource.UML_METAMODEL_URI, UMLPackage.eINSTANCE);
		EPackage.Registry.INSTANCE.put("http://www.eclipse.org/uml2/2.0.0/UML", UMLPackage.eINSTANCE); //$NON-NLS-1$
		EPackage.Registry.INSTANCE.put("http://www.eclipse.org/uml2/2.1.0/UML", UMLPackage.eINSTANCE); //$NON-NLS-1$
	}

}
