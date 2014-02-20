This project contains the UML2OWL scenario. This ATL scenario presents an implementation of the OMG's ODM 
specification. This transformation is used to produce an OWL ontology + OWL Individuals from a UML Model + 
UML Instances.

-- Important  --  --
	To use this transformation scenario, you need to install the UML 2.0 plugin v2.0 or above available at 
	www.eclipse.org/uml2 in order to reference the UML Metamodel used by transformations.
--	--	--	--

Transformation files:
	- UML2OWL.atl: The core transformation of the scenario. It is the implementation in ATL of
	  the QVT mapping UML2OWL from ODM specification. It produces an OWL Model from a UML 2.0 Model.
	- OWL/XmlSyntax/OWL2XML.atl: ATL transformation that transforms an OWL Model into
	  an XML models with OWL/XML syntax elements.
	- build.xml: An ANT script that automatically executes the 2 ATL transformations and
	  the XML extractor to produce from a uml model an owl document that contains the ontology.
	  It can be executed by right-clicking on it, then clicking on "Run As->Ant Build". To execute 
	  this transformation on other uml examples, place your uml models in Samples/UMLModels and change 
	  property -- model -- value in ANT script with your uml model name.
	  
Metamodel files:
	- AMMACore/XML.ecore: XML metamodel in EMF XMI 2.0 format.
	  This metamodel is part of standard metamodels used with ATL.
	  It is used when models are tranformed into XML documents or vice versa.
	- OWL/OWL.km3: OWL Metamodel in km3 textual syntax. This Metamodel has been designed
	  by following the ODM specification.
	- OWL/OWL.ecore: OWL Metamodel in Ecore EMF XMI 2.0 format.
	
Sample files:
	- Samples/UMLModels/Museum.uml: This UML model represents a Museum. This example that is a well known in the 
	  RQL (RDF Query language) communty. This Model also contains some Instances. These latters are used to demonstrate 
	  the feasability to create a knowledge base (OWL Individuals) from UML Instances. This Model is conform to the 
	  UML 2.0 Metamodel available at eclipse.org/uml2.
	  	
	- Samples/OWLModels/Museum-OWL.ecore: The Museum Model conforms to the OWL Metamodel. This Model is obtained after 
	  executing the UML2OWL.atl transformation.
	
	- Samples/UMLFiles/Museum.owl: This file is the Museum ontology in OWL/XML format after executing the OWL2XML 
	  transformation and the XML extractor. This file can be used in an ontology editor like Protégé.
	