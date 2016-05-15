package weka.core.stemmers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Pattern;

import weka.core.RevisionUtils;

/**
 * Osnovna apstraktna klasa za funkcije zajedničke za sve stemere za srpski i hrvatski
 * <p>
 * <i>The basic abstract class for the functions common to all stemmers for Serbian and Croatian</i>
 * 
 * @author Vuk Batanović
 * <br>
 * @see <i>Reliable Baselines for Sentiment Analysis in Resource-Limited Languages: The Serbian Movie Review Dataset</i>, Vuk Batanović, Boško Nikolić, Milan Milosavljević, in Proceedings of the 10th International Conference on Language Resources and Evaluation (LREC 2016), pp. 2688-2696, Portorož, Slovenia (2016).
 * <br>
 * https://github.com/vukbatanovic/SCStemmers
 * <br>
 */
public abstract class SCStemmer implements weka.core.stemmers.Stemmer {

	private static final long serialVersionUID = 9203297959472286928L;

	public SCStemmer () {
		initRules ();
	}
	
	/**
	 * Implementira interfejs iz Weka paketa
	 * <p>
	 * <i>Implements the interface given by Weka</i>
	 */
	public String stem(String word) {
		return stemWord(word);
	}
	
    public String getRevision() {
        return RevisionUtils.extract("$Revision: 1.0.0 $");
    }
	
	/**
	 * Olakšava poređenje stemovanih fajlova sa izlazom nekih izvornih implementacija tako što upisuje svaki token u poseban red izlaznog fajla
	 * <p>
	 * <i>Makes it easier to compare stemmed files with the output of some of the original implementations by writing every token in a separate line of the output file</i>
	 * @param fileInput Ime ulaznog fajla u kome je tekst normalno napisan.
	 * <br><i>The name of the input file in which the text is normally written.</i>
	 * @param fileOutput Ime izlaznog fajla u kome tekst treba da bude napisan tako da svaka reč bude u posebnom redu.
	 * <br><i>The name of the output file in which text should be written one word per line.</i>
	 */
	public void replaceSpaceWithNewLine (String fileInput, String fileOutput) {
		try {
			BufferedReader br = new BufferedReader (new FileReader(fileInput));
			PrintWriter pw = new PrintWriter (fileOutput);
			String line = null;
			while ((line = br.readLine()) != null) {
				line = Pattern.compile("\\W+", Pattern.UNICODE_CHARACTER_CLASS).matcher(line).replaceAll(" ");
				String [] words = line.split("\\s");
				for (String s: words) {
					pw.println(s);
				}
			}
			br.close();
			pw.flush();
			pw.close();
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}
	
	public abstract String stemWord (String word);
	
	public abstract String stemLine (String line);
	
	/**
	 *	Stemuje string koji sadrži više linija teksta
	 *	<p> 
	 *	<i>Stems a string which contains multiple lines of text</i>
	 * @param text String koji sadrži više linija teksta
	 * <br><i>The string which contains multiple lines of text</i>
	 * @return String koji sadrži više linija teksta sa stemovanim rečima
	 * <br><i>The string which contains multiple lines of text with stemmed words</i>
	 */
	public String stemText (String text) {
		String [] lines = text.split("\n");
		StringBuffer sb = new StringBuffer ();
		for (String line: lines)
			sb.append(stemLine(line)).append("\n");
		return sb.toString().trim();
	}
	
	/**
	 * Stemuje sadržaj ulaznog fajla i upisuje stemovani sadržaj u izlazni fajl
	 * <p>
	 * <i>Stems the contents of the input file and writes them into the output file</i>
	 * @param fileInput Ime ulaznog fajla
	 * <br><i>The name of the input file</i>
	 * @param fileOutput Ime izlaznog fajla
	 * <br><i>The name of the output file</i>
	 */
	public void stemFile (String fileInput, String fileOutput) {
		try {
			BufferedReader br = new BufferedReader (new FileReader(fileInput));
			PrintWriter pw = new PrintWriter (fileOutput);
			String line = null;
			while ((line = br.readLine()) != null) {
				String stemmed = stemLine(line);
				pw.println(stemmed);
			}
			br.close();
			pw.flush();
			pw.close();
		}
		catch (IOException e) {
			System.out.println(e);
		}
	}
	
	/**
	 * Inicijalizuje pravila za stemovanje
	 * <p>
	 * <i>Initializes the stemming rules</i>
	 */
	protected abstract void initRules ();
}