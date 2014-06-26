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

import java.nio.channels.FileChannel;


public  class Scorer {

    Pipe pipe;
    int foldNum;
    static int FOLDS = 10;
    static int NUMTREES = 1;
	String CheckFileName = "./CogPOTerms/TaggedAbstracts/";
	
	String OutFileName = "./CogPOTerms/TestAbstracts";

	String[] empty = new String[0];

	String HumanFileName = "./CogPOTerms/Human";

	String AbsFileName = "./CogPOTerms/Abstracts/";
	String ConfName = "./ConfLearn/";

	String SMterminator[] = new String[]{"Auditory","Gustatory",
				"Interoceptive","None",
				"Olfactory","Tactile","Visual"};

	String STterminator[] = new String[]{"3DObjects","Accupuncture",
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

	String Iterminator[] = new String[]{"Attend","Count","Detect","Discriminate",
				"Encode","Fixate","Generate","Imagine",
				"Move","Name","None","Passive","Read",
				"Recall","Repeat","Sing","Smile","Track"};

	String RMterminator[] = new String[]{"Facial",
				"Foot","Hand","None","Ocular"};

	String RTterminator[] = new String[]{"ButtonPress","FingerTapping",
				"Flexion","Grasp",
				"manipulate","None","Saccades",
				"Speech"};

	double[][][] SMDen = new double[NUMTREES][FOLDS][SMterminator.length + 1];
	double[][][] SMNum = new double[NUMTREES][FOLDS][SMterminator.length + 1];

	double[][][] STDen = new double[NUMTREES][FOLDS][STterminator.length + 1];
	double[][][] STNum = new double[NUMTREES][FOLDS][STterminator.length + 1];

	double[][][] RMDen = new double[NUMTREES][FOLDS][RMterminator.length + 1];
	double[][][] RMNum = new double[NUMTREES][FOLDS][RMterminator.length + 1];

	double[][][] RTDen = new double[NUMTREES][FOLDS][RTterminator.length + 1];
	double[][][] RTNum = new double[NUMTREES][FOLDS][RTterminator.length + 1];

	double[][][] IDen = new double[NUMTREES][FOLDS][Iterminator.length + 1];
	double[][][] INum = new double[NUMTREES][FOLDS][Iterminator.length + 1];

    public Scorer() {
        pipe = buildPipe();
	foldNum = 0;


	for(int i = 0; i < NUMTREES; i++){
		for(int j = 0; j < FOLDS; j ++){
			for(int k = 0; k < SMterminator.length + 1; k ++){
				SMDen[i][j][k] = 0;
				SMNum[i][j][k] = 0;
			}
			for(int k = 0; k < STterminator.length + 1; k ++){
				STDen[i][j][k] = 0;
				STNum[i][j][k] = 0;
			}
			for(int k = 0; k < RMterminator.length + 1; k ++){
				RMDen[i][j][k] = 0;
				RMNum[i][j][k] = 0;
			}
			for(int k = 0; k < RTterminator.length + 1; k ++){
				RTDen[i][j][k] = 0;
				RTNum[i][j][k] = 0;
			}
			for(int k = 0; k < Iterminator.length + 1; k ++){
				IDen[i][j][k] = 0;
				INum[i][j][k] = 0;
			}
		}
	}
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
                             new TxtFilter(".txt", FilterPhrases, false, this.foldNum),
                             FileIterator.LAST_DIRECTORY);

        // Construct a new instance list, passing it the pipe
        //  we want to use to process instances.
        InstanceList instances = new InstanceList(pipe);

        // Now process each instance provided by the iterator.
        instances.addThruPipe(iterator);

        return instances;
    }

public String cleanString (String in){
		String [] check = in.split("/");
		return check[check.length-1];	
	}

public boolean check(String in, String[] inlist){
	boolean out = false;
		for(int i = 0; i < inlist.length; i ++){
			if(in.equals(inlist[i]))
				out = true;
		}
	return out;	
}

public double stdev(double[] in, double avg){
	double Accum = 0;
	for(int i = 0; i < FOLDS; i ++)
		Accum += (in[i] - avg)*(in[i] - avg);
	Accum = Accum/((double)FOLDS - 1.0);

	return 2*Math.sqrt(Accum);
}

public static void copyFile(File sourceFile, File destFile) throws IOException {
    if(!destFile.exists()) {
        destFile.createNewFile();
    }

    FileChannel source = null;
    FileChannel destination = null;

    try {
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        destination.transferFrom(source, 0, source.size());
    }
    finally {
        if(source != null) {
            source.close();
        }
        if(destination != null) {
            destination.close();
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

void score(int TreeNum) throws IOException{
	//Scorer();

	//Here I am grabbing the correct values.


	for(int k = 0; k < FOLDS; k++){
	this.foldNum = k;

	InstanceList testInstances = this.readDirectory(new File(OutFileName+this.foldNum+"/"), empty);


	//=============================================
	// Here I do the scoring for stimulus modality
	for(int i = 0; i < SMterminator.length; i ++){
		for(int index = 0; index < testInstances.size(); index ++){
			char [] in = new char[1000];
			char [] in2 = new char[1000];


	    		String AbstractName = this.cleanString(testInstances.get(index).getName().toString());
	    		FileReader fis = new FileReader(new File(CheckFileName+AbstractName));
	    		fis.read(in);
			String [] Correct = (new String(in)).split(" ");
			fis.close();

			fis = new FileReader(new File(OutFileName + this.foldNum + "/" +AbstractName));
			fis.read(in2);
			String [] Guess = (new String(in2)).split(" ");
			fis.close();




			boolean guessed = false;
			if(this.check("StimulusModality" + SMterminator[i], Correct)){
				SMDen[TreeNum][k][i] ++;
				guessed = true;
			}
			if(this.check("StimulusModality" + SMterminator[i], Guess)){
				SMDen[TreeNum][k][i] ++;
				guessed = true;
			}
			if(this.check("StimulusModality" + SMterminator[i], Guess) && 
				this.check("StimulusModality" + SMterminator[i], Correct)){
				SMNum[TreeNum][k][i] ++;
				File source = new File(AbsFileName + AbstractName);
				File dest = new File(ConfName + "SMC/" + AbstractName);
				copyFile(source, dest);
			}
			else if(guessed){
				File source = new File(AbsFileName + AbstractName);
				File dest = new File(ConfName + "SMI/" + AbstractName);
				copyFile(source, dest);
			}
				
		}
		SMNum[TreeNum][k][SMterminator.length] += SMNum[TreeNum][k][i];
		SMDen[TreeNum][k][SMterminator.length] += SMDen[TreeNum][k][i];
		
	}
	if(k == FOLDS-1 && TreeNum == NUMTREES-1){
		double [] out;
		out = new double[NUMTREES];
		for(int v = 0; v < NUMTREES; v ++)
			out[v] = 0;
		double stdev = 0;
		double [][] stdhelp = new double[NUMTREES][FOLDS];
		for(int v = 0; v < NUMTREES; v ++){
			for(int l = 0; l < FOLDS; l ++){
				//Here we calculate the f-score for each point in sthelp and we calculate the
				//average f-score in out...
				if(SMDen[v][l][SMterminator.length] > 0){
					stdhelp[v][l] = (2.0*SMNum[v][l][SMterminator.length]/SMDen[v][l][SMterminator.length]);
					out[v] += (2.0*SMNum[v][l][SMterminator.length]/SMDen[v][l][SMterminator.length]);
				}
				else
					stdhelp[v][l] = 0;
			}
			out[v] = out[v]/FOLDS;
			stdev = this.stdev(stdhelp[v] , out[v]);
			System.out.print("Stimulus Modality[" + v + "]: " + out[v] + " +/-" + stdev + "\n\n");
		}

	}


	//=============================================
	// Here I do the scoring for stimulus type
	//============================================= 

	for(int i = 0; i < STterminator.length; i ++){
		for(int index = 0; index < testInstances.size(); index ++){
			char [] in = new char[1000];
			char [] in2 = new char[1000];


	    		String AbstractName = this.cleanString(testInstances.get(index).getName().toString());
	    		FileReader fis = new FileReader(new File(CheckFileName+AbstractName));
	    		fis.read(in);
			String [] Correct = (new String(in)).split(" ");
			fis.close();

			fis = new FileReader(new File(OutFileName + this.foldNum + "/" +AbstractName));
			fis.read(in2);
			String [] Guess = (new String(in2)).split(" ");
			fis.close();

			boolean guessed = false;
			if(this.check("StimulusType" + STterminator[i], Correct)){
				STDen[TreeNum][k][i] ++;
				guessed = true;
			}
			if(this.check("StimulusType" + STterminator[i], Guess)){
				STDen[TreeNum][k][i] ++;
				guessed = true;
			}
			if(this.check("StimulusType" + STterminator[i], Guess) && 
				this.check("StimulusType" + STterminator[i], Correct)){
				STNum[TreeNum][k][i] ++;
				File source = new File(AbsFileName + AbstractName);
				File dest = new File(ConfName + "STC/" + AbstractName);
				copyFile(source, dest);
			}
			else if(guessed){
				File source = new File(AbsFileName + AbstractName);
				File dest = new File(ConfName + "STI/" + AbstractName);
				copyFile(source, dest);
			}	
		}

		STNum[TreeNum][k][STterminator.length] += STNum[TreeNum][k][i];
		STDen[TreeNum][k][STterminator.length] += STDen[TreeNum][k][i];

	}
	if(k == FOLDS-1 && TreeNum == NUMTREES-1){
		double [] out;
		out = new double[NUMTREES];
		for(int v = 0; v < NUMTREES; v ++)
			out[v] = 0;
		double stdev = 0;
		double [][] stdhelp = new double[NUMTREES][FOLDS];
		for(int v = 0; v < NUMTREES; v ++){
			for(int l = 0; l < FOLDS; l ++){
				//Here we calculate the f-score for each point in sthelp and we calculate the
				//average f-score in out...
				if(STDen[v][l][STterminator.length] > 0){
					stdhelp[v][l] = (2.0*STNum[v][l][STterminator.length]/STDen[v][l][STterminator.length]);
					out[v] += (2.0*STNum[v][l][STterminator.length]/STDen[v][l][STterminator.length]);
				}
				else
					stdhelp[v][l] = 0;
			}
			out[v] = out[v]/FOLDS;
			stdev = this.stdev(stdhelp[v] , out[v]);
			System.out.print("Stimulus Type[" + v + "]: " + out[v] + " +/-" + stdev + "\n\n");
		}
	}
	//}



	//=============================================
	// Here I do the scoring for Response Modality
	//============================================= 

	for(int i = 0; i < RMterminator.length; i ++){
		for(int index = 0; index < testInstances.size(); index ++){
			char [] in = new char[1000];
			char [] in2 = new char[1000];

	    		String AbstractName = this.cleanString(testInstances.get(index).getName().toString());
	    		FileReader fis = new FileReader(new File(CheckFileName+AbstractName));
	    		fis.read(in);
			String [] Correct = (new String(in)).split(" ");
			fis.close();

			fis = new FileReader(new File(OutFileName + this.foldNum + "/" +AbstractName));
			fis.read(in2);
			fis.close();
			String [] Guess = (new String(in2)).split(" ");

			boolean guessed = false;
			if(this.check("ResponseModality" + RMterminator[i], Correct)){
				RMDen[TreeNum][k][i] ++;
				guessed = true;
			}
			if(this.check("ResponseModality" + RMterminator[i], Guess)){
				RMDen[TreeNum][k][i] ++;
				guessed = true;
			}
			if(this.check("ResponseModality" + RMterminator[i], Guess) && 
				this.check("ResponseModality" + RMterminator[i], Correct)){
				RMNum[TreeNum][k][i] ++;
				File source = new File(AbsFileName + AbstractName);
				File dest = new File(ConfName + "RMC/" + AbstractName);
				copyFile(source, dest);
			}
			else if(guessed){
				File source = new File(AbsFileName + AbstractName);
				File dest = new File(ConfName + "RMI/" + AbstractName);
				copyFile(source, dest);
			}	

		}

		RMNum[TreeNum][k][RMterminator.length] += RMNum[TreeNum][k][i];
		RMDen[TreeNum][k][RMterminator.length] += RMDen[TreeNum][k][i];

	}
	if(k == FOLDS-1 && TreeNum == NUMTREES-1){
		double [] out;
		out = new double[NUMTREES];
		for(int v = 0; v < NUMTREES; v ++)
			out[v] = 0;
		double stdev = 0;
		double [][] stdhelp = new double[NUMTREES][FOLDS];
		for(int v = 0; v < NUMTREES; v ++){
			for(int l = 0; l < FOLDS; l ++){
				//Here we calculate the f-score for each point in sthelp and we calculate the
				//average f-score in out...
				if(RMDen[v][l][RMterminator.length] > 0){
					stdhelp[v][l] = (2.0*RMNum[v][l][RMterminator.length]/RMDen[v][l][RMterminator.length]);
					out[v] += (2.0*RMNum[v][l][RMterminator.length]/RMDen[v][l][RMterminator.length]);
				}
				else
					stdhelp[v][l] = 0;
			}
			out[v] = out[v]/FOLDS;
			stdev = this.stdev(stdhelp[v] , out[v]);
			System.out.print("Response Modality[" + v + "]: " + out[v] + " +/-" + stdev + "\n\n");
		}
	}



	//=============================================
	// Here I do the scoring for Response Type
	//============================================= 

	for(int i = 0; i < RTterminator.length; i ++){
		for(int index = 0; index < testInstances.size(); index ++){
			char [] in = new char[1000];
			char [] in2 = new char[1000];

	    		String AbstractName = this.cleanString(testInstances.get(index).getName().toString());
	    		FileReader fis = new FileReader(new File(CheckFileName+AbstractName));
	    		fis.read(in);
			String [] Correct = (new String(in)).split(" ");
			fis.close();

			fis = new FileReader(new File(OutFileName + this.foldNum + "/" +AbstractName));
			fis.read(in2);
			String [] Guess = (new String(in2)).split(" ");
			fis.close();

			
			boolean guessed = false;
			if(this.check("ResponseType" + RTterminator[i], Correct)){
				RTDen[TreeNum][k][i] ++;
				guessed = true;
			}
			if(this.check("ResponseType" + RTterminator[i], Guess)){
				RTDen[TreeNum][k][i] ++;
				guessed = true;
			}
			if(this.check("ResponseType" + RTterminator[i], Guess) && 
				this.check("ResponseType" + RTterminator[i], Correct)){
				RTNum[TreeNum][k][i] ++;
				File source = new File(AbsFileName + AbstractName);
				File dest = new File(ConfName + "RTC/" + AbstractName);
				copyFile(source, dest);
			}
			else if(guessed){
				File source = new File(AbsFileName + AbstractName);
				File dest = new File(ConfName + "RTI/" + AbstractName);
				copyFile(source, dest);
			}	

		}

		RTNum[TreeNum][k][RTterminator.length] += RTNum[TreeNum][k][i];
		RTDen[TreeNum][k][RTterminator.length] += RTDen[TreeNum][k][i];

	}
	if(k == FOLDS-1 && TreeNum == NUMTREES-1){
		double [] out;
		out = new double[NUMTREES];
		for(int v = 0; v < NUMTREES; v ++)
			out[v] = 0;
		double stdev = 0;
		double [][] stdhelp = new double[NUMTREES][FOLDS];
		for(int v = 0; v < NUMTREES; v ++){
			for(int l = 0; l < FOLDS; l ++){
				//Here we calculate the f-score for each point in sthelp and we calculate the
				//average f-score in out...
				if(RTDen[v][l][RTterminator.length] > 0){
					stdhelp[v][l] = (2.0*RTNum[v][l][RTterminator.length]/RTDen[v][l][RTterminator.length]);
					out[v] += (2.0*RTNum[v][l][RTterminator.length]/RTDen[v][l][RTterminator.length]);
				}
				else
					stdhelp[v][l] = 0;
			}
			out[v] = out[v]/FOLDS;
			stdev = this.stdev(stdhelp[v] , out[v]);
			System.out.print("Response Type[" + v + "]: " + out[v] + " +/-" + stdev + "\n\n");
		}
	}
	//}

	//=============================================
	// Here I do the scoring for Instructions
	//============================================= 

	for(int i = 0; i < Iterminator.length; i ++){
		for(int index = 0; index < testInstances.size(); index ++){
			char [] in = new char[1000];
			char [] in2 = new char[1000];
			char [] in3 = new char[1000];

	    		String AbstractName = this.cleanString(testInstances.get(index).getName().toString());
	    		FileReader fis = new FileReader(new File(CheckFileName+AbstractName));
	    		fis.read(in);
			String [] Correct = (new String(in)).split(" ");
			fis.close();

			fis = new FileReader(new File(OutFileName + this.foldNum + "/" +AbstractName));
			fis.read(in2);
			String [] Guess = (new String(in2)).split(" ");
			fis.close();

			boolean guessed = false;
			if(this.check("Instructions" + Iterminator[i], Correct)){
				IDen[TreeNum][k][i] ++;
				guessed = true;			
			}
			if(this.check("Instructions" + Iterminator[i], Guess)){
				IDen[TreeNum][k][i] ++;
				guessed = true;			
			}
			if(this.check("Instructions" + Iterminator[i], Guess) && 
				this.check("Instructions" + Iterminator[i], Correct)){
				INum[TreeNum][k][i] ++;
				File source = new File(AbsFileName + AbstractName);
				File dest = new File(ConfName + "IC/" + AbstractName);
				copyFile(source, dest);
			}	
			else if(guessed){
				File source = new File(AbsFileName + AbstractName);
				File dest = new File(ConfName + "II/" + AbstractName);
				copyFile(source, dest);

			}
		}
		INum[TreeNum][k][Iterminator.length] += INum[TreeNum][k][i];
		IDen[TreeNum][k][Iterminator.length] += IDen[TreeNum][k][i];
	}

	if(k == FOLDS-1 && TreeNum == NUMTREES-1){
		double [] out;
		out = new double[NUMTREES];
		for(int v = 0; v < NUMTREES; v ++)
			out[v] = 0;
		double stdev = 0;
		double [][] stdhelp = new double[NUMTREES][FOLDS];
		for(int v = 0; v < NUMTREES; v ++){
			for(int l = 0; l < FOLDS; l ++){
				//Here we calculate the f-score for each point in sthelp and we calculate the
				//average f-score in out...
				if(IDen[v][l][Iterminator.length] > 0){
					stdhelp[v][l] = (2.0*INum[v][l][Iterminator.length]/IDen[v][l][Iterminator.length]);
					out[v] += (2.0*INum[v][l][Iterminator.length]/IDen[v][l][Iterminator.length]);
				}
				else
					stdhelp[v][l] = 0;
			}
			out[v] = out[v]/FOLDS;
			stdev = this.stdev(stdhelp[v] , out[v]);
			System.out.print("Instructions [" + v + "]: " + out[v] + " +/-" + stdev + "\n\n");
		}
	}
	}

}

}
