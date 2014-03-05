/*******************************************************************************
 * Copyright (c) 2012 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import com.google.common.collect.ImmutableSet;

/**
 * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
 * 
 */
public interface ISpecimenConfiguration {

	/**
	 * Returns the set of EPackages to consider as the metamodel of the model to
	 * be generated
	 * 
	 * @return
	 */
	ImmutableSet<EPackage> ePackages();

	/**
	 * Returns the list of EClass to be instantiated as root objects.
	 * 
	 * @return
	 */
	ImmutableSet<EClass> possibleRootEClasses();

	/**
	 * Those Eclass will never be generated.
	 * 
	 * @return
	 */
	ImmutableSet<EClass> ignoredEClasses();

	/**
	 * Returns the distribution to follow to generate root classes.
	 * 
	 * @param rootEClass
	 * @return
	 */
	IntegerDistribution getRootDistributionFor(EClass rootEClass);

	/**
	 * Size of resources (in number of EObjects) will be distributed following
	 * the returned the distribution.
	 * 
	 * Note that the real distribution may be shifted up in some
	 * SpecimenGenerator.
	 * 
	 * @param eClass
	 *            an EClass from the {@link #possibleRootEClasses()}
	 * @return the distribution
	 */
	IntegerDistribution getResourceSizeDistribution(EClass eClass);

	/**
	 * Returns how many element has to be generated to fill an instance of the
	 * given reference.
	 * 
	 * e.g: the EReference EClass.eSuperTypes
	 * 
	 * return a BinomialDistribution(3,0.5) and you will get a model with the
	 * eSuperType reference filled with a mean of 1.5 elements.
	 * 
	 * @param eReference
	 * @return
	 */
	IntegerDistribution getDistributionFor(EReference eReference);

	/**
	 * Returns the weight of the possibles concrete EClass for the given
	 * reference.
	 * 
	 * e.g.: the EReference EClass.eStructuralFeatures
	 * 
	 * returns the wieght: EAttibute=10, EReference=8 and EOperation=2 to have
	 * those ratio in the eStructuralFeatures references.
	 * 
	 * @param eReference
	 * @param eClass
	 * @return
	 */
	int getWeightFor(EReference eReference, EClass eClass);

	/**
	 * Same as for {@link #getDistributionFor(EReference)}.
	 * 
	 * @param eAttribute
	 * @return
	 */
	IntegerDistribution getDistributionFor(EAttribute eAttribute);

	/**
	 * Returns the distribution to follow describing the depth of an instance of
	 * the given ECLass.
	 * 
	 * e.g: the EClass EPackage is a {@link #possibleRootEClasses()}. The
	 * specimen generator will generate a number of instance following
	 * {@link #getRootDistributionFor(EClass)}. The Depth from those objects to
	 * leaves will follow the returned distribution.
	 * 
	 * @param eClass
	 * @return
	 */
	IntegerDistribution getDepthDistributionFor(EClass eClass);
	
	/**
	 * Returns the configuration seed
	 * @return Long 
	 */
	long getSeed();

}