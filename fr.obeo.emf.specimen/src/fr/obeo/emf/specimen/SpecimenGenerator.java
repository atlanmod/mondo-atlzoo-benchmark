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
package fr.obeo.emf.specimen;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.common.base.Optional;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.ListMultimap;

import fr.obeo.emf.specimen.internal.EPackagesData;
import fr.obeo.emf.specimen.internal.Gpw;

import static com.google.common.collect.Lists.newArrayList;

import static com.google.common.collect.Maps.newHashMap;

import static com.google.common.collect.Iterables.get;

import static com.google.common.primitives.Primitives.isWrapperType;
import static com.google.common.primitives.Primitives.unwrap;

/**
 * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
 *
 */
public class SpecimenGenerator {

	private final Random generator;
	private final ISpecimenConfiguration c;
	private final EPackagesData ePackagesData;

	/* inner Variable state */
	private int currentDepth;
	private int currentMaxDepth;
	
	public SpecimenGenerator(ISpecimenConfiguration configuration) {
		c = configuration;
		ePackagesData = new EPackagesData(c.ePackages(), c.ignoredEClasses());
		generator = new Random();
	}
	
	public List<EObject> generate(ResourceSet resourceSet) {
		List<EObject> ret = newArrayList();
		ListMultimap<EClass, EObject> indexByKind = ArrayListMultimap.create();

		currentDepth = 0;
		currentMaxDepth = 0;
		
		for (EClass eClass : c.possibleRootEClasses()) {
			currentMaxDepth = c.getDepthDistributionFor(eClass).sample();
			int nbInstance = c.getRootDistributionFor(eClass).sample();
			for(int i = 0; i < nbInstance; i++) {
				System.out.println("Generating root " + eClass.getName() + " " + i + "/" + nbInstance);
				Optional<EObject> generateEObject = generateEObject(eClass, indexByKind);
				if (generateEObject.isPresent()) {
					ret.add(generateEObject.get());
				}
			}
		}
		
		System.out.println("Generating XRef");
		
		for (EObject eObjectRoot : ret) {
			TreeIterator<EObject> eAllContents = eObjectRoot.eAllContents();
			while (eAllContents.hasNext()) {
				EObject eObject = eAllContents.next();
				generateCrossReferences(eObject, indexByKind);
			}
		}
		
		Map<EClass, Integer> resourcesSize = newHashMap();
		for (EClass eClass : c.possibleRootEClasses()) {
			setNextResourceSizeForType(resourcesSize, eClass);
		}
		
		System.out.println("Generating resources");
		
		for (EObject eObjectRoot : ret) {
			TreeIterator<EObject> eAllContents = eObjectRoot.eAllContents();
			createResource(resourceSet, resourcesSize, eAllContents, eObjectRoot, ret);
			while (eAllContents.hasNext()) {
				EObject eObject = eAllContents.next();
				createResource(resourceSet, resourcesSize, eAllContents, eObject, ret);
			}
		}
		
		
		System.out.println("#EObject=" + ImmutableSet.copyOf(indexByKind.values()).size());
		System.out.println("#Resource=" + resourceSet.getResources().size());
		
		for (Map.Entry<EClass, Collection<EObject>> entry : indexByKind.asMap().entrySet()) {
			EClass eClass = entry.getKey();
			System.out.println("#"+eClass.getEPackage().getNsURI()+"::"+entry.getKey().getName()+"=" + entry.getValue().size());
		}
		
		return ret;
	}

	private void createResource(ResourceSet resourceSet, Map<EClass, Integer> resourcesSize,
			TreeIterator<EObject> eAllContents, EObject eObject, List<EObject> ret) {
		TreeIterator<EObject> properContents = EcoreUtil.getAllProperContents(eObject, true);
		int allProperContentsSize = Iterators.size(properContents);
		EClass eClass = eObject.eClass();
		Integer integer = resourcesSize.get(eClass);
		if (integer != null && allProperContentsSize <= integer.intValue()) {
			createResource(resourceSet, eObject);
			setNextResourceSizeForType(resourcesSize, eClass);
			eAllContents.prune();
		} else if (eObject.eContainer() == null) {
			List<EObject> precedingSibling = ret.subList(0, ret.indexOf(eObject));
			TreeIterator<Object> allPrecedingSiblingContents = EcoreUtil.getAllProperContents(precedingSibling, true);
			int allPrecedingSiblingContentsSize = Iterators.size(allPrecedingSiblingContents);
			if (integer != null && allPrecedingSiblingContentsSize >= integer.intValue()) {
				createResource(resourceSet, eObject);
				setNextResourceSizeForType(resourcesSize, eClass);
				eAllContents.prune();
			}
		}
	}

	private void setNextResourceSizeForType(Map<EClass, Integer> resourcesSize, EClass eClass) {
		IntegerDistribution sizeDistribution = c.getResourceSizeDistribution(eClass);
		int desiredSize = sizeDistribution.sample();
		resourcesSize.put(eClass, desiredSize);
	}

	/**
	 * @param eObject
	 * @param indexByKind 
	 */
	private void generateCrossReferences(EObject eObject, ListMultimap<EClass,EObject> indexByKind) {
		Iterable<EReference> eAllNonContainment = ePackagesData.eAllNonContainment(eObject.eClass());
		for (EReference eReference : eAllNonContainment) {
			EClass eReferenceType = eReference.getEReferenceType();
			IntegerDistribution distribution = c.getDistributionFor(eReference);
			
			if (eReference.isMany()) {
				List<Object> values = (List<Object>) eObject.eGet(eReference);
				int sample = distribution.sample();
				for (int i = 0; i < sample; i++) {
					List<EObject> possibleValues = indexByKind.get(eReferenceType);
					if (!possibleValues.isEmpty()) {
						final EObject nextEObject = possibleValues.get(generator.nextInt(possibleValues.size()));
						values.add(nextEObject);
					}
				}
			} else {
				if (booleanInDistribution(distribution)) {
					List<EObject> possibleValues = indexByKind.get(eReferenceType);
					if (!possibleValues.isEmpty()) {
						final EObject nextEObject = possibleValues.get(generator.nextInt(possibleValues.size()));
						eObject.eSet(eReference, nextEObject);
					}
				}
			}
		}
	}

	private Optional<EObject> generateEObject(EClass eClass, ListMultimap<EClass,EObject> indexByKind) {
		final EObject eObject;
		if (currentDepth <= currentMaxDepth) {
			eObject = createEObject(eClass, indexByKind);
			generateEAttributes(eObject, eClass);
			generateEContainmentReferences(eObject, eClass, indexByKind);
		} else {
			eObject = null;
		}
		return Optional.fromNullable(eObject);
	}

	private EObject createEObject(EClass eClass, ListMultimap<EClass,EObject> indexByKind) {
		EObject eObject = eClass.getEPackage().getEFactoryInstance().create(eClass);
		
		indexByKind.put(eClass, eObject);
		for (EClass eSuperType : eClass.getEAllSuperTypes()) {
			indexByKind.put(eSuperType, eObject);
		}
		
		return eObject;
	}
	
	/**
	 * @param eObject
	 * @return 
	 */
	private Resource createResource(ResourceSet resourceSet, EObject root) {
		Resource resource = resourceSet.createResource(URI.createURI(Gpw.generate(16)+".xmi"));
		resource.getContents().add(root);
		return resource;
	}

	/**
	 * @param eObject
	 * @param eClass
	 * @param indexByKind 
	 */
	private void generateEContainmentReferences(EObject eObject, EClass eClass, ListMultimap<EClass,EObject> indexByKind) {
		for (EReference eReference : ePackagesData.eAllContainment(eClass)) {
			generateEContainmentReference(eObject, eReference, indexByKind);
		}
		
	}

	/**
	 * @param eObject
	 * @param eReference
	 * @param indexByKind 
	 */
	private void generateEContainmentReference(EObject eObject, EReference eReference, ListMultimap<EClass,EObject> indexByKind) {
		currentDepth++;
		
		ImmutableList<EClass> eAllConcreteSubTypeOrSelf = ePackagesData.eAllConcreteSubTypeOrSelf(eReference);
		ImmutableMultiset<EClass> eAllConcreteSubTypesOrSelf = getEReferenceTypesWithWeight(eReference, eAllConcreteSubTypeOrSelf);
		
		if (!eAllConcreteSubTypesOrSelf.isEmpty()) {
			if (eReference.isMany()) {
				generateManyContainmentReference(eObject, eReference, indexByKind, eAllConcreteSubTypesOrSelf);
			} else {
				generateSingleContainmentReference(eObject, eReference, indexByKind, eAllConcreteSubTypesOrSelf);
			}
		}

		currentDepth--;
	}

	private void generateSingleContainmentReference(EObject eObject, EReference eReference, ListMultimap<EClass, EObject> indexByKind, ImmutableMultiset<EClass> eAllConcreteSubTypesOrSelf) {
		IntegerDistribution distribution = c.getDistributionFor(eReference);
		if (booleanInDistribution(distribution)) {
			int idx = generator.nextInt(eAllConcreteSubTypesOrSelf.size());
			final Optional<EObject> nextEObject = generateEObject(get(eAllConcreteSubTypesOrSelf, idx), indexByKind);
			if (nextEObject.isPresent()) {
				eObject.eSet(eReference, nextEObject.get());
			}
		}
	}

	private void generateManyContainmentReference(EObject eObject, EReference eReference, ListMultimap<EClass, EObject> indexByKind, ImmutableMultiset<EClass> eAllConcreteSubTypesOrSelf) {
		IntegerDistribution distribution = c.getDistributionFor(eReference);
		List<EObject> values = (List<EObject>) eObject.eGet(eReference);
		int sample = distribution.sample();
		for (int i = 0; i < sample; i++) {
			int idx = generator.nextInt(eAllConcreteSubTypesOrSelf.size());
			final Optional<EObject> nextEObject = generateEObject(get(eAllConcreteSubTypesOrSelf, idx), indexByKind);
			if (nextEObject.isPresent()) {
				values.add(nextEObject.get());
			}
		}
	}

	private ImmutableMultiset<EClass> getEReferenceTypesWithWeight(EReference eReference, ImmutableList<EClass> eAllSubTypesOrSelf) {
		ImmutableMultiset.Builder<EClass> eAllSubTypesOrSelfWithWeights = ImmutableMultiset.builder();
		for (EClass eClass : eAllSubTypesOrSelf) {
			eAllSubTypesOrSelfWithWeights.addCopies(eClass, c.getWeightFor(eReference, eClass));
		}
		return eAllSubTypesOrSelfWithWeights.build();
	}

	
	/**
	 * @param eObject
	 * @param eClass
	 */
	private void generateEAttributes(EObject eObject, EClass eClass) {
		for (EAttribute eAttribute : ePackagesData.eAllAttributes(eClass)) {
			generateAttributes(eObject, eAttribute);
		}
	}

	private void generateAttributes(EObject eObject, EAttribute eAttribute) {
		IntegerDistribution distribution = c.getDistributionFor(eAttribute);
		EDataType eAttributeType = eAttribute.getEAttributeType();
		Class<?> instanceClass = eAttributeType.getInstanceClass();
		if (eAttribute.isMany()) {
			generateManyAttribute(eObject, eAttribute, distribution, instanceClass);
		} else {
			generateSingleAttribute(eObject, eAttribute, distribution, instanceClass);
		}
	}

	private void generateSingleAttribute(EObject eObject, EAttribute eAttribute, IntegerDistribution distribution, Class<?> instanceClass) {
		if (booleanInDistribution(distribution)) {
			final Object value = nextValue(instanceClass);
			eObject.eSet(eAttribute, value);
		}
	}

	private void generateManyAttribute(EObject eObject, EAttribute eAttribute, IntegerDistribution distribution, Class<?> instanceClass) {
		List<Object> values = (List<Object>) eObject.eGet(eAttribute);
		for (int i = distribution.getSupportLowerBound(); i < distribution.sample(); i++) {
			final Object value = nextValue(instanceClass);
			values.add(value);
		}
	}

	private Object nextValue(Class<?> instanceClass) {
		final Object value;
		if (instanceClass.isPrimitive() || isWrapperType(instanceClass)) {
			value = nextPrimitive(unwrap(instanceClass));
		} else {
			value = nextObject(instanceClass);
		}
		return value;
	}

	/**
	 * @param instanceClass
	 */
	private Object nextObject(Class<?> instanceClass) {
		if (instanceClass == String.class) {
			return Gpw.generate(generator.nextInt(24) + 1);
		} else {
			log("Do not know how to randomly generate " + instanceClass.getName() + " object");
		}
		return null;
	}

	/**
	 * @param string
	 */
	private void log(String string) {
		System.out.println(string);
	}

	/**
	 * @param eObject
	 * @param eAttribute
	 * @param instanceClass
	 */
	private Object nextPrimitive(Class<?> instanceClass) {
		if (instanceClass == boolean.class) {
			return generator.nextBoolean();
		} else if (instanceClass == byte.class) {
			byte[] buff = new byte[1];
			generator.nextBytes(buff);
			return buff[0];
		} else if (instanceClass == char.class) {
			char nextChar = (char)generator.nextInt();
			return nextChar;
		} else if (instanceClass == double.class) {
			return generator.nextDouble();
		} else if (instanceClass == float.class) {
			return generator.nextFloat();
		} else if (instanceClass == int.class) {
			return generator.nextInt();
		} else if (instanceClass == long.class) {
			return generator.nextLong();
		} else if (instanceClass == short.class) {
			short nextShort = (short)generator.nextInt();
			return nextShort;
		} else {
			throw new IllegalArgumentException();
		}
	}

	private boolean booleanInDistribution(IntegerDistribution distribution) {
		int sample = distribution.sample();
		return sample < distribution.getNumericalMean(); 
	}
}
