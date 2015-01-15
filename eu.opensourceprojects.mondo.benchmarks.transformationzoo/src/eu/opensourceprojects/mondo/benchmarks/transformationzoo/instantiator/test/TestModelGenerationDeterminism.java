package eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.diff.metamodel.DiffGroup;
import org.eclipse.emf.compare.diff.metamodel.DiffModel;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.exceptions.GenerationException;
import eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.impl.DefaultModelGenerator;
import eu.opensourceprojects.mondo.benchmarks.transformationzoo.instantiator.test.util.ModelUtils;


public class TestModelGenerationDeterminism extends TestModelGeneration {

	
public TestModelGenerationDeterminism(URI uri, int seed) {
		super(uri, seed);
		// TODO Auto-generated constructor stub
	}

private static DefaultModelGenerator modelGen;
	
	private static URI modelURI = URI.createFileURI(".\\data\\inputs\\models\\Class.ecore");
	
	private static long seed = 1250;
	
	private static Path samplesPath;
	
	@BeforeClass
	public  void setupEnvironment() {
		try {
			samplesPath = Files.createTempDirectory("samples");
		
		modelGen = new DefaultModelGenerator(modelURI,seed,samplesPath);
		//TODO  ADD additional parameters for tests
		
		ModelUtils.registerMetamodel(modelURI);
		} catch (Exception e) {
			e.printStackTrace();
		}
		}
	
	@Test
	public void generateModels() {
		try {
			modelGen.runGeneration();
			Assert.assertTrue(compareSnapshots(extractClassName()));
		} catch (IOException | InterruptedException | GenerationException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		} 
	}
	
	private String extractClassName() {
		return modelURI.lastSegment().substring(0,modelURI.lastSegment().lastIndexOf('.'));
	}

	protected boolean compareSnapshots(String className) throws IOException, InterruptedException {
		File directory;
		if (samplesPath != null) {
			directory = samplesPath.toFile();
		} else {
			directory = new File(".\\data\\input\\");
		}

		File[] listDirectories = listDirectories(directory) ;
		String path;
		if (listDirectories.length != 0) {
			for (int i = 0; i < listDirectories.length; i++) {
				path = listDirectories[i].getPath();
				if (path.substring(path.lastIndexOf('\\')+1).equals(className)) {
					return compareSnapshots(listDirectories[i]);
				}
			}
		} 
		
		return false;
	}
	private boolean compareSnapshots(File file) throws IOException, InterruptedException {	
		File[] resultDirectories = listDirectories(file);
		File[] expectedDirectories = listDirectories(new File(".\\data\\expected\\"+extractClassName()));
		for (int i=0; i< resultDirectories.length; i++) {
				if (! compareEmfModels(resultDirectories[i], expectedDirectories[i])) {
					return false;
				}
			
		}
		return resultDirectories.length != 0;
	}

	private boolean compareEmfModels(File file, File file2) throws IOException, InterruptedException {
		File [] resultModels = listFilesWithExtension(file, "xmi");
		File [] expectedModels = listFilesWithExtension(file2, "xmi");
		for (int i= 0 ; i< resultModels.length; i++) {
			DiffModel diff;
			try {
				diff = ModelUtils.compareModels(resultModels[i], expectedModels[i],true, true);
			}  catch (InterruptedException e) {
				throw new AssertionFailedError("Error with the comparator while comparing these file : (expected\\)" +resultModels[i]);
			} catch (IOException e) {
				e.printStackTrace();
				throw new AssertionFailedError("file  :" +resultModels[i] + " cannot be loaded");
			}
			if (0 != ((DiffGroup)diff.getOwnedElements().get(0)).getSubchanges()) {
				return false;
			}
		}
		return resultModels.length != 0;
	}

	protected File[] listDirectories(File aDirectory) {
		File[] directories = null;
		if (aDirectory.exists() && aDirectory.isDirectory()) {
			directories = aDirectory.listFiles(new FileFilter() {
				public boolean accept(File file) {
					return file.isDirectory()
							&& !file.getName().startsWith(".") //$NON-NLS-1$
							&& !file.getName().equals("CVS"); //$NON-NLS-1$
				}
			});
			Arrays.sort(directories);
		}
		return directories;
	}
	
	protected File[] listFilesWithExtension(File file, final String extension) {
		File[] files = null;
		if (file.exists() && file.isDirectory()) {
			 files = file.listFiles(new FileFilter(){

				@Override
				public boolean accept(File pathname) {
					return pathname.getName().endsWith(extension);
				}
				
			});
		}
		return files;
	}
	
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		FileUtils.forceDelete(samplesPath.toFile());
		
		
	}
}
