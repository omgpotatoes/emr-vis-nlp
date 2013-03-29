emr-vis-nlp
===========

This project is an effort to incorporate NLP and visualization
techniques into a single system for viewing, annotating, and revising
electronic health records (EHRs). The system is designed to support the
needs of three primary use-cases: retrospective EHR analysis for quality
assessment or cohort inclusion, NLP annotation, and NLP scheme revision.
This code is made available under the New BSD License.

Requirements
------------

-   JDK 1.7 (or newer)
-   Apache Ant 1.8.3
-   Apache Ivy 2.3.0

Running the System
------------------

First, ensure that your system meets the requirements listed above. If
you already have Ant installed, Ivy can be installed by simply unpacking
the latest Ivy jar from http://ant.apache.org/ivy/ into your Ant
installation's lib/ directory.

Second, before running the system, you will need to acquire the EHR
documents for the interface to process. Because of the sensitive nature
of these documents they cannot be kept in public version control.
Contact alexander.p.conrad@gmail.com for a version of the dataset which
is interpretable by this beta version of the tool.

Optionally, the line: in build.xml can be uncommented and edited to
point to the doclist file accompanying the dataset, to load the dataset
immediately upon launch.

Once your system is appropriately configured, navigate to this directory
in a terminal and execute:

-   ant compile
-   ant run

"compile" will invoke Ivy and Ant to download the required third-party
libraries and compile the system, respectively. "run" will launch the
default tab-based interface.

### Architecture

Overall design of the front-end has attempted to roughly follow the
*model-view-controller* design pattern. The primary reason for utilizing
this pattern has been to maintain a logical separation of concerns. As
such, the system consists of a number of front-end *views*, a back-end
data *model*, and a main *controller* which coordinates between the
various components. In addition to the data model, the system also
utilizes a separate NLP module for deriving predictions.

The main controller class is *emr\_vis\_nlp.controller.MainController*.
This monolithic class handles communication back and forth between the
interactive data-presenting *views*, the data *model*, and the NLP
*predictor*, as well as among multiple coordinated *views* themselves.
All purely controller-specific classes, such as auxiliary controllers,
data structures, etc. should be stored alongside *MainController* in the
package *emr\_vis\_nlp.controller*.

### Views

Code relating to a *view* should be stored in *emr\_vis\_nlp.view*, with
classes specific to a particular *view* stored in a subpackage for that
*view* ...

The only exception to this rule is *emr\_vis\_nlp.main.MainTabbedView*;
this is the main class which should be executed to launch the
application. This subclass of *JFrame* contains the overall structural
framework for the interface as a whole. Since this class primarily deals
with layout of standard *Swing* components, it has been designed using
the Netbeans GUI Swing builder. Ideally, changes to this class should be
done through this same Netbeans GUI Builder rather than direct editing
of the code, to allow the project to be cleanly imported into Netbeans
again in the future.

### Models

Code relating to a back-end data *model* should be stored in
*emr\_vis\_nlp.model*. To implement a new *model*, develop a new class
which implements the *emr\_vis\_nlp.model.MainModel* interface. Ideally,
each new *MainModel*-implementing class should be given its own
subpackage inside of *emr\_vis\_nlp.model*, to store any model-specific
classes that are needed. When implementing a new doclist-based
*MainModel*, the method *MainController.setModelFromDoclist* will need
to be updated as well, in order to detect and load the appropriate model
type. For more complex kinds of *MainModel*s, such as JDBC-based models,
*MainTabbedView* may also need to be modified, in order to provide the
additional buttons, menu options, and/or command-line argument parsing
functionality.

Currently, the only *MainModel* implemented so far is
*emr\_vis\_nlp.model.mpqa\_colon.MpqaColonMainModel*. This *MainModel*
is designed to read from an XML-style doclist file in order to load a
MPQA-style "database" containing Dr. Mehrotra's colonoscopy reports.
(note: because of privacy concerns, the data itself cannot be included
in a public repository. Please contact
[alexander.p.conrad@gmail.com](mailto:alexander.p.conrad@gmail.com) for
a copy of the data.)

### Natural Language Processing / Machine Learning Module

Code relating to the NLP/ML components should be stored in
*emr\_vis\_nlp.ml*, with each implemented NLP module residing in its own
sub-package. To develop a new NLP/ML module, implement a subclass of
*emr\_vis\_nlp.ml.MLPredictor*, ensuring that the *loadPredictions()*
method is overridden appropriately. You will also need to modify the
method
*emr\_vis\_nlp.controller.MainController.setModelFromDoclist(File)* to
ensure that the appropriate model is loaded with respect to the chosen
dataset. (Note that, at this time, not all of the classes within
*emr\_vis\_nlp.ml* have been fully implemented; at the time of this
writing, the back-end NLP components remain under development.)

Currently, the only *MLPredictor* implemented so far (aside from
*NullPredictor*, a simple placeholder class for when no predictor is
loaded) is *emr\_vis\_nlp.ml.deprecated.DeprecatedMLPredictor*. This
predictor uses Phuong's exploratory baseline models from Summer 2012
developed on Dr. Mehrotra's colonoscopy reports, and is specific to this
dataset. Note that it may not be methodologically proper to evaluate
this model on the colonoscopy dataset, as some of the documents used in
its training are likely also present in the datasets loaded into the
interface. This model is included in order to provide a provisional
back-end source of NLP predictions until a new model can be developed.
