package fr.inria.instantiator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import com.google.common.collect.Iterators;

import fr.obeo.emf.specimen.SpecimenGenerator;

public class DefaultModelGenerator {

	public final String DEFAULT_MODEL_EXTENSION = "xmi";

	public final String META_MODEL_EXTENSION = "ecore";

	static String modelExtension;

	protected int[] modelsSize = { 1000 }; // this size includes model objects
											// and properties

	protected int[] setSize = { 1 }; // the number of generated models per
										// model' size
	protected URI MMURI;

	protected Path samplesPath;

	protected long seed = 0L;

	protected String rootElement;//

	public int[] getModelsSize() {
		return modelsSize;
	}

	public void setModelsSize(int[] modelsSize) {
		this.modelsSize = modelsSize;
	}

	public int[] getSetSize() {
		return setSize;
	}

	public void setSetSize(int[] setSize) {
		this.setSize = setSize;
	}

	public Path getSamplesPath() {
		return samplesPath;
	}

	public void setSamplesPath(Path samplesPath) {
		this.samplesPath = samplesPath;
	}

	public long getSeed() {
		return seed;
	}

	public void setSeed(long seed) {
		this.seed = seed;
	}

	public DefaultModelGenerator(URI mMURI) {
		super();
		MMURI = mMURI;

	}

	public DefaultModelGenerator(URI mMURI, long seed) {
		super();
		MMURI = mMURI;
		this.seed = seed;
	}

	public DefaultModelGenerator(URI mMURI, long seed, Path samplesPath) {
		super();
		MMURI = mMURI;
		this.seed = seed;
		this.samplesPath = samplesPath;

	}

	public DefaultModelGenerator(URI mMURI, Path samplesPath) {
		super();
		MMURI = mMURI;
		this.samplesPath = samplesPath;
	}

	public String getRootElement() throws GenerationException {
		String result = "";
		if (rootElement != null) {
			result = rootElement;
		} else if (MMURI == null) {
			throw new GenerationException("Cannot find Root Element: Metamodel URI is null, please set it up before proceeding to compute the root elements");
		} else {
			result = computeRootElement();
		}
		return result;
	}

	private String computeRootElement() throws GenerationException {
		String result = "";
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put(META_MODEL_EXTENSION, new EcoreResourceFactoryImpl());
		m.put(getModelExtension(), new XMIResourceFactoryImpl());
		Resource mmResource = null;
		ResourceSetImpl resourceSet = new ResourceSetImpl();

		boolean isRoot = false;
		try {
			if (MMURI == null) {
				throw new GenerationException(" Metamodel URI is null, please set it up before proceeding to generate");
			}
			mmResource = resourceSet.createResource(MMURI);
			mmResource.load(null);

			Iterator<EObject> iter = mmResource.getAllContents();
			EObject eObject = null;
			while (iter.hasNext()) {
				eObject = iter.next();
				if (!(eObject instanceof EClass)) {
					continue;
				}
				if (((EClass) eObject).getEAllSuperTypes().isEmpty() && ((EClass) eObject).getEAllContainments().isEmpty()) {
					isRoot = true;
					break;
				}
				if (((EClass) eObject).getEAllContainments().isEmpty()) {
					continue;
				}

				for (EReference ref : ((EClass) eObject).getEReferences()) {
					if (ref.isContainer()) {
						continue;
					}
				}
				isRoot = true;
				break;
			}

			if (isRoot && eObject != null) {
				result = ((EClass) eObject).getName();
			} else {
				throw new GenerationException("Cannot find a root element");
			}
		} catch (IOException e) {
			throw new GenerationException(e);
		} finally {
			mmResource.unload();
			mmResource = null;
		}
		return result;
	}

	public URI getMMURI() {
		return MMURI;
	}

	public void setMMURI(URI mMURI) {
		MMURI = mMURI;
	}

	public void setRootElement(String rootElement) {
		this.rootElement = rootElement;
	}

	public String getModelExtension() {
		return modelExtension == null ? DEFAULT_MODEL_EXTENSION : modelExtension;
	}

	public void setModelExtension(String newModelExtension) {
		modelExtension = newModelExtension;
	}

	protected int inferNumberOfProperties(int numberOfElements) {
		int result = (int) Math.pow(numberOfElements, 1 / 3);
		while (propertiesEquation(result) < numberOfElements) {
			result++;
		}
		return result - 1;
	}

	private int propertiesEquation(int result) {
		int res = result + 1;
		res += Math.pow(result, 3);
		res += Math.pow(result, 2);
		return res;
	}

	public void runGeneration() throws GenerationException {
		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put(META_MODEL_EXTENSION, new EcoreResourceFactoryImpl());
		m.put(getModelExtension(), new XMIResourceFactoryImpl());
		Resource mmResource = null;
		ResourceSetImpl resourceSet = new ResourceSetImpl();
		SpecimenGenerator generator = null;
		DefaultGeneratorConfiguration genModelConf;

		try {
			if (MMURI == null) {
				throw new GenerationException(" Metamodel URI is null, please set it up before proceeding to generate");
			}

			mmResource = resourceSet.createResource(MMURI);
			mmResource.load(null);
			genModelConf = new DefaultGeneratorConfiguration(mmResource, seed);
			genModelConf.setRootElement(this.getRootElement());
			generator = new SpecimenGenerator(genModelConf);
			int loop = modelsSize.length > setSize.length ? setSize.length : modelsSize.length;
			for (int i = 0; i < loop; i++) {
				int numberOfElements = modelsSize[i];
				for (int j = 0; j < setSize[i]; j++) {
					genModelConf.setNumberOfElements(numberOfElements);
					genModelConf.setNumberOfProperties(inferNumberOfProperties(numberOfElements));// base
																									// *
																									// (scaleBase)^tour
					List<EObject> generatedObjects = generator.generate(resourceSet);
					saveModel(numberOfElements, j, generatedObjects);
				}
			}
		} catch (GenerationException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			GenerationException g = new GenerationException(e);
			g.printStackTrace();
		}
	}

	private void saveModel(int maxElements, int index, List<EObject> generatedObjects) throws GenerationException {
		Resource resource = new ResourceSetImpl().createResource(formatURI(getMetaModelName(), maxElements, index));
		System.gc();
		for (EObject eo : generatedObjects) {
			resource.getContents().add(eo);
		}
		try {
			System.out.println(Iterators.size(resource.getAllContents()));
			resource.save(null);
		} catch (IOException e) {
			throw new GenerationException(e);
		}
		resource = null;
		System.gc();
		Runtime.getRuntime().gc();
		System.gc();
	}

	private String getMetaModelName() {
		return MMURI == null ? "test" : MMURI.lastSegment().substring(0, MMURI.lastSegment().indexOf("."));
	}

	private URI formatURI(String metaModelName, long maxElement, int index) {
		return URI.createFileURI(
				(samplesPath != null ? samplesPath.toString() : "data" + File.separator + "samples" )
				+ File.separator
				+ metaModelName
				+ File.separator
				+ "model"
				+ maxElement
				+ File.separator
				+ "result"
				+ maxElement
				+ "_"
				+ index
				+ "."
				+ getModelExtension());
	}

}
