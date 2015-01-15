package eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.test;


import java.util.Arrays;
import java.util.Collection;
import org.eclipse.emf.common.util.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.exceptions.GenerationException;
import eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.impl.DefaultModelGenerator;

@RunWith(Parameterized.class)
public class TestModelGeneration  {
	
	@Parameterized.Parameters 
	public static Collection<Object[]> data() {
 		return Arrays.asList(new Object[][] { 
 												//{ URI.createFileURI(".\\data\\instantiator\\models\\Class.ecore"), 1250 }, 
 												{ URI.createFileURI(".\\data\\instantiator\\models\\Ant.ecore"), 1000 }, 
 												{ URI.createFileURI(".\\data\\instantiator\\models\\BibTex.ecore"), 1100}, 
 												{ URI.createFileURI(".\\data\\instantiator\\models\\CPL.ecore"), 1200}, 
 												{ URI.createFileURI(".\\data\\instantiator\\models\\DSLModel.ecore"), 1400}, 
 												{ URI.createFileURI(".\\data\\instantiator\\models\\Grafcet.ecore"), 1500 }, 
 												{ URI.createFileURI(".\\data\\instantiator\\models\\Make.ecore"), 1600}, 
 												{ URI.createFileURI(".\\data\\instantiator\\models\\OCL.ecore"), 1700}, 
 												{ URI.createFileURI(".\\data\\instantiator\\models\\IEEE1471ConceptualModel.ecore"), 1800} 
 								});
     }

	private static URI modelURI; 
	
	private static long seed; 
	
	private static DefaultModelGenerator modelGen;
	
	public TestModelGeneration (URI uri, int seed) {
		TestModelGeneration.modelURI = uri;
		TestModelGeneration.seed = seed;	
	}
	
	@Before
	public void setupEnvironment() {
		// the default generator provided with the project
		modelGen = new DefaultModelGenerator(modelURI,seed);
		}
	
	@Test
	public void generateModels() throws GenerationException {
		modelGen.runGeneration();
		System.out.println("finished testing");
		}
										
	}
