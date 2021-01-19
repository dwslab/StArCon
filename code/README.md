## Unsupervised Stance Detection for Arguments from Consequences - Code

We provide a stance classification system as described in our paper "Unsupervised Stance Detection for Arguments from Consequences". As input, it requires pairs of a topic and a claim. Claims or topics which consist of more than one sentence are not supported. The classification result can be either "pro" or "con". The code is written in java, the dependencies are specified in the pom.xml file. Note that the classifier requires an internet connection to access the online dictionary thesaurus.plus, at least for the first time applied on new data - each query is stored for future use; therefore make sure that the program has permission to write. To be able to use the classifier, you have to perform the steps described below:

1. **Inserting the Stemmer**:
 * Download the Stanford CoreNLP Stemmer: https://github.com/stanfordnlp/CoreNLP/blob/master/src/edu/stanford/nlp/process/Stemmer.java
 * Use it to replace the dummy class utils.Stemmer.java.
 * Replace the first line of the new class utils.Stemmer.java by "package utils;"
 * Remove the main Method: Lines 558-581
 * Remove unused imports: Lines 4-7

2. **Download the following lexicons**:
 * "MPQA": MPQA subjectivity lexicon: https://mpqa.cs.pitt.edu/lexicons/subj_lexicon/ (Note: We recommend to clean the lexicon a bit. There are syntactical and orthographic mistakes, and when dealing with American English one might want to add American English variants of British English words.)
 * "Positives", "Negatives": Opinion Lexicon by Hu & Liu (2014): http://www.cs.uic.edu/~liub/FBS/opinion-lexicon-English.rar
 * "Bigrams", "Unigrams": IBM Debater - Sentiment Composition Lexicons: https://www.research.ibm.com/haifa/dept/vst/debating_data.shtml
 * "EffectLexicon": Connotation Frames: https://homes.cs.washington.edu/~hrashkin/annotated_connotation_frames.zip
 * Alternative to EffectLexicon, not recommended: +/-EffectWordNet: https://mpqa.cs.pitt.edu/lexicons/effect_lexicon/

3. **If using the connotation frames (recommended), set up Wordnet**:
 * Download and unpack "WordNet": http://wordnetcode.princeton.edu/wn3.1.dict.tar.gz
 * Modify the file "Properties.XML" by specifying the location to wordnet.
 * When specifying the location of WordNet in step 4, you have to specify the location to the Properties.XML file.

4. **Specify the locations of the lexicons. You have three possibilites**:
 * Editing the locations in the class "utils.Settings.java".
 * Directly storing the lexicons where specified in "utils.Settings.java" :)
 * When running the program from the command line, you can specify the location by using the expression in quotes in steps 2 and 3, e.g. Positives=PATH_TO_POSITIVES

5. **Specify a dataset**:
 * The dataset has to be a tab-separated file. First column is the topic, second column the claim and the third column the label (pro or con). The third column is optional, but required for the evaluation method (for obvious reasons).
 * To reproduce the results in our paper, you can find the datasets at [/data/datasets](/data/datasets).
 * Specify the location of the dataset either directly in the class "utils.Settings.java" or upon calling the program using dataset=PATH_TO_DATASET.

6. **Run the program**
 * A Java Development Kit needs to be installed. We used the jdk1.8.0_211, but we expect that newer versions are fine.
 * Download and install Maven: https://maven.apache.org/download.cgi
 * Add the lib-folder of Maven to your CLASSPATH
 * Go to the folder where this file is located and use "mvn package"
 * Start the program by using the class app.App (e.g. run "java -cp target/StanceClassifier-0.0.1-SNAPSHOT:target/* app.App [parameters]")
 * Possible parameters:
    * MPQA=SOME_PATH and so on, as listed above
    * dataset=SOME_PATH to specify the location of the dataset
    * predict=true to get the predictions and brief explanations for each datainstance. Default: predict=false
    * evaluate=false for not getting an evaluation, such as F1-scores. This should be especially used when no stance labels are provided in the dataset. Default: evaluate=true
    * ewn=true for using EffectWordNet instead of the extended Connotation Frames. The effect lexicon needs to be set accordingly. Default: ewn=false.
