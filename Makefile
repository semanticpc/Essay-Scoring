run:
	javac -cp src src/app/*.java src/features/*.java src/ml/*.java  src/parser/*.java
	java -cp src app.RunPrediction

docs: javadocs
	javadoc javadoc -private -d javadocs/ -classpath src -sourcepath src app features ml

javadocs:
	mkdir javadocs

stats:
	java -cp lib/weka.jar weka.core.Instances training_essay_set_1.arff

weka:
	java -cp lib/weka.jar weka.classifiers.trees.J48 -t training_essay1_discrete.arff -i
