#!/bin/bash
java -jar tmt-0.4.0.jar LDATrain.scala
java -jar tmt-0.4.0.jar LDAInfer.scala llda-cvb0-train LDAAbstractTest.csv out.csv
java ExtractTopTerms
