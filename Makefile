TwoLevel:
	clear
	clear
	javac -cp ".:mallet.jar:mallet-deps.jar:./*" BasicClassifier.java

run:
	java -cp ".:mallet.jar:mallet-deps.jar:./*" BasicClassifier .1	>> out.txt

run2:
	java -cp ".:mallet.jar:mallet-deps.jar:./*" BasicClassifier .1

clean:
	rm *.class
	rm out.txt
