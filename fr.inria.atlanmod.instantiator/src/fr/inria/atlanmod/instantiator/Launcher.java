/*******************************************************************************
 * Copyright (c) 2015 Abel G�mez (AtlanMod) 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Abel G�mez (AtlanMod) - Additional modifications      
 *******************************************************************************/

package fr.inria.atlanmod.instantiator;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.Range;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import jline.TerminalFactory;

/**
 * @author <a href="mailto:abel.gomez-llana@inria.fr">Abel G�mez</a>
 *
 */
public class Launcher {
	
	private final static Logger LOGGER = Logger.getLogger(Launcher.class.getName());

	private static final int DEFAULT_AVERAGE_MODEL_SIZE = 1000;
	private static final float DEFAULT_DEVIATION = 0.1f;
	
	private static final int ERROR = 1;

	private static final String METAMODEL 					= "m";
	private static final String METAMODEL_LONG 				= "metamodel";
	private static final String ADDITIONAL_METAMODEL 		= "a";
	private static final String ADDITIONAL_METAMODEL_LONG 	= "additional-metamodel";
	private static final String OUTPUT_DIR 					= "o";
	private static final String OUTPUT_DIR_LONG				= "output-dir";
	private static final String N_MODELS					= "n";
	private static final String N_MODELS_LONG				= "number-models";
	private static final String SIZE 						= "s";
	private static final String SIZE_LONG					= "size";
	private static final String VARIATION 					= "p";
	private static final String VARIATION_LONG				= "variation";
	private static final String PROP_VARIATION 				= "v";
	private static final String PROP_VARIATION_LONG			= "properties-variation";
	private static final String DEGREE 						= "d";
	private static final String DEGREE_LONG 				= "references-degree";
	private static final String VALUES_SIZE 				= "z";
	private static final String VALUES_SIZE_LONG			= "values-size";
	private static final String SEED 						= "e";
	private static final String SEED_LONG 					= "seed";
	private static final String FORCE 						= "f";
	private static final String FORCE_LONG 					= "force";
	private static final String DIAGNOSE	 				= "g";
	private static final String DIAGNOSE_LONG				= "diagnose";



	private static class OptionComparator<T extends Option> implements Comparator<T> {
	    private static final String OPTS_ORDER = "maonspdzefg";

	    @Override
		public int compare(T o1, T o2) {
	        return OPTS_ORDER.indexOf(o1.getOpt()) - OPTS_ORDER.indexOf(o2.getOpt());
	    }
	}

	public static void main(String[] args) throws GenerationException, IOException {

		{
			// Initialize the global registry
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
					EcorePackage.eNS_PREFIX, new EcoreResourceFactoryImpl());
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(
					Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		}
		
		Options options = new Options();

		configureOptions(options);

		CommandLineParser parser = new GnuParser();

		try {
			CommandLine commandLine = parser.parse(options, args);

			String metamodel = commandLine.getOptionValue(METAMODEL);
			
			Resource metamodelResource = new XMIResourceImpl(URI.createFileURI(metamodel));
			metamodelResource.load(Collections.emptyMap());
			EcoreUtil.resolveAll(metamodelResource);
			{
				BasicDiagnostic diagnosticChain = diagnoseResource(metamodelResource);
				if (isFailed(diagnosticChain)) {
					LOGGER.severe(MessageFormat.format(
							"Found ''{0}'' unrecoverable issues(s) in the resource ''{1}''. Please fix them before attempting a generation or use option -{2}|--{3}.",
							diagnosticChain.getChildren().size(), metamodelResource.getURI(), FORCE, FORCE_LONG));
					for (Diagnostic diagnostic : diagnosticChain.getChildren()) {
						LOGGER.severe(diagnostic.getMessage());
					}
					if (commandLine.hasOption(FORCE)) {
						LOGGER.info(MessageFormat.format(
								"An attempt to run the generation will be done anyway (-{0}|--{1} option selected)",
								FORCE, FORCE_LONG));
					} else {
						System.exit(ERROR);
					}
				} else if (isWarning(diagnosticChain)) {
					LOGGER.warning(MessageFormat.format(
							"Found ''{0}'' issue(s) in the resource ''{1}''. An attempt to run the generation will be done anyway.",
							diagnosticChain.getChildren().size(), metamodelResource.getURI()));
					for (Diagnostic diagnostic : diagnosticChain.getChildren()) {
						LOGGER.fine(diagnostic.getMessage());
					}
				}
			}
			
			registerPackages(metamodelResource);

			int size = Launcher.DEFAULT_AVERAGE_MODEL_SIZE;
			if (commandLine.hasOption(SIZE)) {
				Number number = (Number) commandLine.getParsedOptionValue(SIZE);
				 size = (int) Math.min(Integer.MAX_VALUE, number.longValue());
			}
			
			float variation = Launcher.DEFAULT_DEVIATION;
			if (commandLine.hasOption(VARIATION)) {
				Number number = (Number) commandLine.getParsedOptionValue(VARIATION);
				if (number.floatValue() < 0.0f || number.floatValue() > 1.0f) {
					throw new ParseException(MessageFormat.format("Invalid value for option -{0}: {1}", VARIATION, number.floatValue()));
				}
				variation = number.floatValue();
			}
			
			float propVariation = Launcher.DEFAULT_DEVIATION;
			if (commandLine.hasOption(PROP_VARIATION)) {
				Number number = (Number) commandLine.getParsedOptionValue(PROP_VARIATION);
				if (number.floatValue() < 0.0f || number.floatValue() > 1.0f) {
					throw new ParseException(MessageFormat.format("Invalid value for option -{0}: {1}", PROP_VARIATION, number.floatValue()));
				}
				propVariation = number.floatValue();
			}
			
			
			long seed = System.currentTimeMillis();
			if (commandLine.hasOption(SEED)) {
				seed = ((Number) commandLine.getParsedOptionValue(SEED)).longValue();
			}

			Range<Integer> range = Range.between(
					Math.round(size * (1 - variation)), 
					Math.round(size * (1 + variation)));
			
			GenericMetamodelConfig config = new GenericMetamodelConfig(metamodelResource, range, seed);
			GenericMetamodelGenerator modelGen = new GenericMetamodelGenerator(config);

			if (commandLine.hasOption(ADDITIONAL_METAMODEL)) {
				for (String additionalMetamodel : commandLine.getOptionValues(ADDITIONAL_METAMODEL)) {
					URI additionalMetamodelUri = URI.createFileURI(additionalMetamodel);
					Resource resource = new XMIResourceImpl(additionalMetamodelUri);
					resource.load(Collections.emptyMap());
					
					BasicDiagnostic diagnosticChain = diagnoseResource(resource);
					if (isFailed(diagnosticChain)) {
						LOGGER.severe(MessageFormat.format(
								"Found ''{0}'' unrecoverable issues(s) in the resource ''{1}''. Please fix them before attempting a generation or use option -{2}|--{3}.",
								diagnosticChain.getChildren().size(), resource.getURI(), FORCE, FORCE_LONG));
						for (Diagnostic diagnostic : diagnosticChain.getChildren()) {
							LOGGER.severe(diagnostic.getMessage());
						}
						if (commandLine.hasOption(FORCE)) {
							LOGGER.info(MessageFormat.format(
									"An attempt to run the generation will be done anyway (-{0}|--{1} option selected)",
									FORCE, FORCE_LONG));
						} else {
							System.exit(ERROR);
						}
					} else if (isWarning(diagnosticChain)) {
						LOGGER.warning(MessageFormat.format(
								"Found ''{0}'' issue(s) in the resource ''{1}''. An attempt to run the generation will be done anyway.",
								diagnosticChain.getChildren().size(), resource.getURI()));
						for (Diagnostic diagnostic : diagnosticChain.getChildren()) {
							LOGGER.fine(diagnostic.getMessage());
						}
					}
					
					registerPackages(resource);
				}
			}

			if (commandLine.hasOption(OUTPUT_DIR)) {
				String outDir = commandLine.getOptionValue(OUTPUT_DIR);
				modelGen.setSamplesPath(Paths.get(outDir));
			} else {
				modelGen.setSamplesPath(Paths.get("."));
			}

			int numberOfModels = 1;
			if (commandLine.hasOption(N_MODELS)) {
				numberOfModels = ((Number) commandLine.getParsedOptionValue(N_MODELS)).intValue();
			}

			int valuesSize = GenericMetamodelConfig.DEFAULT_AVERAGE_VALUES_LENGTH;
			if (commandLine.hasOption(VALUES_SIZE)) {
				Number number = (Number) commandLine.getParsedOptionValue(VALUES_SIZE);
				valuesSize = (int) Math.min(Integer.MAX_VALUE, number.longValue());
			}

			int referencesSize = GenericMetamodelConfig.DEFAULT_AVERAGE_REFERENCES_SIZE;
			if (commandLine.hasOption(VALUES_SIZE)) {
				Number number = (Number) commandLine.getParsedOptionValue(DEGREE);
				referencesSize = (int) Math.min(Integer.MAX_VALUE, number.longValue());
			}
			

			config.setValuesRange(
					Math.round(valuesSize * (1 - propVariation)), 
					Math.round(valuesSize * (1 + propVariation)));
			
			config.setReferencesRange(
					Math.round(referencesSize * (1 - propVariation)), 
					Math.round(referencesSize * (1 + propVariation)));
			
			config.setPropertiesRange(
					Math.round(referencesSize * (1 - propVariation)), 
					Math.round(referencesSize * (1 + propVariation)));
			
			ResourceSetImpl resourceSet = new ResourceSetImpl();
			modelGen.runGeneration(resourceSet, numberOfModels, size, variation);
			
			if (commandLine.hasOption(DIAGNOSE)) {
				for (Resource resource : resourceSet.getResources()) {
					LOGGER.info(MessageFormat.format("Requested validation for resource ''{0}''", resource.getURI()));
					BasicDiagnostic diagnosticChain = diagnoseResource(resource);
					if (!isFailed(diagnosticChain)) {
						LOGGER.info(MessageFormat.format("Result of the diagnosis of resurce ''{0}'' is ''OK''",
												resource.getURI()));
					} else {
						LOGGER.severe(MessageFormat.format("Found ''{0}'' error(s) in the resource ''{1}''",
								diagnosticChain.getChildren().size(), resource.getURI()));
						for (Diagnostic diagnostic : diagnosticChain.getChildren()) {
							LOGGER.fine(diagnostic.getMessage());
						}
					}
				}
				LOGGER.info("Validation finished");
			}
			
		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.setOptionComparator(new OptionComparator<Option>());
			try {
				formatter.setWidth(Math.max(TerminalFactory.get().getWidth(), 80));
			} catch (Throwable t) {
				LOGGER.warning("Unable to get console information");
			};
			formatter.printHelp("java -jar <this-file.jar>", options, true);
			System.exit(ERROR);
		} catch (Throwable t) {
			System.err.println("ERROR: " + t.getLocalizedMessage());
			StringWriter stringWriter = new StringWriter();
			t.printStackTrace(new PrintWriter(stringWriter));
			LOGGER.fine(stringWriter.toString());
			System.exit(ERROR);
		} 
	}



	private static BasicDiagnostic diagnoseResource(Resource resource) {
		BasicDiagnostic diagnosticChain = new BasicDiagnostic();
		for (EObject eObject : resource.getContents()) {
			Diagnostician.INSTANCE.validate(eObject, diagnosticChain);
		}
		return diagnosticChain;
	}
	
	private static boolean isFailed(BasicDiagnostic diagnosticChain) {
		return (diagnosticChain.getSeverity() & Diagnostic.ERROR) == Diagnostic.ERROR;
	}

	private static boolean isWarning(BasicDiagnostic diagnosticChain) {
		return (diagnosticChain.getSeverity() & Diagnostic.WARNING) == Diagnostic.WARNING;
	}


	/**
	 * Configures the program options
	 *
	 * @param options
	 */
	private static void configureOptions(Options options) {
		Option metamodelOpt = OptionBuilder.create(METAMODEL);
		metamodelOpt.setLongOpt(METAMODEL_LONG);
		metamodelOpt.setArgName("path_to_metamodel.ecore");
		metamodelOpt.setDescription("Ecore metamodel");
		metamodelOpt.setArgs(1);
		metamodelOpt.setRequired(true);

		Option additionalMetamodelOpt = OptionBuilder.create(ADDITIONAL_METAMODEL);
		additionalMetamodelOpt.setLongOpt(ADDITIONAL_METAMODEL_LONG);
		additionalMetamodelOpt.setArgName("path_to_metamodel.ecore");
		additionalMetamodelOpt.setDescription("Additional ecore metamodel(s) that need to be registered");
		additionalMetamodelOpt.setArgs(Option.UNLIMITED_VALUES);

		Option outDirOpt = OptionBuilder.create(OUTPUT_DIR);
		outDirOpt.setLongOpt(OUTPUT_DIR_LONG);
		outDirOpt.setArgName("path_to_output.dir");
		outDirOpt.setDescription("Output directory (defaults to working dir)");
		outDirOpt.setArgs(1);

		Option nModelsOpt = OptionBuilder.create(N_MODELS);
		nModelsOpt.setLongOpt(N_MODELS_LONG);
		nModelsOpt.setArgName("models");
		nModelsOpt.setDescription("Number of generated models (defaults to 1)");
		nModelsOpt.setType(Number.class);
		nModelsOpt.setArgs(1);

		Option sizeOption = OptionBuilder.create(SIZE);
		sizeOption.setLongOpt(SIZE_LONG);
		sizeOption.setArgName("size");
		sizeOption.setDescription(MessageFormat.format("Average models'' size (defaults to {0})", Launcher.DEFAULT_AVERAGE_MODEL_SIZE));
		sizeOption.setType(Number.class);
		sizeOption.setArgs(1);

		Option variationOption = OptionBuilder.create(VARIATION);
		variationOption.setLongOpt(VARIATION_LONG);
		variationOption.setArgName("proportion");
		variationOption.setDescription(
				MessageFormat.format("Variation ([0..1]) in the models'' size (defaults to {0})",
				Launcher.DEFAULT_DEVIATION));
		variationOption.setType(Number.class);
		variationOption.setArgs(1);

		Option propVariationOption = OptionBuilder.create(PROP_VARIATION);
		propVariationOption.setLongOpt(PROP_VARIATION_LONG);
		propVariationOption.setArgName("properties deviation");
		propVariationOption.setDescription(
				MessageFormat.format("Variation ([0..1]) in the properties'' size (defaults to {0})",
				Launcher.DEFAULT_DEVIATION));
		propVariationOption.setType(Number.class);
		propVariationOption.setArgs(1);
		
		Option seedOption = OptionBuilder.create(SEED);
		seedOption.setLongOpt(SEED_LONG);
		seedOption.setArgName("seed");
		seedOption.setDescription("Seed number (random by default)");
		seedOption.setType(Number.class);
		seedOption.setArgs(1);

		Option valuesSizeOption = OptionBuilder.create(VALUES_SIZE);
		valuesSizeOption.setLongOpt(VALUES_SIZE_LONG);
		valuesSizeOption.setArgName("size");
		valuesSizeOption.setDescription(
				MessageFormat.format("Average size for attributes with variable length (defaults to {0}). Actual sizes may vary +/- {1}%.", 
				GenericMetamodelConfig.DEFAULT_AVERAGE_VALUES_LENGTH, GenericMetamodelConfig.DEFAULT_VALUES_DEVIATION * 100));
		valuesSizeOption.setType(Number.class);
		valuesSizeOption.setArgs(1);

		Option degreeOption = OptionBuilder.create(DEGREE);
		degreeOption.setLongOpt(DEGREE_LONG);
		degreeOption.setArgName("degree");
		degreeOption.setDescription(
				MessageFormat.format("Average number of references per EObject (defaults to {0}). Actual sizes may vary +/- {1}%.",
						GenericMetamodelConfig.DEFAULT_AVERAGE_REFERENCES_SIZE, GenericMetamodelConfig.DEFAULT_REFERENCES_DEVIATION * 100));
		degreeOption.setType(Number.class);
		degreeOption.setArgs(1);
		
		Option forceOption = OptionBuilder.create(FORCE);
		forceOption.setLongOpt(FORCE_LONG);
		forceOption.setDescription("Force the generation, even if input metamodels contain errors");
		
		Option diagnoseOption = OptionBuilder.create(DIAGNOSE);
		diagnoseOption.setLongOpt(DIAGNOSE_LONG);
		diagnoseOption.setDescription("Run diagnosis on the result model");
		
		
		options.addOption(metamodelOpt);
		options.addOption(additionalMetamodelOpt);
		options.addOption(outDirOpt);
		options.addOption(nModelsOpt);
		options.addOption(sizeOption);
		options.addOption(variationOption);
		options.addOption(propVariationOption);
		options.addOption(valuesSizeOption);
		options.addOption(degreeOption);
		options.addOption(seedOption);
		options.addOption(forceOption);
		options.addOption(diagnoseOption);
	}

	/**
	 * Registers the packages
	 * @param resource
	 */
	private static void registerPackages(Resource resource) {
		EObject eObject = resource.getContents().get(0);
		if (eObject instanceof EPackage) {
			EPackage p = (EPackage) eObject;
			EPackage.Registry.INSTANCE.put(p.getNsURI(), p);
		}
	}
}
