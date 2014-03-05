package eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.internal;

import java.util.Random;

public class GpwSeeded  {

	public static long seed = -1;
	
	public static String  generate(int pwl) {
		if (seed != -1 ) {
			Gpw.setSeed(seed);
		}
		return Gpw.generate(pwl);
		
	}
	
	
}
