package weka.core.stemmers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * <p>
 * Ova apstraktna klasa implementira zajedničke funkcije za stemere za srpski opisane u radu:
 * <br>
 * Pristup izgradnji stemera i lematizora za jezike s bogatom fleksijom i oskudnim resursima zasnovan na obuhvatanju sufiksa, Vlado Kešelj, Danko Šipka, Infoteka 9(1-2), 21-31 (2008).
 * <br> 
 * <a href="http://infoteka.bg.ac.rs/pdf/Srp/2008/04%20Vlado-Danko_Stemeri.pdf">http://infoteka.bg.ac.rs/pdf/Srp/2008/04%20Vlado-Danko_Stemeri.pdf</a>
 * <br>
 * (originalna implementacija u Perlu i drugi resursi su dostupni na: <a href="http://www.cs.dal.ca/~vlado/nlp/2007-sr/">http://www.cs.dal.ca/~vlado/nlp/2007-sr/</a>)
 * </p>
 * <p>
 * Ovi stemeri koriste tzv. <b>dual1</b> kodovanje u kome se sva ćirilična slova prevode u latinična a svako latinično slovo koje sadrži dijakritičke oznake - š, đ, č, ć, ž, dž -
 * se piše kao skup dva latinična slova bez dijakritičkih oznaka. Pored toga, slova lj/Lj i nj/Nj se prevode u oblike ly/Ly i ny/Ny.
 * </p>
 * <p>
 * <i>This abstract class implements the common functions of the stemmers for Serbian described in the paper:</i>
 * <br>
 * <i>A Suffix Subsumption-Based Approach to Building Stemmers and Lemmatizers for Highly Inflectional Languages with Sparse Resources, Vlado Kešelj, Danko Šipka, Infotheca 9(1-2), 23a-33a (2008).</i>
 * <br>
 * <i><a href="http://infoteka.bg.ac.rs/pdf/Eng/2008/INFOTHECA_IX_1-2_May2008_23a-33a.pdf">http://infoteka.bg.ac.rs/pdf/Eng/2008/INFOTHECA_IX_1-2_May2008_23a-33a.pdf</a></i>
 * <br>
 * <i>(the original implementation in Perl and other resources are available at: <a href="http://www.cs.dal.ca/~vlado/nlp/2007-sr/">http://www.cs.dal.ca/~vlado/nlp/2007-sr/</a>)</i>
 * </p>
 * <p>
 * <i>These stemmers use the so-called <b>dual1</b> coding system in which all Cyrillic letters are transformed into their Latin equivalents and every Latin letter that contains diacritical marks - š, đ, č, ć, ž, dž -
 * is written as a set of two Latin letters without the diacritical marks. Furthermore, the letters lj/Lj and nj/Nj are transformed into the ly/Ly and ny/Ny forms.</i>
 * </p>
 * @author Vuk Batanović
 * <br>
 * @see <i>Reliable Baselines for Sentiment Analysis in Resource-Limited Languages: The Serbian Movie Review Dataset</i>, Vuk Batanović, Boško Nikolić, Milan Milosavljević, in Proceedings of the 10th International Conference on Language Resources and Evaluation (LREC 2016), pp. 2688-2696, Portorož, Slovenia (2016).
 * <br>
 * https://github.com/vukbatanovic/SCStemmers
 * <br>
 */
public abstract class KeseljSipkaStemmer extends SerbianStemmer  {
	
	private static final long serialVersionUID = 5427699406177734026L;

	public String stemDual1Word (String word) {
		if (word.length() <= 3)
			return word;
		String s = word.toLowerCase();
		while (s.length() > maxSuffixLen || (!rules.containsKey(s) && !s.equals("")))
			s = s.substring(1);
		if (s.equals(""))
			return word;
		return word.substring(0, word.length() - rules.get(s).length());
	}
	
	public String stemDual1Line (String line) {
	/* 
	 * Stemuje reči duže od 3 slova a kraće od 31, kao u originalnoj implementaciji
	 *
	 * Stems words longer than 3 letters and not shorter than 31, per the original implementation
	 */
		Matcher matcher = Pattern.compile("(\\b[a-zA-Z-]{4,30}\\b)").matcher(line);
		StringBuffer sb = new StringBuffer ();
		boolean work = matcher.find();
		int start = 0;
		while (work) {
			sb.append(line.substring(start, matcher.start()));
			sb.append(stemDual1Word(line.substring(matcher.start(), matcher.end())));
			start = matcher.end();
			work = matcher.find();
		}
		sb.append(line.substring(start));
		return sb.toString();
	}
	
	protected String convertToDual1Character (int intCharacter, char oldChar) {
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
         * Ako se radi o karakteru 'j'/'J', onda je možda u pitanju deo slova Lj/Nj/Dj/lj/nj/dj
         * 
         * If it's the 'j'/'J' character, then it might be a part of the letter Lj/Nj/Dj/lj/nj/dj
         */
        else if (ch == 'j' || ch == 'J') {
            if (oldChar != 'l' && oldChar != 'L' && oldChar != 'n' && oldChar != 'N' && oldChar != 'd' && oldChar != 'D')
                return Character.toString(ch);
            else return "y";
        }

        /*
         * Ovde se radi obrada dijakritičkih oznaka i ćiriličnih slova
         * 
         * This is where the processing of diacritical marks and Cyrillic letters is performed
         */
        else switch (ch) {
	        /*
	         * Latinična slova sa dijakritičkim oznakama
	         * 
	         * Latin characters with diacritical marks
	         */
            case 'ć': return "cy";
            case 'č': return "cx";
            case 'š': return "sx";
            case 'ž':  	/*
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
}