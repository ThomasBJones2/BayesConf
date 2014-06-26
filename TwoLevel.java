import cc.mallet.classify.*;
import cc.mallet.util.*;
import cc.mallet.types.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.types.*;
import cc.mallet.pipe.Pipe;
import java.net.URI;

public class TwoLevel {

    Pipe pipe;
    Tree T;
    boolean foldVal;
    int foldNum;
    static int FOLDS = 10;
    float BayesianThreshold = 0;
    static int NUMTREES = 5;
    static int MAXABS = 3000;

	String [] Aud;
	String [] Gus;
	String [] Olf;
	String [] Tac;
	String [] Vis;
	String [] Arm;
	String [] Fac;
	String [] Foo;
	String [] Han;
	String [] Leg;
	String [] Ocu;

    public TwoLevel() {
        this.pipe = buildPipe();
	this.foldVal = true;
	this.foldNum = 0;
    }

    public void BuildTree(String Root){
	T = RootTree(Root);

    }


    private Tree RootTree(String Root){
		
	String FileName = "";
	String[] terminatorOut = new String[0];
	String LocalName = "";

	if(Root.equals("SM")){

		LocalName = "StimulusModality";

		FileName = "./CogPOTerms/StimulusModality/";

		terminatorOut = new String[]{"Auditory","Gustatory",
				"Interoceptive","None",
				"Olfactory","Tactile","Visual"};
	}

	else if(Root.equals("ST")){

		LocalName = "StimulusType";

		FileName = "./CogPOTerms/StimulusType/";

		terminatorOut =  new String[]{"3DObjects","Accupuncture",
				"AsianCharacters","BrailleDots",
				"BreathableGas","ChordSequences","Clicks",
				"Digits", "ElectricalStimulation",
				"Faces", "FalseFonts", "FilmClip",
				"FixationPoint", "FlashingCheckerboard",
				"Food","Fractals","Heat","InfraredLaser",
				"Infusion", "Music", "Noise", "None",
				"NonverbalVocalSounds", "NonvocalSounds",
				"Objects", "Odor", "Pain", "Pictures", "Point",
				"PointsofLight", "Pseudowords", "RandomDots",
				"ReversedSpeech", "Shapes", "Syllables", 
				"Symbols", "TactileStimulation", "TMS",
				"Tones", "VibratoryStimulation", "Words"};
	}

	else if(Root.equals("RM")){

		LocalName = "ResponseModality";

		FileName = "./CogPOTerms/ResponseModality/";

		terminatorOut =  new String[]{"Facial",
				"Foot","Hand","None","Ocular"};
	}

	else if(Root.equals("RT")){

		LocalName = "ResponseType";

		FileName = "./CogPOTerms/ResponseType/";

		terminatorOut =  new String[]{"ButtonPress","FingerTapping",
				"Flexion","Grasp",
				"manipulate","None","Saccades",
				"Speech"};
	}


	else if(Root.equals("I")){
		LocalName = "Instructions";
		
		FileName = "./CogPOTerms/Instructions/";

		terminatorOut = new String[]{"Attend","Count","Detect","Discriminate",
				"Encode","Fixate","Generate","Imagine",
				"Move","Name","None","Passive","Read",
				"Recall","Repeat","Sing","Smile","Track"};

	}

	String[] empty = new String[0];

	InstanceList ThisLevel = readDirectory(new File(FileName),empty);;
	Classifier ThisBayes = trainClassifierBayes(ThisLevel);

	Classifier[] RootClassifier = new Classifier[1];
	RootClassifier[0] = ThisBayes;

	String[] LocalNames = new String[1];
	LocalNames[0] = LocalName;

	Tree outTree = new Tree("", RootClassifier, LocalNames, SecLevel(terminatorOut, Root));
	return outTree;
    }

    private Tree[] SecLevel(String[] terminator, String Root) {
	Tree [] NoChildren = new Tree[0];
	String[] filenames = new String[4];
	String[] LocalNames = new String[4];
	String[] empty = new String[0];
	String RootName = "";
	if(Root == "SM"){
		RootName = "StimulusModality";
		filenames[0] = "./CogPOTerms/StimulusType/";
		filenames[1] = "./CogPOTerms/ResponseModality/";
		filenames[2] = "./CogPOTerms/ResponseType/";
		filenames[3] = "./CogPOTerms/Instructions/";

		LocalNames[0] = "StimulusType";
		LocalNames[1] = "ResponseModality";
		LocalNames[2] = "ResponseType";
		LocalNames[3] = "Instructions";
	}
	else if(Root == "ST"){
		RootName = "StimulusType";
		filenames[0] = "./CogPOTerms/StimulusModality/";
		filenames[1] = "./CogPOTerms/ResponseModality/";
		filenames[2] = "./CogPOTerms/ResponseType/";
		filenames[3] = "./CogPOTerms/Instructions/";

		LocalNames[0] = "StimulusModality";
		LocalNames[1] = "ResponseModality";
		LocalNames[2] = "ResponseType";
		LocalNames[3] = "Instructions";
	}
	else if(Root == "RM"){
		RootName = "ResponseModality";
		filenames[0] = "./CogPOTerms/StimulusType/";
		filenames[1] = "./CogPOTerms/StimulusModality/";
		filenames[2] = "./CogPOTerms/ResponseType/";
		filenames[3] = "./CogPOTerms/Instructions/";

		LocalNames[0] = "StimulusType";
		LocalNames[1] = "StimulusModality";
		LocalNames[2] = "ResponseType";
		LocalNames[3] = "Instructions";
	}
	else if(Root == "RT"){
		RootName = "ResponseType";
		filenames[0] = "./CogPOTerms/StimulusType/";
		filenames[1] = "./CogPOTerms/ResponseModality/";
		filenames[2] = "./CogPOTerms/StimulusModality/";
		filenames[3] = "./CogPOTerms/Instructions/";

		LocalNames[0] = "StimulusType";
		LocalNames[1] = "ResponseModality";
		LocalNames[2] = "StimulusModality";
		LocalNames[3] = "Instructions";
	}
	else if(Root == "I"){
		RootName = "Instructions";
		filenames[0] = "./CogPOTerms/StimulusType/";
		filenames[1] = "./CogPOTerms/ResponseModality/";
		filenames[2] = "./CogPOTerms/ResponseType/";
		filenames[3] = "./CogPOTerms/StimulusModality/";

		LocalNames[0] = "StimulusType";
		LocalNames[1] = "ResponseModality";
		LocalNames[2] = "ResponseType";
		LocalNames[3] = "StimulusModality";
	}

	Tree[] out = new Tree[terminator.length];

	for(int i = 0; i < terminator.length; i ++){
		String[] RestrictList = new String[1];
		RestrictList[0] = RootName + terminator[i];
		InstanceList[] TrainingSet = new InstanceList[filenames.length];
		Classifier[] Bayesian = new Classifier[filenames.length]; 		
		for(int j = 0; j < filenames.length; j ++){
			TrainingSet[j] = readDirectory(new File(filenames[j]),RestrictList);
			if(TrainingSet[j].size() > 0)
				Bayesian[j] = trainClassifierBayes(TrainingSet[j]);
			else{
				TrainingSet[j] = readDirectory(new File(filenames[j]),empty);	
				Bayesian[j] = trainClassifierBayes(TrainingSet[j]);
			}
		}
		out[i] = new Tree (terminator[i], Bayesian, LocalNames, NoChildren);	
	}	
	return out;

    }

    public Pipe buildPipe() {
        ArrayList pipeList = new ArrayList();

        // Read data from File objects
        pipeList.add(new Input2CharSequence("UTF-8"));

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers, 
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        Pattern tokenPattern =
            Pattern.compile("[\\p{L}\\p{N}_]+");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // Normalize all tokens to all lowercase
        pipeList.add(new TokenSequenceLowercase());

        // Remove stopwords from a standard English stoplist.
        //  options: [case sensitive] [mark deletions]
        pipeList.add(new TokenSequenceRemoveStopwords(false, false));

        // Rather than storing tokens as strings, convert 
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Do the same thing for the "target" field: 
        //  convert a class label string to a Label object,
        //  which has an index in a Label alphabet.
        pipeList.add(new Target2Label());

        // Now convert the sequence of features to a sparse vector,
        //  mapping feature IDs to counts.
        pipeList.add(new FeatureSequence2FeatureVector());

        // Print out the features and the label
 //       pipeList.add(new PrintInputAndTarget());

        return new SerialPipes(pipeList);
    }

    public InstanceList readDirectory(File directory, String[] FilterPhrases) {
        return readDirectories(new File[] {directory}, FilterPhrases);
    }

    public InstanceList readDirectories(File[] directories, String[]  FilterPhrases) {
        
        // Construct a file iterator, starting with the 
        //  specified directories, and recursing through subdirectories.
        // The second argument specifies a FileFilter to use to select
        //  files within a directory.
        // The third argument is a Pattern that is applied to the 
        //   filename to produce a class label. In this case, I've 
        //   asked it to use the last directory name in the path.
        FileIterator iterator =
            new FileIterator(directories,
                             new TxtFilter(".txt", FilterPhrases, this.foldVal, this.foldNum),
                             FileIterator.LAST_DIRECTORY);

        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(pipe);

        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);

        return instances;
    }

    public Classifier trainClassifierBayes(InstanceList trainingInstances) {

        // Here we use a maximum entropy (ie polytomous logistic regression)                               
        //  classifier. Mallet includes a wide variety of classification                                   
        //  algorithms, see the JavaDoc API for details.                                                   

        ClassifierTrainer trainer = new NaiveBayesTrainer();
        return trainer.train(trainingInstances);
    }


    public Classifier loadClassifier(File serializedFile)
        throws FileNotFoundException, IOException, ClassNotFoundException {

        // The standard way to save classifiers and Mallet data                                            
        //  for repeated use is through Java serialization.                                                
        // Here we load a serialized classifier from a file.                                               

        Classifier classifier;

        ObjectInputStream ois =
            new ObjectInputStream (new FileInputStream (serializedFile));
        classifier = (Classifier) ois.readObject();
        ois.close();

        return classifier;
    }


    public void saveClassifier(Classifier classifier, File serializedFile)
        throws IOException {

        // The standard method for saving classifiers in                                                   
        //  Mallet is through Java serialization. Here we                                                  
        //  write the classifier object to the specified file.                                             

        ObjectOutputStream oos =
            new ObjectOutputStream(new FileOutputStream (serializedFile));
        oos.writeObject (classifier);
        oos.close();
    }

public String cleanString (String in){
	String [] check = in.split("/");
	int i = 0;
	while(!Character.isDigit(check[i].charAt(0))){
		i++;
	}
		
	return check[i];	
}

public boolean NotInFile(String FileName, String check) throws IOException{
	char[] in = new char[1000];
	FileReader fis = new FileReader(new File(FileName));
	fis.read(in);
	String [] Correct = (new String(in)).split(" ");
	fis.close();
	boolean out = true;
	for(int i = 0; i < Correct.length; i ++){
		if(Correct[i].equals(check))
			out = false;
	}
	return out;
}


//This function accepts a boolean classifier, a list of abstracts to be tested, and preappend string called checker. 
//It then prints out the classification provided by the classifier in the Test-Abstracts folder under the correct name
//insuring to preappend the checker value...
    public void printLabelings(Tree T, InstanceList testInstances, FileWriter[] fos, FileWriter[] fosAlt, int index, String abstractOutName, String abstractName, int depth) throws IOException {

   	Tree [] children;
        Labeling[] labelings = new Labeling[T.getClassifiers().length];
	for(int i = 0; i < T.getClassifiers().length; i ++)
		labelings[i] = T.getClassifiers(i).classify(testInstances.get(index)).getLabeling();
        // print the labels with their weights in descending order (ie best first) 
    	/******************************************************************************* 
    	 * added by Jiawei, an example shows how to directly pull the correct labels
    	 *******************************************************************************/
	if(depth == 0){
    /*		char [] in = new char[1000];
    		String CheckFileName = "./CogPOTerms/TaggedAbstracts/";
    		FileReader fis = new FileReader(new File(CheckFileName+abstractName));
	    	fis.read(in);
			String [] Correct = (new String(in)).split(" ");
    		for (String correct : Correct) {
    			if (!correct.startsWith(T.getTitles(0)))
    				continue;
			else{
    				fos[index].write(correct+" ");
    				correct = correct.replaceAll(T.getTitles(0), "");
    				children = T.getChildren();
    				for(int child = 0; child < children.length; child++){
        				if(correct.equals(children[child].getValue()))
        					printLabelings(children[child], testInstances, fos, index, abstractOutName, abstractName, depth + 1);
				}
			}	
    		}*/

		char [] in = new char[1000];
		String TestSetFilename = "./CogPOTerms/Abstracts/";
	    	FileReader fis = new FileReader(new File(TestSetFilename+abstractName));
	    	fis.read(in);
		String [] CurAbs = (new String(in)).split(" ");
		fis.close();

		if(this.T.getTitles(0).equals("StimulusModality")){
			boolean check = false;
			if(this.check(this.Vis, CurAbs)){
					check = true;
			        	fos[index].write("StimulusModalityVisual ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Visual").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}	

			if(this.check(this.Aud, CurAbs)){
					check = true;
			        	fos[index].write("StimulusModalityAuditory ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Auditory").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}

			if(this.check(this.Gus, CurAbs)){
					check = true;
			        	fos[index].write("StimulusModalityGustatory ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Gustatory").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 	
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}

			if(this.check(this.Olf, CurAbs)){
					check = true;
			        	fos[index].write("StimulusModalityOlfactory ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Olfactory").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}

			if(this.check(this.Tac, CurAbs)){
					check = true;
			        	fos[index].write("StimulusModalityTactile ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Tactile").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}	
			if(!check){
				//System.err.print("Q was written Stimulus Modality " + abstractName + " " + fosAlt[index].toString() + "\n");
				fosAlt[index].write("Q ");
				fosAlt[index].close();
			}			
		}
			
		else if (this.T.getTitles(0).equals("ResponseModality")){
			boolean check = false;
			if(this.check(this.Arm, CurAbs)){
					check = true;
			        	fos[index].write("ResponseModalityArm ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Arm").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}

			if(this.check(this.Fac, CurAbs)){
					check = true;
			        	fos[index].write("ResponseModalityFacial ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Facial").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}

			if(this.check(this.Foo, CurAbs)){
					check = true;
			        	fos[index].write("ResponseModalityFoot ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Foot").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}

			if(this.check(this.Han, CurAbs)){
					check = true;
			        	fos[index].write("ResponseModalityHand ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Hand").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}

			if(this.check(this.Leg, CurAbs)){
					check = true;
			        	fos[index].write("ResponseModalityLeg ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Leg").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}

			if(this.check(this.Ocu, CurAbs)){
					check = true;
			        	fos[index].write("ResponseModalityOcular ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(("Ocular").equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, 
							fos, fos, index, abstractOutName, abstractName, depth + 1);
					}
			}
			if(!check){
				fosAlt[index].write("Q ");
				//System.err.print("Q was written Response Modality\n");
				fosAlt[index].close();
			}	

		}


		
		else for(int j = 0; j < labelings.length; j++){
        		for (int rank = 0; rank < labelings[j].numLocations(); rank++){
        			if(labelings[j].getValueAtRank(rank) > BayesianThreshold){// + .5){
        				fos[index].write(T.getTitles(j)+labelings[j].getLabelAtRank(rank).toString()+" ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(labelings[j].getLabelAtRank(rank).toString().equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, fos, fos, index, abstractOutName, abstractName, depth + 1);
					}	
        			}
        			else if(labelings[j].getValueAtRank(rank) > BayesianThreshold){
        				fosAlt[index].write(T.getTitles(j)+labelings[j].getLabelAtRank(rank).toString()+" ");
    					children = T.getChildren();
    					for(int child = 0; child < children.length; child++){
        					if(labelings[j].getLabelAtRank(rank).toString().equals(children[child].getValue()))
        						printLabelings(children[child], testInstances, fosAlt, fosAlt, index, abstractOutName, abstractName, depth + 1);
					} 	
        			}
				
        		}
		}

    		//fis.close();
    		//System.out.print("\n");
    		return;		
	}
    	/*******************************************************************************/
    	else{                
		for(int j = 0; j < labelings.length; j++){
        		for (int rank = 0; rank < labelings[j].numLocations(); rank++){
        			if(labelings[j].getValueAtRank(rank) > BayesianThreshold){
        				fos[index].write(T.getTitles(j)+labelings[j].getLabelAtRank(rank).toString()+" ");
        				//System.out.print(T.getTitles(j)+labelings[j].getLabelAtRank(rank).toString()+" ");
        			}	
        		}
		}
	}

    }

public void ClearDirectory(String Directory){

 	File filedel = new File(Directory);        
        String[] myFiles;      
            if(filedel.isDirectory()){  
                myFiles = filedel.list();  
                for (int i=0; i<myFiles.length; i++) {  
                    File myFile = new File(filedel, myFiles[i]);   
                    myFile.delete();  
                }  
             } 
}

public void WriteFold(int [] K_Fold) throws IOException {

 	File filewrite = new File("./CogPOTerms/FoldAbstracts" + this.foldNum + "/");
	File fileread = new File("./CogPOTerms/Abstracts/");        
        String[] myFiles;      
            if(fileread.isDirectory()){  
                myFiles = fileread.list();  
                for (int i=0; i<myFiles.length; i++) {  
                    FileWriter fos = new FileWriter( new File(filewrite, myFiles[i]));   
                    fos.write((char)(K_Fold[i] + 48));
		    fos.close();  
                }  
             } 

}
	
	String TreeVal(int trees){
		if(trees == 0)
			return "SM";
		else if(trees == 1)
			return "ST";
		else if(trees == 2)
			return "RM";
		else if(trees == 3)
			return "RT";
		else
			return "I";
	}


public boolean check(String[] in, String[] inlist){
	boolean out = false;
		for(int i = 0; i < inlist.length; i ++)
			for(int j = 0; j < in.length; j ++){
				if(in[j].equals(inlist[i]))
					out = true;
		}
	return out;	
}

    public static void main (String[] args) throws IOException {
	String TestSetFilename = "./CogPOTerms/Abstracts/";
	String OutFileName = "./CogPOTerms/TestAbstracts";
	String AltFileName = "./CogPOTerms/Human";
	String FoldFileName = "./CogPOTerms/FoldAbstracts/";
	Scorer MyScorer = new Scorer();
	String Root;

	for(int tree = 0; tree < NUMTREES; tree ++){
		for (int k = 0; k < FOLDS; k ++){	
			System.err.print("Currently on tree: " + tree + " and K " + k + "\n");
			int []K_fold = new int[MAXABS];
			Random generator = new Random();

			for(int i = 0; i < MAXABS; i ++){
				K_fold[i] = Math.abs(generator.nextInt()%FOLDS);
			}

        		TwoLevel twolevel = new TwoLevel();
			twolevel.foldNum = k;
			twolevel.ClearDirectory(OutFileName + twolevel.foldNum + "/");
			twolevel.ClearDirectory(FoldFileName + twolevel.foldNum + "/");
			twolevel.ClearDirectory(AltFileName + twolevel.foldNum + "/");
			twolevel.WriteFold(K_fold);


			Root = twolevel.TreeVal(tree);


			System.out.print("Currently on TwoLevel Tree: " + Root + "\n");

			String[] empty = new String[0];

			twolevel.BayesianThreshold = Float.parseFloat(args[0]);

			twolevel.BuildTree(Root);

			twolevel.foldVal = false;

			InstanceList testInstances = twolevel.readDirectory(new File(TestSetFilename), empty);

			//Here I get the abstract name for the guy I am writing out..;
			FileWriter[] fos = new FileWriter[testInstances.size()];    
			FileWriter[] fosAlt = new FileWriter[testInstances.size()];    
			String[] FileNames = new String[testInstances.size()];
			String[] AbstractNames = new String[testInstances.size()];
			for(int i = 0; i < testInstances.size(); i ++){
				String AbstractName = twolevel.cleanString(testInstances.get(i).getName().toString());
				fos[i] = new FileWriter(new File(OutFileName + twolevel.foldNum + "/" + AbstractName));
				fosAlt[i] = new FileWriter(new File(AltFileName + twolevel.foldNum + "/" + AbstractName));
				FileNames[i] = OutFileName + twolevel.foldNum + "/" + AbstractName;
				AbstractNames[i] = AbstractName;
				
			}

			char [] SMVis = new char[1000];
			char [] SMGus = new char[1000];
			char [] SMOlf = new char[1000];
			char [] SMTac = new char[1000];
			char [] SMAud = new char[1000];


			char [] RMArm = new char[1000];
			char [] RMFac = new char[1000];
			char [] RMFoo = new char[1000];
			char [] RMHan = new char[1000];
			char [] RMLeg = new char[1000];
			char [] RMOcu = new char[1000];

			FileReader fis = new FileReader(new File("./BOWSM/BagOfWords_StimulusModality_Auditory"));
			fis.read(SMAud);
			twolevel.Aud = (new String(SMAud)).split(" ");
			fis.close();

			fis = new FileReader(new File("./BOWSM/BagOfWords_StimulusModality_Gustatory"));
			fis.read(SMGus);
			twolevel.Gus = (new String(SMGus)).split(" ");
			fis.close();


			fis = new FileReader(new File("./BOWSM/BagOfWords_StimulusModality_Olfactory"));
			fis.read(SMOlf);
			twolevel.Olf = (new String(SMOlf)).split(" ");
			fis.close();


			fis = new FileReader(new File("./BOWSM/BagOfWords_StimulusModality_Tactile"));
			fis.read(SMTac);
			twolevel.Tac = (new String(SMTac)).split(" ");
			fis.close();


			fis = new FileReader(new File("./BOWSM/BagOfWords_StimulusModality_Visual"));
			fis.read(SMVis);
			twolevel.Vis = (new String(SMVis)).split(" ");
			fis.close();


			fis = new FileReader(new File("./BOWRM/BagOfWords_ResponseModality_Arm"));
			fis.read(RMArm);
			twolevel.Arm = (new String(RMArm)).split(" ");
			fis.close();


			fis = new FileReader(new File("./BOWRM/BagOfWords_ResponseModality_Facial"));
			fis.read(RMFac);
			twolevel.Fac = (new String(RMFac)).split(" ");
			fis.close();



			fis = new FileReader(new File("./BOWRM/BagOfWords_ResponseModality_Foot"));
			fis.read(RMFoo);
			twolevel.Foo = (new String(RMFoo)).split(" ");
			fis.close();


			fis = new FileReader(new File("./BOWRM/BagOfWords_ResponseModality_Hand"));
			fis.read(RMHan);
			twolevel.Han = (new String(RMHan)).split(" ");
			fis.close();


			fis = new FileReader(new File("./BOWRM/BagOfWords_ResponseModality_Leg"));
			fis.read(RMLeg);
			twolevel.Leg = (new String(RMLeg)).split(" ");
			fis.close();


			fis = new FileReader(new File("./BOWRM/BagOfWords_ResponseModality_Ocular"));
			fis.read(RMOcu);
			twolevel.Ocu = (new String(RMOcu)).split(" ");
			fis.close();

			//Here I print out the results from my classifier...
			for(int index = 0; index < testInstances.size(); index++){
				twolevel.printLabelings(twolevel.T, testInstances, fos, fosAlt, index, FileNames[index], AbstractNames[index], 0);
			}

			for(int i = 0; i < testInstances.size(); i ++){
				fos[i].close();
			}
		}	
		MyScorer.score(tree);
	}
    	
}

}


