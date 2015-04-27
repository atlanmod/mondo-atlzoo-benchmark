# EMF random instantiator

This program produces sets of random instances for EMF (Ecore) metamodels. The *EMF random instantiator* is deterministic, and provides the same set of result instances when a seed is specified.

A default configuration using uniform probability distributions for each meta-class and structural feature is provided. This configuration is ready to use, and can be invoked from command line. Instances can be generated without any knowledge of the metamodel. The default configuration considers that any non-abstract *EClass* without a required containing *EReference* is a valid type for a root *EObject*.

A generation configuration holds information such as (i) metaclasses that should (not) be involved in the generation; and (ii) probability distributions to establish how many instances should be generated for each metaclass, and which values should be assigned to structural features. Details of the configuration can be found [here](https://github.com/atlanmod/mondo-atlzoo-benchmark/blob/master/fr.inria.atlanmod.instantiator/src/fr/inria/atlanmod/instantiator/GenericMetamodelConfig.java). 

The instantiation process is guided by a goal number of *EObjects* (i.e., expected size of the result instance in terms of number of elements). The generation stops once this number of elements is reached and no multiplicity constraint is violated. To guarantee that the generated instances are valid, the tool can (optionally) run a diagnosis on the set of generated models.

## Running the *EMF random instantiator*

The *EMF random instantiator* can be directly executed using the provided (fat jar)[] which contains all the required dependencies:

```
$ java -jar dist/instantiator.jar <program arguments>
```

The only required argument is the file containing the metamodel to instantiate. Nevertheles, several additional parameters can be configured as shown in the *usage information*:

```
$ java -jar dist/instantiator.jar
Missing required option: m
usage: java -jar <this-file.jar> -m <path_to_metamodel.ecore> [-a <path_to_metamodel.ecore>] [-o <path_to_output.dir>] [-n <models>] [-s <size>] [-p <proportion>] [-d <degree>] [-z <size>] [-e <seed>] [-g]
 -m,--metamodel <path_to_metamodel.ecore>              Ecore metamodel
 -a,--additional-metamodel <path_to_metamodel.ecore>   Additional ecore metamodel(s)
                                                       that need to be registered
 -o,--output-dir <path_to_output.dir>                  Output directory (defaults to
                                                       working dir)
 -n,--number-models <models>                           Number of generated models
                                                       (defaults to 1)
 -s,--size <size>                                      Average models' size
                                                      (defaults to 1 000)
 -p,--variation <proportion>                           Variation ([0..1]) in the
                                                       models' size (defaults to 0,1)
 -d,--references-degree <degree>                       Average number of references per
                                                       EObject (defaults to 8).
                                                       Actual sizes may vary +/- 10%.
 -z,--values-size <size>                               Average size for attributes with
                                                       variable length (defaults to 64).
                                                       Actual sizes may vary +/- 10%.
 -e,--seed <seed>                                      Seed number (random by default)
 -g,--diagnose                                         Run diagnosis on the result model
```

## Example

The next example shows the command line arguments to generate a set of 2 random models, of an average size of 100 elements, and using the default configuration with a random seed. The file `logging.default.properties` is used to control the verbosity of the log messages: `INFO` level for the `java.util.logging.ConsoleHandler` and `FINE` level for the `java.util.logging.FileHandler`. When a `FINE` log level is requested, the *EMF random instantiator* will provide **extensive** information about each object and cross-reference creation. 

```
$ >java -Djava.util.logging.config.file=logging.default.properties -jar instantiator-fatjar.jar -m Grafcet.ecore -n 2 -s 100 -g
[Mon Apr 27 16:37:31 CEST 2015] INFO: Creating 2 models
[Mon Apr 27 16:37:31 CEST 2015] INFO: Generator seed is '1,430,145,451,692'
[Mon Apr 27 16:37:31 CEST 2015] INFO: Config parameters: range for models size is [90, 110]
[Mon Apr 27 16:37:31 CEST 2015] INFO: Config parameters: range for properties number is [7, 9]
[Mon Apr 27 16:37:31 CEST 2015] INFO: Config parameters: range for references number is [7, 9]
[Mon Apr 27 16:37:31 CEST 2015] INFO: Config parameters: range for values length is [58, 70]
[Mon Apr 27 16:37:31 CEST 2015] INFO: Start generation of resource ./Grafcet/model0/result0_100.xmi with an average size of 100 elements
[Mon Apr 27 16:37:31 CEST 2015] INFO: Generating cross-references
[Mon Apr 27 16:37:31 CEST 2015] INFO: Requested #EObject=109
[Mon Apr 27 16:37:31 CEST 2015] INFO: Actual #EObject=115
[Mon Apr 27 16:37:31 CEST 2015] INFO: #Grafcet::Step=20
[Mon Apr 27 16:37:31 CEST 2015] INFO: #Grafcet::Element=57
[Mon Apr 27 16:37:31 CEST 2015] INFO: #Grafcet::Connection=51
[Mon Apr 27 16:37:31 CEST 2015] INFO: #Grafcet::NamedElement=115
[Mon Apr 27 16:37:31 CEST 2015] INFO: #Grafcet::LocatedElement=115
[Mon Apr 27 16:37:31 CEST 2015] INFO: #Grafcet::Transition=37
[Mon Apr 27 16:37:31 CEST 2015] INFO: #Grafcet::TransitionToStep=23
[Mon Apr 27 16:37:31 CEST 2015] INFO: #Grafcet::StepToTransition=28
[Mon Apr 27 16:37:31 CEST 2015] INFO: #Grafcet::Grafcet=7
[Mon Apr 27 16:37:31 CEST 2015] INFO: Generation finished for resource './Grafcet/model0/result0_100.xmi'
[Mon Apr 27 16:37:31 CEST 2015] INFO: Start generation of resource ./Grafcet/model1/result1_100.xmi with an average size of 100 elements
[Mon Apr 27 16:37:31 CEST 2015] INFO: Generating cross-references
[Mon Apr 27 16:37:32 CEST 2015] INFO: Requested #EObject=106
[Mon Apr 27 16:37:32 CEST 2015] INFO: Actual #EObject=111
[Mon Apr 27 16:37:32 CEST 2015] INFO: #Grafcet::Step=35
[Mon Apr 27 16:37:32 CEST 2015] INFO: #Grafcet::Element=55
[Mon Apr 27 16:37:32 CEST 2015] INFO: #Grafcet::Connection=49
[Mon Apr 27 16:37:32 CEST 2015] INFO: #Grafcet::NamedElement=111
[Mon Apr 27 16:37:32 CEST 2015] INFO: #Grafcet::LocatedElement=111
[Mon Apr 27 16:37:32 CEST 2015] INFO: #Grafcet::Transition=20
[Mon Apr 27 16:37:32 CEST 2015] INFO: #Grafcet::TransitionToStep=26
[Mon Apr 27 16:37:32 CEST 2015] INFO: #Grafcet::StepToTransition=23
[Mon Apr 27 16:37:32 CEST 2015] INFO: #Grafcet::Grafcet=7
[Mon Apr 27 16:37:32 CEST 2015] INFO: Generation finished for resource './Grafcet/model1/result1_100.xmi'
[Mon Apr 27 16:37:32 CEST 2015] INFO: Saving resource ./Grafcet/model0/result0_100.xmi
[Mon Apr 27 16:37:32 CEST 2015] INFO: Saving resource ./Grafcet/model1/result1_100.xmi
[Mon Apr 27 16:37:32 CEST 2015] INFO: All resources have been saved
[Mon Apr 27 16:37:32 CEST 2015] INFO: Requested validation for resource './Grafcet/model0/result0_100.xmi'
[Mon Apr 27 16:37:32 CEST 2015] INFO: Result of the diagnosis of resurce './Grafcet/model0/result0_100.xmi' is 'OK'
[Mon Apr 27 16:37:32 CEST 2015] INFO: Requested validation for resource './Grafcet/model1/result1_100.xmi'
[Mon Apr 27 16:37:32 CEST 2015] INFO: Result of the diagnosis of resurce './Grafcet/model1/result1_100.xmi' is 'OK'
[Mon Apr 27 16:37:32 CEST 2015] INFO: Validation finished

```

## Dependencies and build Prerequisites

The *EMF random instantiator* uses Ivy to manage the project's dependencies. The dependencies as declared in the `ivy.xml` file are:

```
<dependencies>
	<dependency org="commons-cli" name="commons-cli" rev="1.2"/>
	<dependency org="org.apache.commons" name="commons-math3" rev="3.4.1"/>
	<dependency org="org.apache.commons" name="commons-lang3" rev="3.3.2"/>
	<dependency org="com.google.guava" name="guava" rev="12.0"/>
	<dependency org="jline" name="jline" rev="2.12"/>
	<dependency org="org.eclipse.emf" name="org.eclipse.emf.ecore.xmi" rev="2.10.1"/>
	<dependency org="org.eclipse.emf" name="org.eclipse.emf.ecore" rev="2.10.1"/>
	<dependency org="org.eclipse.emf" name="org.eclipse.emf.common" rev="2.10.1"/>
</dependencies>
```

We recommend using [Eclipse](http://eclipse.org/downloads/) and [Apache IvyIDE](https://ant.apache.org/ivy/ivyde/download.html).

## Credits

This program has been developed by the [AtlanMod Team](http://www.emn.fr/z-info/atlanmod/index.php/Main_Page) (Inria, Mines-Nantes, Lina).

This program reuses some code from the [emf.specimen](https://github.com/Obeo/emf.specimen) generator from [Obeo](http://www.obeo.fr/).