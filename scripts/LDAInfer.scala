// http://nlp.stanford.edu/software/tmt/0.3/

// tells Scala where to find the TMT classes
import scalanlp.io._;
import scalanlp.stage._;
import scalanlp.stage.text._;
import scalanlp.text.tokenize._;
import scalanlp.pipes.Pipes.global._;

import edu.stanford.nlp.tmt.stage._;
import edu.stanford.nlp.tmt.model.lda._;
import edu.stanford.nlp.tmt.model.llda._;

if (args.length != 3) {
System.err.println("Arguments: modelPath input.tsv output.tsv");
System.err.println("  modelPath:  trained LLDA model");
System.err.println("  input.tsv:  path to input file with two tab separated columns: id, words");
System.err.println("  output.tsv: id followed by (word (label:prob)*)* for each word in each doc");
System.exit(-1);
}

val modelPath = file(args(0));
val inputPath = file(args(1));
val outputPath = file(args(2));

System.err.println("Loading model ...");

val lldaModel = LoadCVB0LabeledLDA(modelPath);
val model = lldaModel.asCVB0LDA;
val source = TSVFile(inputPath) ~> IDColumn(1);
val text = source ~> Column(1) ~> TokenizeWith(model.tokenizer.get);

val dataset = LDADataset(text,model.termIndex);

System.err.println("Generating output ...");

println("Writing document distributions to "+ outputPath +"-document-topic-distributions.csv");

val perDocTopicDistributions = InferCVB0DocumentTopicDistributions(model, dataset);

CSVFile(outputPath +"-document-topic-distributuions.csv").write(perDocTopicDistributions);
println("Writing topic usage to "+ outputPath +"-usage.csv");

val usage = QueryTopicUsage(model, dataset, perDocTopicDistributions);

CSVFile(outputPath +"-usage.csv").write(usage);
println("Estimating per-doc per-word topic distributions");

val perDocWordTopicDistributions = EstimatePerWordTopicDistributions(

model, dataset, perDocTopicDistributions);

 

println("Writing top terms to "+ outputPath +"-top-terms.csv");

val topTerms = QueryTopTerms(model, dataset, perDocWordTopicDistributions, numTopTerms=5);

CSVFile(outputPath +"-top-terms.csv").write(topTerms);
 














