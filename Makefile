run:
	javac -cp src src/app/RunPrediction.java
	java -cp src app.RunPrediction

docs: javadocs
	javadoc javadoc -private -d javadocs/ -classpath src -sourcepath src app features ml

javadocs:
	mkdir javadocs