package weka.core.stemmers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * <p>
 * Ova apstraktna klasa implementira zajedničke funkcije za stemere za srpski opisane u radu:
 * <br>
 * Pristup izgradnji stemera i lematizora za jezike s bogatom fleksijom i oskudnim resursima zasnovan na obuhvatanju sufiksa, Vlado Kešelj, Danko Šipka, Infoteka 9(1-2), 21-31 (2008).
 * <br> 
 * <a href="http://infoteka.bg.ac.rs/pdf/Srp/2008/04%20Vlado-Danko_Stemeri.pdf">http://infoteka.bg.ac.rs/pdf/Srp/2008/04%20Vlado-Danko_Stemeri.pdf</a>
 * <br>
 * (originalna implementacija u Perlu i drugi resursi su dostupni na: <a href="http://www.cs.dal.ca/~vlado/nlp/2007-sr/">http://www.cs.dal.ca/~vlado/nlp/2007-sr/</a>)
 * </p>
 * 
 * <p>
 * i za stemer kreiran u master radu Nikole Miloševića, opisan u ArXiv dokumentu:
 * <br>
 * Stemmer for Serbian language, Nikola Milošević, arXiv preprint arXiv:1209.4471 (2012).
 * <br>
 * <a href="http://arxiv.org/abs/1209.4471">http://arxiv.org/abs/1209.4471</a>
 * </p>
 * 
 * <p>
 * Svi stemeri za srpski koriste tzv. <b>dual1</b> kodovanje u kome se sva ćirilična slova prevode u latinična a svako latinično slovo koje sadrži dijakritičke oznake - š, đ, č, ć, ž, dž -
 * se piše kao skup dva latinična slova bez dijakritičkih oznaka. Pored toga, kod nekih stemera se slova lj/Lj i nj/Nj prevode u oblike ly/Ly i ny/Ny.
 * </p>
 * <br>
 * <p>
 * <i>This abstract class implements the common functions of the stemmers for Serbian described in the paper:</i>
 * <br>
 * <i>A Suffix Subsumption-Based Approach to Building Stemmers and Lemmatizers for Highly Inflectional Languages with Sparse Resources, Vlado Kešelj, Danko Šipka, Infotheca 9(1-2), 23a-33a (2008).</i>
 * <br>
 * <i><a href="http://infoteka.bg.ac.rs/pdf/Eng/2008/INFOTHECA_IX_1-2_May2008_23a-33a.pdf">http://infoteka.bg.ac.rs/pdf/Eng/2008/INFOTHECA_IX_1-2_May2008_23a-33a.pdf</a></i>
 * <br>
 * <i>(the original implementation in Perl and other resources are available at: <a href="http://www.cs.dal.ca/~vlado/nlp/2007-sr/">http://www.cs.dal.ca/~vlado/nlp/2007-sr/</a>)</i>
 * </p>
 * 
 * <p>
 * <i>and the stemmer created in the Master's degree thesis of Nikola Milošević, described in the ArXiv paper:</i>
 * <br>
 * <i>Stemmer for Serbian language, Nikola Milošević, arXiv preprint arXiv:1209.4471 (2012).</i>
 * <br>
 * <i><a href="http://arxiv.org/abs/1209.4471">http://arxiv.org/abs/1209.4471</a></i>
 * </p>
 * <p>
 * <i>All stemmers for Serbian use the so-called <b>dual1</b> coding system in which all Cyrillic letters are transformed into their Latin equivalents and every Latin letter that contains diacritical marks - š, đ, č, ć, ž, dž -
 * is written as a set of two Latin letters without the diacritical marks. Furthermore, in some stemmers the letters lj/Lj and nj/Nj are transformed into the ly/Ly and ny/Ny forms.
 * </i></p>
 * 
 * @author Vuk Batanović
 * <br>
 * @see <i>Reliable Baselines for Sentiment Analysis in Resource-Limited Languages: The Serbian Movie Review Dataset</i>, Vuk Batanović, Boško Nikolić, Milan Milosavljević, in Proceedings of the 10th International Conference on Language Resources and Evaluation (LREC 2016), pp. 2688-2696, Portorož, Slovenia (2016).
 * <br>
 * https://github.com/vukbatanovic/SCStemmers
 * <br>
 */
public abstract class SerbianStemmer extends SCStemmer {
	
	private static final long serialVersionUID = -156167314139821879L;

	/**
	 * Dužina (u karakterima) najdužeg sufiksnog pravila
	 * <p>
	 * <i>Length (in characters) of the longest suffix rule</i>
	 */
	protected int maxSuffixLen = 0;
	
	/**
	 * Spisak sufiksnih pravila
	 * <p>
	 * <i>The list of suffix rules</i>
	 */
	protected HashMap<String, String> rules;
	
	/**
	 * Alocira memoriju za spisak sufiksnih pravila
	 * <p>
	 * <i>Allocates the memory for the list of suffix rules</i>
	 */
	protected void initRules () {
		rules = new LinkedHashMap<String, String> (20000);
	}
	
	public SerbianStemmer () {
		super();
		initMaxSuffixLen();
	}
	
	/**
	 * Pronalazi maksimalnu dužinu sufiksa u sufiksnim pravilima
	 * <p>
	 * <i>Finds the maximal suffix length in the suffix rules</i>
	 */
	protected void initMaxSuffixLen () {
		for (String key: rules.keySet()) {
			if (key.length() > maxSuffixLen)
				maxSuffixLen = key.length();
		}
	}
	
	/**
	 * Stemuje reč koja je napisana u standardnom obliku (ćirilicom ili latinicom)
	 * <p>
	 * <i>Stems a word written in the standard form (in the Cyrillic or Latin script)</i>
	 * @param word Reč koju treba stemovati
	 * <br><i>The word to be stemmed</i>
	 * @return Stemovana reč
	 * <br><i>The stemmed word</i>
	 */
	public String stemWord (String word) {
		word = convertToDual1String(word);
		String stem = stemDual1Word(word);
		return convertToNormalString(stem);
	}
	
	/**
	 * Stemuje liniju teksta koja je napisana u standardnom obliku (ćirilicom ili latinicom)
	 * <p>
	 * <i>Stems a line of text written in the standard form (in the Cyrillic or Latin script)</i>
	 * @param line Linija teksta koju treba obraditi
	 * <br><i>The line of text to be processed</i>
	 * @return Linija teksta sa stemovanim rečima
	 * <br><i>The line of text with stemmed words</i>
	 */
	public String stemLine (String line) {
		line = convertToDual1String(line);
		String stem = stemDual1Line(line);
		return convertToNormalString(stem);
	}
	
	/**
	 * Stemuje reč koja je napisana u dual1 kodiranju
	 * <p>
	 * <i>Stems a word written in the dual1 coding system</i>
	 * @param word Reč koju treba stemovati
	 * <br><i>The word to be stemmed</i>
	 * @return Stemovana reč
	 * <br><i>The stemmed word</i>
	 */
	public abstract String stemDual1Word (String word);
	
	/**
	 * Stemuje liniju teksta koja je napisana u dual1 kodiranju
	 * <p>
	 * <i>Stems a line of text written in the dual1 coding system</i>
	 * @param line Linija teksta koju treba obraditi
	 * <br><i>The line of text to be processed</i>
	 * @return Linija teksta sa stemovanim rečima
	 * <br><i>The line of text with stemmed words</i>
	 */
	public abstract String stemDual1Line (String line);
	
	/**
	 * Stemuje sadržaj ulaznog fajla napisanog u dual1 kodiranju i upisuje ga u izlazni fajl
	 * <p>
	 * <i>Stems the contents of the input file written in the dual1 coding system and writes them to the output file</i>
	 * @param fileInput Ime ulaznog fajla
	 * <br><i>The name of the input file</i>
	 * @param fileOutput Ime izlaznog fajla
	 * <br><i>The name of the output file</i>
	 */
	public void stemDual1File (String fileInput, String fileOutput) {
		try {
			BufferedReader br = new BufferedReader (new FileReader(fileInput));
			PrintWriter pw = new PrintWriter (fileOutput);
			String line = null;
			while ((line = br.readLine()) != null) {
				String stemmed = stemDual1Line(line);
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
	 * Konvertuje zadati string (reč ili liniju teksta) iz dual1 kodiranja u standardan latinični oblik
	 * <p>
	 * <i>Converts the given string (a word or a line of text) from the dual1 coding system into the standard Latin script form</i>
	 * @param wordOrLine String napisan u dual1 kodiranju
	 * <br> <i>A string written in the dual1 coding system</i>
	 * @return String napisan u standardnom obliku
	 * <br> <i>A string written in the standard form</i>
	 */
	public String convertToNormalString (String wordOrLine) {
		String newWord = wordOrLine.replace("cy", "ć");
		newWord = newWord.replace("Cy", "Ć");
		newWord = newWord.replace("cx", "č");
		newWord = newWord.replace("Cx", "Č");
		newWord = newWord.replace("sx", "š");
		newWord = newWord.replace("Sx", "Š");
		newWord = newWord.replace("dx", "dž");
		newWord = newWord.replace("Dx", "Dž");
		newWord = newWord.replace("dy", "đ");
		newWord = newWord.replace("Dy", "Đ");
		newWord = newWord.replace("ly", "lj");
		newWord = newWord.replace("Ly", "Lj");
		newWord = newWord.replace("ny", "nj");
		newWord = newWord.replace("Ny", "Nj");
		newWord = newWord.replace("zx", "ž");
		newWord = newWord.replace("Zx", "Ž");
		return newWord;
	}
	
	
	/**
	 * Konvertuje zadati string (reč ili liniju teksta) iz standardnog oblika (ćirilice ili latinice) u dual1 kodiranje
	 * <p>
	 * <i>Converts the given string (a word or a line of text) from the standard form (in the Cyrillic or Latin script) into the dual1 coding system</i>
	 * @param wordOrLine String napisan u standardnom obliku
	 * <br> <i>A string written in the standard form</i>
	 * @return String napisan u dual1 kodiranju
	 * <br> <i>A string written in the dual1 coding system</i>
	 */
	public String convertToDual1String (String wordOrLine) {
        StringBuffer sb = new StringBuffer();
        if (wordOrLine == null || wordOrLine.equals("")) return "";
        char ch;
        char oldChar = ' ';
        int intCharacter;
        for (int i=0; i<wordOrLine.length(); i++) {
            intCharacter = wordOrLine.codePointAt(i);
            ch = (char) intCharacter;
            String output = convertToDual1Character (intCharacter, oldChar);
            sb.append(output);
            oldChar = ch;
        }
        return sb.toString();
    }
	
	/**
	 * Konvertuje sadržaj zadatog ulaznog fajla iz dual1 kodiranja u standardni latinični oblik i upisuje ga u zadati izlazni fajl
	 * <p>
	 * <i>Converts the contents of a given input file from the dual1 coding system into the standard Latin script form and writes them into the given output file</i>
	 * @param fileInput Ulazni fajl čiji je sadržaj zapisan u dual1 kodiranju
	 * <br> <i>The input file whose contents are written in the dual1 coding system</i>
	 * @param fileOutput Izlazni fajl čiji sadržaj treba da bude zapisan u standardnom obliku
	 * <br> <i>The output file whose contents should be written in the standard form</i>
	 */
	public void convertToNormalFile (String fileInput, String fileOutput) {
		try {
			BufferedReader br = new BufferedReader (new FileReader(fileInput));
			PrintWriter pw = new PrintWriter (fileOutput);
			String line = null;
			while ((line = br.readLine()) != null) {
				String convert = convertToNormalString(line);
				pw.println(convert);
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
	 * Konvertuje sadržaj zadatog ulaznog fajla iz standardnog oblika (ćirilice ili latinice) u dual1 kodiranje i upisuje ga u zadati izlazni fajl
	 * <p>
	 * <i>Converts the contents of a given input file from the standard form (in the Cyrillic or Latin script) into the dual1 coding system and writes them into the given output file</i>
	 * @param fileInput Ulazni fajl čiji je sadržaj zapisan u standardnom obliku
	 * <br> <i>The input file whose contents are written in the standard form</i>
	 * @param fileOutput Izlazni fajl čiji sadržaj treba da bude zapisan u dual1 kodiranju
	 * <br> <i>The output file whose contents should be written in the dual1 coding system</i>
	 */
	public void convertToDual1File (String fileInput, String fileOutput) {
		try {
			BufferedReader br = new BufferedReader (new FileReader(fileInput));
			PrintWriter pw = new PrintWriter (fileOutput);
			String line = null;
			while ((line = br.readLine()) != null) {
				String convert = convertToDual1String(line);
				pw.println(convert);
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
	 * Konvertuje jedan karakter iz standardnog oblika (ćirilice ili latinice) u dual1 kodiranje
	 * <p>
	 * <i>Converts a given character from the standard form (in the Cyrillic or Latin script) to the dual1 coding system</i>
	 * @param intCharacter Unicode kod karaktera koji treba prevesti u dual1 sistem
	 * <br> <i>Unicode code point of the character that should be translated into the dual1 system</i>
	 * @param oldChar Karakter koji je u tekstu prethodio trenutno zadatom karakteru
	 * <br> <i>The character which preceded the currently given one within the text</i>
	 * @return String koji sadrži dual1 reprezentaciju zadatog karaktera
	 * <br><i> A string which contains the dual1 representation of the given character</i>
	 */
	protected abstract String convertToDual1Character (int intCharacter, char oldChar);
}