package weka.core.stemmers;

import java.util.HashMap;

/**
 * <p>
 * Ova klasa implementira stemer za srpski kreiran u master radu Nikole Miloševića, opisan u ArXiv dokumentu:
 * <br>
 * Stemmer for Serbian language, Nikola Milošević, arXiv preprint arXiv:1209.4471 (2012).
 * <br>
 * <a href="http://arxiv.org/abs/1209.4471">http://arxiv.org/abs/1209.4471</a>
 * </p>
 * <p>
 * Ovaj stemer koristi blago modifikovanu verziju tzv. <b>dual1</b> kodovanja u kome se sva ćirilična slova prevode u latinična a svako latinično slovo koje sadrži dijakritičke oznake - š, đ, č, ć, ž, dž -
 * se piše kao skup dva latinična slova bez dijakritičkih oznaka. Za razliku od stemera Kešelja i Šipke, ovde se slova lj/Lj i nj/Nj <b>NE</b> prevode u oblike ly/Ly i ny/Ny.
 * </p>
 * <p>
 * <b>Napomena</b> - implementacija algoritma se u nekim aspektima razlikuje od one iz ArXiv dokumenta - videti kod za detalje
 * </p>
 * <br>
 * <p><i>
 * This class implements the stemmer created in the Master's degree thesis of Nikola Milošević, described in the ArXiv paper:
 * <br>
 * Stemmer for Serbian language, Nikola Milošević, arXiv preprint arXiv:1209.4471 (2012).
 * <br>
 * <a href="http://arxiv.org/abs/1209.4471">http://arxiv.org/abs/1209.4471</a>
 * </i></p>
 * <p><i>
 * This stemmer uses a slightly modified version of the so-called <b>dual1</b> coding system in which all Cyrillic letters are transformed into their Latin equivalents and every Latin letter that contains diacritical marks - š, đ, č, ć, ž, dž -
 * is written as a set of two Latin letters without the diacritical marks. In contrast to the stemmers of Kešelj and Šipka, the letters lj/Lj and nj/Nj are <b>NOT</b> transformed into the ly/Ly and ny/Ny forms here.
 * </i></p>
 * <p><i>
 * <b>Note</b> - the algorithm implementation differs in certain aspects from the one in the ArXiv document - see the code for details
 * </i></p>
 * 
 * @author Vuk Batanović 
 * <br>
 * @see <i>Reliable Baselines for Sentiment Analysis in Resource-Limited Languages: The Serbian Movie Review Dataset</i>, Vuk Batanović, Boško Nikolić, Milan Milosavljević, in Proceedings of the 10th International Conference on Language Resources and Evaluation (LREC 2016), pp. 2688-2696, Portorož, Slovenia (2016).
 * <br>
 * https://github.com/vukbatanovic/SCStemmers
 * <br>
 */
public class MilosevicStemmer extends SerbianStemmer {

	private static final long serialVersionUID = 7446909123310468956L;
	/**
	 * Rečnik koji se koristi za normalizovanje često korišćenih nepravilnih glagola
	 * <p>
	 * <i>A dictionary which is used to normalize the frequently used irregular verbs</i>
	 */
	private HashMap<String, String> dictionary;
	
	/**
	 * U ovoj implementaciji je donekle izmenjen originalni Miloševićev algoritam, tako da više liči na stemere Kešelja i Šipke. Uzrok toga je što su uočena dva problema:
	 * <ol>
	 * <li>Originalni algoritam je sporiji od stemera Kešelja i Šipke jer iterira kroz sva pravila za svaku reč umesto da pronađe ono pravilo koje zahvata maksimalan deo sufiksa.</li>
	 * <li>Pošto originalni algoritam uzima u obzir redosled kojim su pravila uneta u sistem, neka složenija/duža pravila postaju beskorisna jer se do njih nikako
	 *	   ne može doći zbog kraćih pravila ispred njih. Npr. kod reči koje se završavaju na 'ene' ili 'une' odseći će se samo 'ne' jer to pravilo
	 *     u redosledu pravila dolazi pre navedena duža dva (ovo ponašanje bi bilo ispravno samo kod reči koje bi odsecanjem dužih sufiksa bile svedene na 1 slovo).
	 *     Međutim, kako ovakvih primera ima relativno malo, izlaz ove modifikovane verzije Miloševićevog algoritma se retko gde razlikuje od izlaza originala.</li>
	 * </ol>
	 * U dnu funkcije je zakomentarisan originalni algoritam.
	 * <br>
	 * <p>
	 * <i>In this implementation the original Milošević's algorithm is somewhat altered, so it looks more like the stemmers of Kešelj and Šipka. The reason for this is that two issues were detected:</i>
	 * <ol>
	 * <li><i>The original algorithm is slower than the stemmers of Kešelj and Šipka since it iterates through all the rules for each word instead of finding the rule which covers the maximal part of the suffix.</i></li>
	 * <li><i>Since the original algorithm takes into account the ordering in which the rules were entered into the system, some rules which are longer/more complex become useless, as it becomes
	 *     impossible to reach them due to the shorter rules before them. For instance, in words ending with 'ene' or 'une' only 'ne' will be removed since that rule's place
	 *     in the rule ordering is before the aforementioned longer two (this behavior would be correct only for words which would be reduced to 1 letter if the longer suffixes were to be removed).
	 *     However, since there are relatively few such examples, the output of this modified version of Milošević's algorithm rarely differs from the output of the original.</i></li>
	 * </ol>
	 * <i>The original algorithm is commented out at the bottom of the function.</i>
	 */
	public String stemDual1Word (String word) {
		/* Proverava da li se reč nalazi u rečniku - ako se nalazi, vraća njen izvorni oblik
		 * Checks if the word can be found in the dictionary - if so, it returns its original form
		 */
		if (dictionary.containsKey(word.toLowerCase()))
			/* Očuvava kapitalizaciju na početku rečenice
			 * Preserves the capitalization at the start of a sentence
			 */
			if (Character.isUpperCase(word.codePointAt(0))) {
				String ret = dictionary.get(word.toLowerCase());
				char first = Character.toUpperCase(ret.charAt(0));
				ret = Character.toString(first) + ret.substring(1);
				return ret;
			}
			else
				return dictionary.get(word);

		String s = word.toLowerCase();
		int minWordLength;
		if (word.matches("\\b(cx|cy|zx|dx|dy|sx|Cx|Cy|Zx|Dx|Dy|Sx)\\w*"))
			minWordLength = 3;
		else
			minWordLength = 2;
		while (s.length() > maxSuffixLen || (s.length() > 0 && (word.length() - s.length() < minWordLength)) || (!rules.containsKey(s) && !s.equals("")))
			s = s.substring(1);
		if (s.equals(""))
			return word;
		return word.substring(0, word.length() - s.length()) + rules.get(s);

		/* Originalni algoritam
		 * The original algorithm
		 
		for (String key: rules.keySet()) {
			String pattern;
			if (word.matches("\\b(cx|cy|zx|dx|dy|sx|Cx|Cy|Zx|Dx|Dy|Sx)\\w*"))
				pattern = "(\\w{3,})" + key + "\\b";
			else
				pattern = "(\\w{2,})" + key + "\\b";
			
			if (word.matches(pattern)) {
				String ret = word.substring(0, word.length() - key.length());
				ret += rules.get(key);
				return ret;
			}
		}
		
		return word;
		*/
	}
	
	public String stemDual1Line (String line) {
		String [] words = line.split("\\b");
		StringBuffer sb = new StringBuffer ();
		for (String word: words)
			sb.append(stemDual1Word(word));
		return sb.toString().trim();
	}
	
	/**
	 * Konvertuje jedan karakter iz standardnog oblika (ćirilice ili latinice) u dual1 kodiranje. Miloševićev stemer ne implementira pun dual1 sistem kodiranja - izostavlja konvertovanje 'lj' i 'nj' u 'ly' i 'ny', pa je ova funkcija blago drugačija u odnosu na onu korišćenu kod stemera Kešelja i Šipke.
	 * <p>
	 * <i>Converts a given character from the standard form (in the Cyrillic or Latin script) to the dual1 coding system. Milošević's stemmer does not implement the full dual1 coding system - it dispenses with the conversion of 'lj' and 'nj' into 'ly' and 'ny', making this function slighty different to the one used for the stemmers of Kešelj and Šipka</i>
	 * @param intCharacter Unicode kod karaktera koji treba prevesti u dual1 sistem
	 * <br> <i>Unicode code point of the character that should be translated into the dual1 system</i>
	 * @param oldChar Karakter koji je u tekstu prethodio trenutno zadatom karakteru
	 * <br> <i>The character which preceded the currently given one within the text</i>
	 * @return String koji sadrži dual1 reprezentaciju zadatog karaktera
	 * <br><i> A string which contains the dual1 representation of the given character</i>
	 */
	protected String convertToDual1Character (int intCharacter, char oldChar)
    {
        char ch = (char) intCharacter;
        
        /* 
         * Ako karakter nije slovo -> prepisuje se
         * 
         * If the character is not a letter -> it is copied
         */
        if (!Character.isLetter(intCharacter))
            return Character.toString(ch);

        /* 
         * Ako se radi o karakteru koje spada u engleski alfabet i nije 'j'/'J', prepisuje se
         * 
         * If it's a character from the English alphabet except for 'j'/'J', it is copied
         */
        else if (Character.toString(ch).matches("\\w") && ch != 'j' && ch != 'J') 
            return Character.toString(ch);

        /* 
         * Ako se radi o karakteru 'j'/'J', onda je možda u pitanju deo slova Dj/dj
         * 
         * If it's the 'j'/'J' character, then it might be a part of the letter Dj/dj
         */
        else if (ch == 'j' || ch == 'J') 
        {
            if (oldChar != 'd' && oldChar != 'D')
                return Character.toString(ch);
            else return "y";
        }

        /*
         * Ovde se radi obrada dijakritičkih oznaka i ćiriličnih slova
         * 
         * This is where the processing of diacritical marks and Cyrillic letters is performed
         */       
        else switch (ch)
        {
	        /*
	         * Latinična slova sa dijakritičkim oznakama
	         * 
	         * Latin characters with diacritical marks
	         */
            case 'ć': return "cy";
            case 'č': return "cx";
            case 'š': return "sx";
            case 'ž':   /*
				 		 * Provera da li se radi o slovu dž
				 		 * 
				 		 * Checks whether it's the letter dž 
				 		 */ 
                if (oldChar == 'd' || oldChar == 'D')
                    return "x";
                else
                    return "zx";
            case 'đ': return "dy";
            case 'Š': return "Sx";
            case 'Ž': return "Zx";
            case 'Đ' : return "Dy";
            case 'Ć': return "Cy";
            case 'Č': return "Cx";

            /*
             *  Ćirilica
             *  
             *  Cyrillic letters
             */ 
            case 'а': return "a";
            case 'А': return "A";
            case 'б': return "b";
            case 'Б': return "B";
            case 'в': return "v";
            case 'В': return "V";
            case 'г': return "g";
            case 'Г': return "G";
            case 'д': return "d";
            case 'Д': return "D";
            case 'ђ': return "dy";
            case 'Ђ': return "Dy";
            case 'е': return "e"; 
            case 'Е': return "E";
            case 'ж': return "zx";
            case 'Ж': return "Zx";   
            case 'з': return "z";
            case 'З': return "Z";
            case 'и': return "i";
            case 'И': return "I";    
            case 'ј': return "j";    
            case 'Ј': return "J";    
            case 'к': return "k";    
            case 'К': return "K";    
            case 'л': return "l";   
            case 'Л': return "L";    
            case 'љ': return "ly";
            case 'Љ': return "Ly";    
            case 'м': return "m";    
            case 'М': return "M";    
            case 'н': return "n";    
            case 'Н': return "N";    
            case 'њ': return "ny";    
            case 'Њ': return "Ny";
            case 'о': return "o";    
            case 'О': return "O";    
            case 'п': return "p";    
            case 'П': return "P";    
            case 'р': return "r";
            case 'Р': return "R";    
            case 'с': return "s";    
            case 'С': return "S";    
            case 'т': return "t";    
            case 'Т': return "T";    
            case 'ћ': return "cy";
            case 'Ћ': return "Cy";    
            case 'у': return "u";    
            case 'У': return "U";    
            case 'ф': return "f";    
            case 'Ф': return "F";    
            case 'х': return "h";
            case 'Х': return "H"; 
            case 'ц': return "c";
            case 'Ц': return "C";     
            case 'ч': return "cx";                
            case 'Ч': return "Cx";                         
            case 'џ': return "dx";                         
            case 'Џ': return "Dx";                         
            case 'ш': return "sx";                         
            case 'Ш': return "Sy";   
            
        }
     return "";   
}
	
	/**
	 * Milošević: Currently 285 rules
	 */
	protected void initRules () {
		super.initRules();

		// RULES
		rules.put("ovnicxki", "");
		rules.put("ovnicxka", "");
		rules.put("ovnika", "");
		rules.put("ovniku", "");
		rules.put("ovnicxe", "");
		rules.put("kujemo", "");
		rules.put("ovacyu", "");
		rules.put("ivacyu", "");
		rules.put("isacyu", "");
		rules.put("dosmo", "");
		rules.put("ujemo", "");
		rules.put("ijemo", "");
		rules.put("ovski", "");
		rules.put("ajucxi", "");
		rules.put("icizma", "");
		rules.put("ovima", "");
		rules.put("ovnik", "");
		rules.put("ognu", "");
		rules.put("inju", "");
		rules.put("enju", "");
		rules.put("cxicyu", "");
		rules.put("sxtva", "");
		rules.put("ivao", "");
		rules.put("ivala", "");
		rules.put("ivalo", "");
		rules.put("skog", "");
		rules.put("ucxit", "");
		rules.put("ujesx", "");
		rules.put("ucyesx", "");
		rules.put("ocyesx", "");
		rules.put("osmo", "");
		rules.put("ovao", "");
		rules.put("ovala", "");
		rules.put("ovali", "");
		rules.put("ismo", "");
		rules.put("ujem", "");
		rules.put("esmo", "");
		rules.put("asmo", "");		// Milošević: pravi grešku kod pevasmo
		rules.put("zxemo", "");
		rules.put("cyemo", "");
		rules.put("cyemo", "");
		rules.put("bemo", "");
		rules.put("ovan", "");
		rules.put("ivan", "");
		rules.put("isan", "");
		rules.put("uvsxi", "");
		rules.put("ivsxi", "");
		rules.put("evsxi", "");
		rules.put("avsxi", "");
		rules.put("sxucyi", "");
		rules.put("uste", "");
		rules.put("icxe", "i");		// Milošević: bilo ik
		rules.put("acxe", "ak");
		rules.put("uzxe", "ug");
		rules.put("azxe", "ag");	// Milošević: možda treba az, pokazati, pokazxe
		rules.put("aci", "ak");
		rules.put("oste", "");
		rules.put("aca", "");
		rules.put("enu", "");
		rules.put("enom", "");
		rules.put("enima", "");
		rules.put("eta", "");
		rules.put("etu", "");
		rules.put("etom", "");
		rules.put("adi", "");
		rules.put("alja", "");
		rules.put("nju", "nj");
		rules.put("lju", "");
		rules.put("lja", "");
		rules.put("lji", "");
		rules.put("lje", "");
		rules.put("ljom", "");
		rules.put("ljama", "");
		rules.put("zi", "g");
		rules.put("etima", "");
		rules.put("ac", "");
		rules.put("becyi", "beg");
		rules.put("nem", "");
		rules.put("nesx", "");
		rules.put("ne", "");
		rules.put("nemo", "");
		rules.put("nimo", "");
		rules.put("nite", "");
		rules.put("nete", "");
		rules.put("nu", "");
		rules.put("ce", "");
		rules.put("ci", "");
		rules.put("cu", "");
		rules.put("ca", "");
		rules.put("cem", "");
		rules.put("cima", "");
		rules.put("sxcyu", "s");
		rules.put("ara", "r");
		rules.put("iste", "");
		rules.put("este", "");
		rules.put("aste", "");
		rules.put("ujte", "");
		rules.put("jete", "");
		rules.put("jemo", "");
		rules.put("jem", "");
		rules.put("jesx", "");
		rules.put("ijte", "");
		rules.put("inje", "");
		rules.put("anje", "");
		rules.put("acxki", "");
		rules.put("anje", "");
		rules.put("inja", "");
		rules.put("cima", "");
		rules.put("alja", "");
		rules.put("etu", "");
		rules.put("nog", "");
		rules.put("omu", "");
		rules.put("emu", "");
		rules.put("uju", "");
		rules.put("iju", "");
		rules.put("sko", "");
		rules.put("eju", "");
		rules.put("ahu", "");
		rules.put("ucyu", "");
		rules.put("icyu", "");
		rules.put("ecyu", "");
		rules.put("acyu", "");
		rules.put("ocu", "");
		rules.put("izi", "ig");
		rules.put("ici", "ik");
		rules.put("tko", "d");
		rules.put("tka", "d");
		rules.put("ast", "");
		rules.put("tit", "");
		rules.put("nusx", "");
		rules.put("cyesx", "");
		rules.put("cxno", "");
		rules.put("cxni", "");
		rules.put("cxna", "");
		rules.put("uto", "");
		rules.put("oro", "");
		rules.put("eno", "");
		rules.put("ano", "");
		rules.put("umo", "");
		rules.put("smo", "");
		rules.put("imo", "");
		rules.put("emo", "");
		rules.put("ulo", "");
		rules.put("sxlo", "");
		rules.put("slo", "");
		rules.put("ila", "");
		rules.put("ilo", "");
		rules.put("ski", "");
		rules.put("ska", "");
		rules.put("elo", "");
		rules.put("njo", "");
		rules.put("ovi", "");
		rules.put("evi", "");
		rules.put("uti", "");
		rules.put("iti", "");
		rules.put("eti", "");
		rules.put("ati", "");
		rules.put("vsxi", "");
		rules.put("vsxi", "");
		rules.put("ili", "");
		rules.put("eli", "");
		rules.put("ali", "");
		rules.put("uji", "");
		rules.put("nji", "");
		rules.put("ucyi", "");
		rules.put("sxcyi", "");
		rules.put("ecyi", "");
		rules.put("ucxi", "");
		rules.put("oci", "");
		rules.put("ove", "");
		rules.put("eve", "");
		rules.put("ute", "");
		rules.put("ste", "");
		rules.put("nte", "");
		rules.put("kte", "");
		rules.put("jte", "");
		rules.put("ite", "");
		rules.put("ete", "");
		rules.put("cyi", "");
		rules.put("usxe", "");
		rules.put("esxe", "");
		rules.put("asxe", "");
		rules.put("une", "");
		rules.put("ene", "");
		rules.put("ule", "");
		rules.put("ile", "");
		rules.put("ele", "");
		rules.put("ale", "");
		rules.put("uke", "");
		rules.put("tke", "");
		rules.put("ske", "");
		rules.put("uje", "");
		rules.put("tje", "");
		rules.put("ucye", "");
		rules.put("sxcye", "");
		rules.put("icye", "");
		rules.put("ecye", "");
		rules.put("ucxe", "");
		rules.put("oce", "");
		rules.put("ova", "");
		rules.put("eva", "");
		rules.put("ava", "av");
		rules.put("uta", "");
		rules.put("ata", "");
		rules.put("ena", "");
		rules.put("ima", "");
		rules.put("ama", "");
		rules.put("ela", "");
		rules.put("ala", "");
		rules.put("aka", "");
		rules.put("aja", "");
		rules.put("jmo", "");
			//"uga", "");			// Milošević
		rules.put("oga", "");
		rules.put("ega", "");
		rules.put("acya", "");		/* Batanović: ispravio sa 'aća' na 'acya'
									 *			  corrected from 'aća' to 'acya'
									 */
		rules.put("oca", "");
		rules.put("aba", "");
		rules.put("cxki", "");
		rules.put("ju", "");
		rules.put("hu", "");
		rules.put("cyu", "");
		rules.put("cu", "");
		rules.put("ut", "");
		rules.put("it", "");
		rules.put("et", "");
		rules.put("at", "");
		rules.put("usx", "");
		rules.put("isx", "");
		rules.put("esx", "");
		rules.put("esx", "");
		rules.put("uo", "");
		rules.put("no", "");
		rules.put("mo", "");
		rules.put("mo", "");
		rules.put("lo", "");
		rules.put("ko", "");
		rules.put("io", "");
		rules.put("eo", "");
		rules.put("ao", "");
		rules.put("un", "");
		rules.put("an", "");
		rules.put("om", "");
		rules.put("ni", "");
		rules.put("im", "");
		rules.put("em", "");
		rules.put("uk", "");
		rules.put("uj", "");
		rules.put("oj", "");
		rules.put("li", "");
		rules.put("ci", "");
		rules.put("uh", "");
		rules.put("oh", "");
		rules.put("ih", "");
		rules.put("eh", "");
		rules.put("ah", "");
		rules.put("og", "");
		rules.put("eg", "");
		rules.put("te", "");
		rules.put("sxe", "");
		rules.put("le", "");
		rules.put("ke", "");
		rules.put("ko", "");
		rules.put("ka", "");
		rules.put("ti", "");
		rules.put("he", "");
		rules.put("cye", "");
		rules.put("cxe", "");
		rules.put("ad", "");
		rules.put("ecy", "");
		rules.put("ac", "");
		rules.put("na", "");
		rules.put("ma", "");
		rules.put("ul", "");
		rules.put("ku", "");
		rules.put("la", "");
		rules.put("nj", "nj");
		rules.put("lj", "lj");
		rules.put("ha", "");
		rules.put("a", "");
		rules.put("e", "");
		rules.put("u", "");
		rules.put("sx", "");
		rules.put("o", "");
		rules.put("i", "");
			//"k", "");				// Milošević
		rules.put("j", "");
			//"t", "");				// Milošević
			//"n", ""); 			// Milošević: London, londona
		rules.put("i", "");
	
		
		// DICTIONARY
		dictionary = new HashMap<String, String> (200);
		/* Glagol 'biti'
		 * The verb 'to be'
		 */
		dictionary.put("bih", "biti");
		dictionary.put("bi", "biti");
		dictionary.put("bismo", "biti");
		dictionary.put("biste", "biti");
		dictionary.put("bisxe", "biti");
		dictionary.put("budem", "biti");
		dictionary.put("budesx", "biti");
		dictionary.put("bude", "biti");
		dictionary.put("budemo", "biti");
		dictionary.put("budete", "biti");
		dictionary.put("budu", "biti");
		dictionary.put("bio", "biti");
		dictionary.put("bila", "biti");
		dictionary.put("bili", "biti");
		dictionary.put("bile", "biti");
		dictionary.put("biti", "biti");
		dictionary.put("bijah", "biti");
		dictionary.put("bijasxe", "biti");
		dictionary.put("bijasmo", "biti");
		dictionary.put("bijaste", "biti");
		dictionary.put("bijahu", "biti");
		dictionary.put("besxe", "biti");
		// Glagol 'jesam'
		dictionary.put("sam", "jesam");
		dictionary.put("si", "jesam");
		dictionary.put("je", "jesam");
		dictionary.put("smo", "jesam");
		dictionary.put("ste", "jesam");
		dictionary.put("su", "jesam");
		dictionary.put("jesam", "jesam");
		dictionary.put("jesi", "jesam");
		dictionary.put("jeste", "jesam");
		dictionary.put("jesmo", "jesam");
		dictionary.put("jeste", "jesam");
		dictionary.put("jesu", "jesam");
		/* Glagol 'hteti'
		 * The verb 'to want'
		 */
		dictionary.put("cyu", "hteti");
		dictionary.put("cyesx", "hteti");
		dictionary.put("cye", "hteti");
		dictionary.put("cyemo", "hteti");
		dictionary.put("cyete", "hteti");
		dictionary.put("hocyu", "hteti");
		dictionary.put("hocyesx", "hteti");
		dictionary.put("hocye", "hteti");
		dictionary.put("hocyemo", "hteti");
		dictionary.put("hocyete", "hteti");
		dictionary.put("hocye", "hteti");
		dictionary.put("hteo", "hteti");
		dictionary.put("htela", "hteti");
		dictionary.put("hteli", "hteti");
		dictionary.put("htelo", "hteti");
		dictionary.put("htele", "hteti");
		dictionary.put("htedoh", "hteti");
		dictionary.put("htede", "hteti");
		dictionary.put("htede", "hteti");
		dictionary.put("htedosmo", "hteti");
		dictionary.put("htedoste", "hteti");
		dictionary.put("htedosxe", "hteti");
		dictionary.put("hteh", "hteti");
		dictionary.put("hteti", "hteti");
		dictionary.put("htejucyi", "hteti");
		dictionary.put("htevsxi", "hteti");
		/* Glagol 'moći'
		 * The verb 'can'
		 */
		dictionary.put("mogu", "mocyi");	/* Batanović: na nekoliko mesta slova sa dijakritičkim oznakama su konvertovana u dual1 kodiranje
		 									 * 			  in several places letters with diacritical marks were converted into the dual1 coding system
		 									 */
		dictionary.put("mozxesx", "mocyi");
		dictionary.put("mozxe", "mocyi");
		dictionary.put("mozxemo", "mocyi");
		dictionary.put("mozxete", "mocyi");
		dictionary.put("mogao", "mocyi");
		dictionary.put("mogli", "mocyi");
		dictionary.put("mocyi", "mocyi");
		
		/* Ovi oblici se ne javljaju u ArXiv radu. Oni su preuzeti iz koda Miloševićevog ličnog veb sajta (www.inspiratron.org)
		 * These forms do not appear in the ArXiv paper. They were taken from the code of Milošević's personal website (www.inspiratron.org)
		 */
		dictionary.put("htecxu", "hteti");
		dictionary.put("htecxesx", "hteti");
		dictionary.put("htecye", "hteti");
		dictionary.put("necyu", "NE_hteti");
		dictionary.put("necyesx", "NE_hteti");
		dictionary.put("necye", "NE_hteti");
		dictionary.put("necyemo", "NE_hteti");
		dictionary.put("necyete", "NE_hteti");
		dictionary.put("necyesx", "NE_hteti");
		dictionary.put("nisam", "NE_jesam");
		dictionary.put("nisi", "NE_jesam");
		dictionary.put("nije", "NE_jesam");
		dictionary.put("nismo", "NE_jesam");
		dictionary.put("niste", "NE_jesam");
		dictionary.put("nisu", "NE_jesam");
	}
}