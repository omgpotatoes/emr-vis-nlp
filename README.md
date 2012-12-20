emr-vis-nlp
===========

This project is an effort to incorporate NLP and visualization techniques into a single system for viewing, annotating, and revising electronic health records (EHRs). The system is designed to support the needs of three primary use-cases: retrospective EHR analysis for quality assessment or cohort inclusion, NLP annotation, and NLP scheme revision. This code is made available under the New BSD License.


requirements
------------

* JDK 1.7 (or newer)
* Apache Ant 1.8.3
* Apache Ivy 2.3.0


running the system
------------------

First, ensure that your system meets the requirements listed above. If you already have Ant installed, Ivy can be installed by simply unpacking the latest Ivy jar from http://ant.apache.org/ivy/ into your Ant installation's lib/ directory.

Second, before running the system, you will need to acquire the EHR documents for the interface to process. Because of the sensitive nature of these documents they cannot be kept in public version control. Contact alexander.p.conrad@gmail.com for a version of the dataset which is interpretable by this beta version of the tool.

Optionally, the line:

			<!--<arg value="path_to_ehr_doclist"/>-->

in build.xml can be edited to point to the doclist file accompanying the dataset, to load the dataset immediately upon launch.

Once your system is appropriately configured, navigate to this directory in a terminal and execute:

* ant compile
* ant run

"compile" will invoke Ivy and Ant to download the required third-party libraries and compile the system, respectively. "run" will launch the default tab-based interface.


