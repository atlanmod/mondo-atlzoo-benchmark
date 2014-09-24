package eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.impl;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.distribution.IntegerDistribution;
import org.apache.commons.math3.distribution.UniformIntegerDistribution;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import com.google.common.collect.ImmutableSet;

import eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.ISpecimenConfiguration;
import eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.exceptions.GenerationException;


public  class DefaultGeneratorConfiguration implements ISpecimenConfiguration {
	
	protected long seed = 0L;
	protected int numberOfProperties;
	protected int numberOfElements;
	protected Random random;
	protected String rootElement;

	
	
	protected int inferNumberOfPropertiesPerClassesNumber(int numberOfElements) {	
		int result = (int) Math.pow(numberOfElements, 1/3);
		while (propertiesEquation(result) < numberOfElements) {
			result ++;
		}
		return result - 1;
	}

	private int propertiesEquation(int result) {
		int res = result + 1;
		res+=  Math.pow(result,3);
		res+= Math.pow(result,2);
		return res;
	}

	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}
	
	public long getSeed() {
		return seed;
	}
	public int getNumberOfProperties() {
		return numberOfProperties;
	}

	public void setNumberOfProperties(int numberOfProperties) {
		this.numberOfProperties = numberOfProperties;
	}

	public int getNumberOfElements() {
		return numberOfElements;
	}

	public void setNumberOfElements(int numberOfElements) {
		this.numberOfElements = numberOfElements;
	}



	public Resource getMmResource() {
		return mmResource;
	}

	public void setMmResource(Resource mmResource) {
		this.mmResource = mmResource;
	}

	protected Resource mmResource;
	
	public DefaultGeneratorConfiguration(Resource mmResource, long seed) {
		super();
		this.mmResource = mmResource;
		if (seed != 0L) {
			this.seed = seed;
		random = new Random(seed);} 
		else {
			random = new Random();
		}
	}
	
	public DefaultGeneratorConfiguration(Resource mmResource) {
		super();
		this.mmResource = mmResource;
		random = new Random();
		
	}
	@Override
	public ImmutableSet<EPackage> ePackages() {
		HashSet<EPackage> ret = new HashSet<EPackage>();
		for (Iterator<?> i = mmResource.getAllContents(); i.hasNext();) {
			EObject eo = (EObject) i.next();
			
			if (eo instanceof EPackage) {
				ret.add((EPackage) eo);
			}
		}
		return ImmutableSet.copyOf(ret);
	}

	@Override
	public ImmutableSet<EClass> possibleRootEClasses() {
		HashSet<EClass> ret = new HashSet<EClass>();
		EList<EClass> allClasses = getAllClasses();
		for (EObject eo : allClasses ) {
			if (eo instanceof EClass
					&& ((EClass) eo).getName().equals(rootElement)) {
				// "System" the root element
				EClass ec = (EClass) eo;
				if (!ec.isAbstract())	
					ret.add(ec);
				else{
					addPossibleRootSubClasses(ret, allClasses, ec);
				}
			}
		}
		return ImmutableSet.copyOf(ret);
	}

	private EList<EClass> getAllClasses() {
		EList<EClass> result = new BasicEList<EClass>();
		
		for (Iterator<?> i = mmResource.getAllContents(); i.hasNext();) {
			EObject eo = (EObject) i.next();
			if (eo instanceof EClass) {
				result.add((EClass) eo);
			}		
		}
			return result;
	}

	private void addPossibleRootSubClasses(HashSet<EClass> ret,
			EList<EClass> subClasses, EClass ec) {
		for (EClass eCls : subClasses) {
			if (! eCls.getESuperTypes().isEmpty()){
				if (eCls.getESuperTypes().contains(ec)) {
					if (! eCls.isAbstract() && isNotComposite(eCls)) {
						if (! ret.contains(eCls))  ret.add(eCls);
					} else {
						addPossibleRootSubClasses(ret, subClasses,eCls);
					}
				}
			}
		}
	}

	private boolean isNotComposite(EClass eCls) {
		for (EReference eReference : eCls.getEAllReferences()) {
			if (eReference.isContainer()) return false;
		}
		return true;
	}

	@Override
	public ImmutableSet<EClass> ignoredEClasses() {
		HashSet<EClass> ret = new HashSet<EClass>();
		for (Iterator<?> i = mmResource.getAllContents(); i.hasNext();) {
			EObject eo = (EObject) i.next();
			if (eo.eClass().getName().equals("EClass")) {
				EClass ec = (EClass) eo;
				if (ec.isAbstract())
					ret.add(ec);
			}
		}
		return ImmutableSet.copyOf(ret);
	}
	
	public int getMaxClasses() {

		return numberOfElements;
	}
	
	
	public int getMaxProperties() {
		return numberOfProperties == 0 ? inferNumberOfPropertiesPerClassesNumber(numberOfElements) : numberOfProperties ;
	}

	
	public void setMaxClasses(int maxClasses) {
		numberOfElements = maxClasses;
		
	}

	
//	public void setMaxProperties(int maxProperties) {
//		numberOfProperties = maxProperties;
//		
//	}
	
	@Override
	public IntegerDistribution getRootDistributionFor(EClass rootEClass) {
		// In case the metamodel has one possible root Metaclass, it is rather better to return a distributon 
		 IntegerDistribution x = new UniformIntegerDistribution(numberOfProperties-1, numberOfProperties);	 
			 x.reseedRandomGenerator(random.nextLong());		 
		 return x;
	}
	/*
	 * @see eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.ISpecimenConfiguration#getResourceSizeDistribution(org.eclipse.emf.ecore.EClass)
	 */
	@Override
	public IntegerDistribution getResourceSizeDistribution(EClass eClass) {
		 String className = eClass.getName();
		 IntegerDistribution x = null;
         if (className.equals("String") || className.equals("Integer") || className.equals("Boolean") ){
        	 x= new UniformIntegerDistribution(numberOfElements, numberOfElements);
         }
         else  { 
        	 x = new UniformIntegerDistribution(numberOfElements-1, numberOfElements);		 
         }
	     
		 x.reseedRandomGenerator(random.nextLong());
		 
		 return x;
	}

	@Override
	public IntegerDistribution getDistributionFor(EReference eReference) {
		IntegerDistribution x = new UniformIntegerDistribution(eReference.getLowerBound(), numberOfProperties);
		     x.reseedRandomGenerator(random.nextLong());
		  
		 return x;	 
	}

	@Override
	public int getWeightFor(EReference eReference, EClass eClass) {
		return 1;
	}

	@Override
	public IntegerDistribution getDistributionFor(EAttribute eAttribute) {
		IntegerDistribution x = new UniformIntegerDistribution(eAttribute.getLowerBound(), numberOfProperties);
			 x.reseedRandomGenerator(random.nextLong());
		  
		 return x;	 
	}
	
	@Override
	public IntegerDistribution getDepthDistributionFor(EClass eClass) {
		return new UniformIntegerDistribution(numberOfProperties-1, numberOfProperties);
	}
}
