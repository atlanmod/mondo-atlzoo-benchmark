/*******************************************************************************
 * Copyright (c) 2015 agomez
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * agomez - initial API and implementation
 *******************************************************************************/

package fr.inria.instantiator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;

import jline.TerminalFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

/**
 * @author agomez
 *
 */
public class Instantiator {

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
	private static final String SEED 						= "e";
	private static final String SEED_LONG 					= "seed";


	private static class OptionComarator<T extends Option> implements Comparator<T> {
	    private static final String OPTS_ORDER = "maonse";

	    @Override
		public int compare(T o1, T o2) {
	        return OPTS_ORDER.indexOf(o1.getOpt()) - OPTS_ORDER.indexOf(o2.getOpt());
	    }
	}

	public static void main(String[] args) throws GenerationException, IOException {

		Options options = new Options();

		configureOptions(options);

		CommandLineParser parser = new GnuParser();

		try {
			CommandLine commandLine = parser.parse(options, args);

			String metamodel = commandLine.getOptionValue(METAMODEL);
			DefaultModelGenerator modelGen = new DefaultModelGenerator(URI.createFileURI(metamodel));

			if (commandLine.hasOption(ADDITIONAL_METAMODEL)) {
				for (String additionalMetamodel : commandLine.getOptionValues(ADDITIONAL_METAMODEL)) {
					URI additionalMetamodelUri = URI.createFileURI(additionalMetamodel);
					Resource resource = new XMIResourceImpl(additionalMetamodelUri);
					resource.load(Collections.emptyMap());
					registerPackages(resource);
				}
			}

			if (commandLine.hasOption(OUTPUT_DIR)) {
				String outDir = commandLine.getOptionValue(OUTPUT_DIR);
				modelGen.setSamplesPath(Paths.get(outDir));
			} else {
				modelGen.setSamplesPath(Paths.get("."));
			}
			if (commandLine.hasOption(N_MODELS)) {
				int models = ((Number) commandLine.getParsedOptionValue(N_MODELS)).intValue();
				modelGen.setSetSize(new int[] { models });
			} else {
				modelGen.setSetSize(new int[] { 1 });
			}
			if (commandLine.hasOption(SIZE)) {
				Number number = (Number) commandLine.getParsedOptionValue(SIZE);
				int size = (int) Math.min(Integer.MAX_VALUE, number.longValue());
				modelGen.setModelsSize(new int[] { size });
			} else {
				modelGen.setModelsSize(new int[] { 1000 });
			}
			if (commandLine.hasOption(SEED)) {
				int seed = ((Number) commandLine.getParsedOptionValue(SEED)).intValue();
				modelGen.setSeed(seed);
			} else {
				modelGen.setSeed(System.currentTimeMillis());
			}
			modelGen.runGeneration();
		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			HelpFormatter formatter = new HelpFormatter();
			formatter.setOptionComparator(new OptionComarator<Option>());
			try {
				formatter.setWidth(Math.max(TerminalFactory.get().getWidth(), 80));
			} catch (Throwable t) {
				// Nothing to do...
			};
			formatter.printHelp("java -jar <this-file.jar>", options, true);
		}
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
		sizeOption.setDescription("Models' size (defaults to 1000)");
		sizeOption.setType(Number.class);
		sizeOption.setArgs(1);

		Option seedOption = OptionBuilder.create(SEED);
		seedOption.setLongOpt(SEED_LONG);
		seedOption.setArgName("seed");
		seedOption.setDescription("Seed number (random by default)");
		seedOption.setType(Number.class);
		seedOption.setArgs(1);

		options.addOption(metamodelOpt);
		options.addOption(additionalMetamodelOpt);
		options.addOption(outDirOpt);
		options.addOption(nModelsOpt);
		options.addOption(sizeOption);
		options.addOption(seedOption);
	}

	/**
	 * Registers the packages
	 * @param resource
	 */
	public static void registerPackages(Resource resource) {
		EObject eObject = resource.getContents().get(0);
		if (eObject instanceof EPackage) {
			EPackage p = (EPackage) eObject;
			EPackage.Registry.INSTANCE.put(p.getNsURI(), p);
		}
	}
}
