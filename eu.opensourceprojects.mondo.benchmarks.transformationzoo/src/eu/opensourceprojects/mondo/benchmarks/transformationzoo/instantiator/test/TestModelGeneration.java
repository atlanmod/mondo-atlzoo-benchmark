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
public class testModelGeneration  {
	
	@Parameterized.Parameters 
	public static Collection<Object[]> data() {
 		return Arrays.asList(new Object[][] { 
 												{ URI.createFileURI("./data/inputs/models/Class.ecore"), 1250 }, 
 												{ URI.createFileURI("./data/inputs/models/Ant.ecore"), 1000 }, 
 												{ URI.createFileURI("./data/inputs/models/BibTex.ecore"), 1100}, 
 												{ URI.createFileURI("./data/inputs/models/CPL.ecore"), 1200}, 
 												{ URI.createFileURI("./data/inputs/models/DSLModel.ecore"), 1300} 
 								});
     }

	private static URI modelURI; //= URI.createFileURI("./data/inputs/models/Class.ecore");
	
	private static long seed; // = 1250;
	
	private static DefaultModelGenerator modelGen;
	
	public testModelGeneration (URI uri, int seed) {

		this.modelURI = uri;
		this.seed = seed;
		
	}
	
	@Before
	public void setupEnvironment() {
		modelGen = new DefaultModelGenerator(modelURI,seed);
		}
	
	@Test
	public void generateModels() throws GenerationException {
		modelGen.runGeneration();
		System.out.println("finished testing");
		}
										
	}
