package eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.test;


import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.exceptions.GenerationException;
import eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.impl.DefaultModelGenerator;

public class testModelGeneration  {

	private static DefaultModelGenerator modelGen;
	
	private static URI modelURI = URI.createFileURI("./data/inputs/models/Class.ecore");
	
	private static long seed = 1250;
	@BeforeClass
	public static void setupEnvironment() {
		modelGen = new DefaultModelGenerator(modelURI,seed);
		//TODO  ADD additional parameters for tests
		}
	
	@Test
	public void generateModels() throws GenerationException {
		modelGen.runGeneration();
	}
	
	
}
