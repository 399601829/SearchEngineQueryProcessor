# SearchEngineQueryProcessor
Returns Top 10 URL results and corresponding ranking scores for user entered search query.Implements conjunctive as well as disjunctive query processing and uses BM25 as the ranking function.Very Efficient, returns results for conjunctive queries in less than 50ms.


<b>How to run Query Processor on mac :</b>

Go to the root of source code directory:

java –Xms6000m -cp .:out/artifacts/QuerySearcher_jar/QuerySearcher.jar QuerySearch

where –Xms6000m allocates a heap memory of 6GB at startup to the JVM.

External Libraries used:
•	JavaFastPFOR Integer Compression 
•	BM25

<b>How to run Index Generator on mac :</b>

Go to the root of source code directory:

java –Xms5000m -cp .:out/artifacts/IndexBuilder_jar/IndexBuilder.jar Indexer 2000000000

where 2000000000 is the number of bytes of total memory to be used by sort and merge which is equivalent to 2GB,
and –Xms5000m allocates a heap memory of 5GB at startup to the JVM and hence the program.

External Libraries used:
•	Apache commons IO
•	WARC ArchiveReader


<b>Broad Features of the program:</b>

Query Processor:

•	Returns Top 10 URL results and corresponding ranking scores for user entered search query.
•	Implements conjunctive as well as disjunctive query processing.
•	Uses BM25 as the ranking function.
•	Very Efficient, returns results for conjunctive queries in less than 50ms.
•	Only Loads Lexicon and URL Table into main memory on startup.
•	Performs seeks on disk in order to read only those inverted lists from disk that correspond to query words.

Index Generator:

•	Generates an Inverted Index in binary format, a Lexicon , and an URL table.
•	Takes gzip compressed wet files as input and only uncompresses the Document in the wet file currently being parsed.
•	Parses each document in wet file , extracts alphanumeric terms, assigns termId in the order the words are encountered , adds the term to a Term To Term Id Mapping.
•	Assigns DocId in the order the Documents are encountered.
•	Appends a row with docId, URL & No. of terms in Page of the visited doc to the URL Table txt file.   
•	Generates Intermediate posting in binary for every Term encountered by writing 4 bytes for TermId & 4 bytes for corresponding DocId. Hence every record is of 8 bytes.
•	Once all the wet files are parsed, it performs I/O efficient Merge Sort on the intermediate postings using C.
•	Also Checks the output of Merge Sort for correctness.
•	Parses the Sorted Intermediate Postings and removes WordId from each posting and adds Term Frequency by implementing a smart O(n) algorithm.
•	The final Inverted Index contains records of 8 byte comprised of 4 Bytes of DocId & next 4 bytes of term Frequency. Subsequently it is compressed to save disk space.
•	Lexicon is also updated to map each Word to start Index, end Index & Document Frequency and written out to disk.
•	Block wise compression applied to inverted index using JavaFastPFOR library which uses coding techniques such as Binary Packing, NewPFD, OptPFD, Variable Byte, Simple 9 etc.

<b>Structure of an Inverted List inside the compressed Index:</b>

Last[],SizeOfDocIds[],SizeOfTermFreqs[],chunk0,chunk1…and so on where:

Last[] : An array containing last docID in each chunk
SizeOfDocIds[] : An array containing size of compressed docId chunks.
SizeOfTermFreqs[]: An array containing size of compressed Term Frequency chunks.
Chunk : 128 postings where docId’s and Term Frequencies are separated out.



<b>Working of the program:</b>

Query Processing

Data Structures Used:

•	Map<String,Long[]> lexicon implemented as a HashMap, Contains mappings of the form :
		Term => (StartIndex, EndIndex, DocumentFrequency)
•	List<PageTable> pageTableList implemented as an ArrayList ,where PageTable is a user defined class and each of its object represents a row in the URL Table.

•	TreeMap<Double,TopResult> top10Results which stores the BM25 score as key and metadata associated with the web page such as url,term frequency etc as value.

•	ListPointer Class containing the following member variables:
public String term;
public int docFreq;
public int[] last;
public RandomAccessFile randomAccessFile;
public int numberOfChunks;
public boolean[] uncompressed;
public int block;
public int[] sizeDocId;
public int[] sizeTf;
public int[] temp;
public int ind = 0;
public long realStartIndex;

and method:
	public void readLastAndSizeArrays(long startIndex)



Methods :

•	public void loadLexiconIntoMemory()
•	public void loadURLTableIntoMemory()
•	public void computeBM25Score(int docId,String[] terms, int[] tf, Integer[] n)
•	public ListPointer openList(String term)
•	public void closeList()
•	public int[] uncompress(ListPointer lp)
•	public int nextGEQ(ListPointer lp,int k)
•	public int getFreq(ListPointer lp)
•	public void printResults()
•	public SortListPointersResult sortListPointersAndRemoveNull(ListPointer[] lp, String[] q, Integer[] docFreq)
•	public int findMaxDocId(ListPointer lp)
•	public void executeQuery(String query)


Internal Working of the program:

•	loadLexiconIntoMemory() reads the lexicon from disk and populates a HashMap with mappings of the form :
	Term => (StartIndex, EndIndex,DocumentFrequency)

•	loadURLTableIntoMemory() reads each row in the URL table into an Array List.

•	readLastAndSizeArrays(long startIndex) in ListPointer Class seeks to the given offset on the compressed inverted index and reads in the Integer arrays last, SizeOfDocIds, SizeOfTermFreqs and stores in the respective member variables. Explanation of some of the member variables: 

boolean[] uncompressed : stores which chunks have been already uncompressed.
	int block: index of current chunk being processed.
long realStartIndex: Starting index (in bytes) of the chunks in the inverted list i.e the end index of the header. Provides convenience in computation.


•	openList(String term) : Initializes a ListPointer object for the given term.

•	closeList() : Clears the top10Results TreeMap before the user enters next search query.


•	nextGEQ(ListPointer lp,int k) finds the next posting in list lp with docID >= k and return its docID. Returns value > MAXDID if none exists.

•	getFreq(ListPointer lp) returns the corresponding frequency of the docID in the chunk inside list lp.
 
•	computeBM25Score(int docId,String[] terms, int[] tf, Integer[] n) computes a score for the Intersecting docId using all the terms in search query by applying the following formula :

 

•	findMaxDocId(ListPointer lp) returns the max DocId in the inverted list i.e the last integer in the Last[] array.

•	executeQuery(String query) splits the user entered search query into words, opens the list for each word & applies Document-at-a-time Query Processing algo.

  
Algorithm for Document-at-a-time Query Processing:

•	Example : open 3 inverted lists for reading using openList() 
•	This returns 3 pointers lp0, lp1, and lp2 to the starts of the lists
•	Call d0 = nextGEQ(lp0, 0) to get docID of first posting in lp0
•	Call d1 = nextGEQ(lp1, d0) to check for matching docID in lp1
•	If (d1 > d0), start again at first list and call d0 = nextGEQ(lp0, d1)
•	If (d1 = d0), call d2 = nextGEQ(lp2, d0) to see if d0 also in lp2
•	If (d2 > d0), start again at first list and call d0 = nextGEQ(lp0, d2)
•	If (d2 = d0), then d0 is in all three lists; compute its score; then continue at first list and call d0 = nextGEQ(lp0, d2+1) 
•	Whenever a score is computed for a docID, check if it should be inserted into TreeMap of current top-10 results; at the end, return results in TreeMap


Index Generation

Data Structures Used:

•	List<PageTable> pageTableList implemented as an ArrayList ,where PageTable is a user defined class and each of its object represents a row in the URL Table:

public class PageTable {
    public Integer docId;
    public String url;
    public Integer numberOfTerms;

    public PageTable(Integer docId, String url,Integer numberOfTerms){
        this.docId = docId;
        this.url = url ;
        this.numberOfTerms = numberOfTerms;
    }

}

•	Map<String,Integer> wordToWordId implemented as a HashMap, contains the Term To TermId mapping

•	Map<Integer,String> wordIdToWord implemented as a HashMap, contains the TermId To Term mapping which is used during reformatting the Intermediate postings to generate the final Inverted Index

•	Map<String,List<Integer>> lexicon implemented as a HashMap,
Contains mappings of the form :
		Term => (StartIndex, EndIndex, DocumentFrequency)

Methods :

•	public void parseWetFile(String wetFilePath) 
•	public void generateIntermediatePosting(int wordId,int docId)
•	public void mergeSort(String memoryLimit)
•	public void generateURLTableFile()
•	public void reformatPostings()
•	public void updateLexicon(int wordId,int startIndex,int endIndex,int docFrequency)
•	public void generateLexiconFile()
•	public void generatePosting(int docId,int termFrequency)


Internal Working of the program:

•	parseWetFile(String wetFilePath) takes one wet file at a time, uncompresses one document at a time and parses it using ArchiveReader Library. Firstly, It Checks whether the document is of mime type text/plain. Then, it reads the uncompressed ArchiveRecord into a byte array. Converts the byte array to string and splits the string using all characters other than (a-z,A-Z,0-9,’) as seperators. This is performed using Regular Expression.

Now, the program iterates over the Terms array and insert each term as key and its termId as the value into the wordToWordId hashmap. In particular, words are assigned wordId’s in the order they are first discovered.
The Hashmap for words, initially contains numw = 0 words and whenever a word is parsed out that has never occurred before, it assigns the word ID numw and increase numw by one.

•	generateIntermediatePosting(int wordId,int docID) takes wordId & docId as input and writes them in binary into the IntermediateInvertedIndex file. Firstly, the 2 integers are inserted into a ByteBuffer and byte ordering is changed to LITTLE_ENDIAN since C program for sorting reads the bytes in LITTLE_ENDIAN unlike Java where the default is BIG_ENDIAN.Then, the byte buffer is converted to a byte array and written to the Intermediate Inverted Index using DataOutputStream Class.

•	addEntryToPageTable(String url,int size) appends a new row to the URLTableFile.

•	I/O efficient merge sort program written in C is run using ProcessBuilder class of Java. Function mergeSort(String memoryLimit) takes in the memoryLimit parameter passed in by the user and passes it to the C Program. It comprises of 3 phases :

o	Sort Phase: Sort Phase reads in memsize no. of bytes from the intermediate Inverted List at a time and subsequently reads 8 bytes at a time which is one record and inserts it into a qsort() which is a modified partion-exchange sort or queuesort.

Qsort uses a custom comparator function which implements the following algo:

if(wordIdLeft == wordIdRight){
    return (docIdLeft - docIdRight);
}
else{
return (wordIdLeft - wordIdRight);
}

For eg. if you have up to 20 MB of main memory available for sorting, running 
       sortphase 8 20000000 data temp list
   will create 40 sorted files of size 20MB each, called temp0, temp1,... temp39. The list of generated files is also written to file "list"

o	Merge Phase: Merge Phase uses a min-heap ,copies the record corresponding to the minimum wordId which is at the root to the output & replaces the minimum in heap by the next record from that file.
For eg. To merge the 40 files in one phase, assuming you have again 20MB of main memory available,
       mergephase 8 20000000 40 list result list2
    will merge the 40 files listed in "list", merge them into one
    result file called "result0", and write the filename "result0" into file
    "list2".

o	Check correctness Phase : Checkoutput compares the 2 consecutive records at a time & return Not Sorted if (wordIdLeft > wordIdRight || (wordIdLeft == wordIdRight && docIdLeft > docIdRight))

•	MergeSort Process generates result0 which is then reformatted to generate the Final Inverted Index. reformatPostings() reads the bytes into a byte buffer, converts to LITTLE_ENDIAN and updates Map<Integer,Integer> 
docIdToTermFrequency implemented as a TreeMap in a single iteration. Hence the custom algorithm works in linear time and updates the Lexicon using docIdToTermFrequency TreeMap.

•	generateLexiconFile() iterates over the keyset of lexicon TreeMap and writes out the Terms in alphabetical order along with their startIndex,EndIndex & DocumentFrequency to Lexicon.txt file.

•	Simplistic Usage of Compression Library:

   	IntegratedIntCompressor iic = new IntegratedIntCompressor();
        int[] data = ... ; // to be compressed
        int[] compressed = iic.compress(data); // compressed array
        int[] recov = iic.uncompress(compressed); // equals to data


<b>Results:</b>

I parsed about 5.7 million pages in 143 minutes = 2.4 hours which give a speed of about 662 pages /second approximately.

The Final Inverted Index was 16 GB and after compressing using differential coding and appending last[],SizeOfDocIds[], SizeOfTermFreqs[] headers to each list it became 11.93 GB.

Query Processor returns top 10 results in under 50 ms for conjunctive Query Processing & in under 400 ms for disjunctive Query Processing.


<b>Design Decisions:</b>

Query Processor:

•	I decided to write 2 size arrays namely SizeOfDocIds[], SizeOfTermFreqs[] as the header in compressed inverted list instead of just one size array since I’ve separated out the docId’s and TermFrequencies inside each chunk rather than letting the postings remain as it is & then compressing because that would have been inefficient for compression and list traversal as well.
•	Used JavaFastPFOR integer compression library since it applies differential coding & is highly efficient being able to decompress integers at a rate of over 1.2 billions per second (4.5 GB/s).

Index Generation:

•	I figured out that implementing the first part i.e. generating intermediate postings from the documents and writing these postings out in unsorted or partially sorted form could be done best using Java rather than C or C++ since there is a lot of support for WET parsing libraries in Java but not so much for C/C++.
•	The second part i.e. sorting and merging the postings needed I/O efficiency & C performs well in that scenario so I decided to go ahead with C for this part also since it is pretty easy to integrate C or for that fact any other executable with Java, since Java provides a ProcessBuilder class which runs any executable as you would in the terminal and also writes out the output & error stream back to console output.
•	For the last part i.e. reformatting the sorted postings into the final index and updating lookup structures, I decided to go ahead once again with Java since manipulating bytes is pretty easy using ByteBuffer class and also updation & writing out the Lexicon and URL Table to disk is pretty convenient.
•	One of the problems which I faced was not being able to figure why the bytes written in Java , when read in C, would get changed. After doing much research I found out that Java’s Native byte format uses BIG_ENDIAN notation which is useful for communicating over networks whereas C uses LITTLE_ENDIAN notation by default. So I fixed that up, and converted everything to LITTLE_ENDIAN notation.
•	Also I decided that uncompressing only the document that needs to parsed at a moment, is the most efficient way to save disk space.
•	For the purpose of reformatting the postings I chose to implement a WordIdToWord Hashmap as well, which is the most efficient way to replace the WordId with the word in the final Inverted Index.


<b>Design Limitations:</b>

HashMap’s in Java grow pretty significantly and occupy more than intended main memory space and start affecting performance after a certain time. By playing around with initial Capacity and load factor values for Hashmap , I tried optimizing the performance. Also writing them out to disk & emptying them after the size has grown beyond a certain threshold could also be a possible solution. 

Due to this a considerable amount of heap space needs to be allocated to Query Processor program since it loads the Lexicon and URL table into HashMap’s.
Nonetheless, query processing performance in java in terms of time taken is excellent.




