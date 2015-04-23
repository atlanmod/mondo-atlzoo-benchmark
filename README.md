# ATL Zoo Benchmark

In this benchmark we select a subset of the transformations in the ATL Transformation Zoo based on their quality level and usage in
real-world applications. 
We specifically include only transformations that may be used in production environments. 
We automatize the sequential execution of this subset and the generation of performance analysis data.

Input models are generated using are generated using a probabilistic instantiator.
It takes as parameter a generation configuration specified by the user.
A generation configuration holds information such as 1) metaclasses that should (not) be involved in the generation, 2) probability distributions to establish how many instances should be generated for each metaclass, and which values should be assigned
to structural features. 
A default generation configuration, using uniform probability distributions for each meta-class and structural feature is provided [here](https://github.com/atlanmod/mondo-atlzoo-benchmark/blob/master/eu.opensourceprojects.mondo.benchmarks.transformationzoo/src/eu/opensourceprojects/mondo/benchmarks/transformationzoo/instantiator/impl/DefaultGeneratorConfiguration.java). 

The transformations can be launched separately using ATLs default launch configuration, or all together usinf the launch configuration sample provided in the **build** folder. A launch configuration file to launch the the instantiator is also provided to guide the users generating their sample models. 



## Prerequisites

* EMF
* JUnit


