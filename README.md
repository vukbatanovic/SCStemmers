# SCStemmers - a collection of stemmers for Serbian and Croatian
This package is a Java reimplementation of four previously published stemming algorithms for Serbian and Croatian:
* The greedy and the optimal subsumption-based stemmer for Serbian, by Vlado Kešelj and Danko Šipka ([originally written in Perl](http://www.cs.dal.ca/~vlado/nlp/2007-sr/))
* A refinement of the greedy subsumption-based stemmer, by Nikola Milošević ([originally written in PHP](http://arxiv.org/abs/1209.4471), [later also made available in Python](https://nikolamilosevic86.github.io/SerbianStemmer/))
* A "Simple stemmer for Croatian v0.1", by Nikola Ljubešić and Ivan Pandžić ([originally written in Python](http://nlp.ffzg.hr/resources/tools/stemmer-for-croatian/))

## Text Encoding
All stemmers expect the input text to be formatted in UTF-8. Their outputs are also UTF-8 encoded.

Since Serbian is a digraphic language the input texts can be in either the Cyrillic or the Latin script. All stemmers produce output in the Latin script.

### Dual1 Coding System
The stemmers for Serbian internally use the so-called *dual1* coding system in which only the Latin script characters without diacritical marks are allowed.
To obtain dual1-coded texts all Cyrillic characters are first translated into their Latin script equivalents. Afterwards, all characters with diacritical marks are replaced in the following manner:
* **Č/č** is coded as **Cx/cx**
* **Ć/ć** is coded as **Cy/cy**
* **Dž/dž** is coded as **Dx/dx**
* **Đ/đ** is coded as **Dy/dy**
* **Ž/ž** is coded as **Zx/zx**
* **Š/š** is coded as **Sx/sx**

The greedy and the optimal stemmers of Kešelj and Šipka (but not Milošević's refinement of the greedy stemmer) also apply the following:
* **Lj/lj** is coded as **Ly/ly**
* **Nj/nj** is coded as **Ny/ny**

The stemmers for Serbian also accept texts in the dual1 coding as input, but will still produce the normal Latin script text as output.
However, this behavior can easily be changed by applying the coding transformation methods, supplied within the *SerbianStemmer* class, to the output text.

## Usage
All stemmers can be used in a program through the interface declared in the *SCStemmer* abstract class, via the methods:
```
public String stemWord (String word)
public String stemLine (String line)
public String stemText (String text)
public void stemFile (String fileInput, String fileOutput)
```

### Command-line interface
The supplied [SCStemmers.jar](https://github.com/vukbatanovic/SCStemmers/releases/download/v1.1.0/SCStemmers.jar) file makes it possible to stem the contents of textual files using the command line. Stemmers from the SCStemmers package can be invoked by the following command:
```
java -jar SCStemmers.jar StemmerID InputFile OutputFile
```
where *StemmerID* is a number identifying the stemming algorithm:
* 1 - Kešelj & Šipka - Greedy
* 2 - Kešelj & Šipka - Optimal
* 3 - Milošević
* 4 - Ljubešić & Pandžić

*InputFile* is the path of the TXT file encoded in UTF-8 that is to be stemmed. The stemmed text will be placed in the file determined by the *OutputFile* argument.

### Weka
Alternatively, the stemmers can be utilized as an unofficial plug-in module within Weka (Waikato Environment for Knowledge Analysis).
To do so, download the [SCStemmers Weka package](https://github.com/vukbatanovic/SCStemmers/releases/download/v1.1.0/SCStemmers_1.1.0.zip).
Open the Weka package manager (available in Weka >= 3.7) and use the "Unofficial - File/URL" option to select and install SCStemmers.
After restarting Weka, the list of available stemmers (within the StringToWordVector filter) will also contain the four stemmers from this package.

## References
If you wish to use this package in your paper or project, please include a reference to the following paper in which it was presented:

**[Reliable Baselines for Sentiment Analysis in Resource-Limited Languages: The Serbian Movie Review Dataset](http://www.lrec-conf.org/proceedings/lrec2016/pdf/284_Paper.pdf)**, Vuk Batanović, Boško Nikolić, Milan Milosavljević, in Proceedings of the 10th International Conference on Language Resources and Evaluation (LREC 2016), pp. 2688-2696, Portorož, Slovenia (2016).

Be sure to also cite the original paper of each stemmer you use:
* For the greedy and the optimal subsumption-based stemmer for Serbian: *[A Suffix Subsumption-Based Approach to Building Stemmers and Lemmatizers for Highly Inflectional Languages with Sparse Resources](http://infoteka.bg.ac.rs/pdf/Eng/2008/INFOTHECA_IX_1-2_May2008_23a-33a.pdf)*, Vlado Kešelj, Danko Šipka, Infotheca 9(1-2), pp. 23a-33a (2008).
* For the refinement of the greedy subsumption-based stemmer: *[Stemmer for Serbian language](http://arxiv.org/abs/1209.4471)*, Nikola Milošević, arXiv preprint arXiv:1209.4471 (2012).
* For the "Simple stemmer for Croatian v0.1": *[Retrieving Information in Croatian: Building a Simple and Efficient Rule-Based Stemmer](http://nlp.ffzg.hr/data/publications/nljubesi/ljubesic07-retrieving.pdf)*, Nikola Ljubešić, Damir Boras, Ozren Kubelka, Digital Information and Heritage, pp. 313–320 (2007).

## Additional Documentation
All classes and non-trivial methods contain extensive documentation and comments, in both Serbian and English.
If you have any questions about the stemmers' functioning, please review the supplied javadoc documentation, the source code, and the papers listed above.
If no answer can be found, feel free to contact me at: vuk.batanovic / at / student.etf.bg.ac.rs

## License
GNU General Public License 3.0 (GNU GPL 3.0)
