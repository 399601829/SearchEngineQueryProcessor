import me.lemire.integercompression.differential.IntegratedIntCompressor;
import smile.nlp.Bigram;
import smile.nlp.Corpus;
import smile.nlp.Text;
import smile.nlp.TextTerms;
import smile.nlp.relevance.BM25;
import smile.nlp.relevance.Relevance;
import smile.nlp.relevance.RelevanceRanker;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

public class QuerySearch {
    private final Map<String,Long[]> lexicon;
    private final List<PageTable> pageTableList;
    private final TreeMap<Double,TopResult> top10Results;
    private Long averageDocumentSize = 0l;

    public QuerySearch(){
        this.lexicon = new HashMap<>(31000000, 0.75f);
        this.pageTableList = new ArrayList<>(6000000);
        this.top10Results = new TreeMap<>(Collections.reverseOrder());
    }

    public void loadLexiconIntoMemory() throws IOException {
        FileReader in = new FileReader("/Users/pravarsingh/IdeaProjects/IndexBuilder/Lexicon.txt");
        BufferedReader br = new BufferedReader(in,4096 * 100000);

        String line;
        int count =0;
        while ((line = br.readLine()) != null) {

            String[] words = line.split(" ");

            Long[] list = new Long[3];
            list[0]=Long.parseLong(words[2]);
            list[1]=Long.parseLong(words[4]);
            list[2]=Long.parseLong(words[6]);

            lexicon.put(words[0],list);
            if(count%10000 == 0) {
                System.out.println("Loading lexicon " + count + " " + words[0]);
            }
            count++;

        }
        br.close();
    }

    public void loadURLTableIntoMemory() throws IOException {
        FileReader in = new FileReader("/Users/pravarsingh/IdeaProjects/IndexBuilder/URLTable.txt");
        BufferedReader br = new BufferedReader(in,4096 * 100000);

        String line;
        int numberOfDocuments =0;
        while ((line = br.readLine()) != null) {

            String[] words = line.split("\t");
            PageTable pageTable = new PageTable(words[1],Integer.parseInt(words[2]));

            pageTableList.add(pageTable);
            if(numberOfDocuments %10000 == 0) {
                System.out.println("Loading URL Table " + numberOfDocuments + " " + words[1]);
            }
            numberOfDocuments++;
            averageDocumentSize += Integer.parseInt(words[2]);
        }
        averageDocumentSize = averageDocumentSize/numberOfDocuments;
        br.close();
    }

    //n : Array of Document Frequencies for each term in query
    public void computeBM25Score(int docId,String[] terms, int[] tf, Integer[] n){
        BM25 bm25 = new BM25();
        double r = 0.0;
        for (int i = 0; i < terms.length; i++) {
            r += bm25.rank(new Corpus() {
                               @Override
                               public long size() {
                                   return 0;
                               }

                               @Override
                               public int getNumDocuments() {
                                   return pageTableList.size();
                               }

                               @Override
                               public int getNumTerms() {
                                   return 0;
                               }

                               @Override
                               public long getNumBigrams() {
                                   return 0;
                               }

                               @Override
                               public int getAverageDocumentSize() {
                                   return averageDocumentSize.intValue();
                               }

                               @Override
                               public int getTermFrequency(String term) {
                                   return 0;
                               }

                               @Override
                               public int getBigramFrequency(Bigram bigram) {
                                   return 0;
                               }

                               @Override
                               public Iterator<String> getTerms() {
                                   return null;
                               }

                               @Override
                               public Iterator<Bigram> getBigrams() {
                                   return null;
                               }

                               @Override
                               public Iterator<Text> search(String term) {
                                   return null;
                               }

                               @Override
                               public Iterator<Relevance> search(RelevanceRanker ranker, String term) {
                                   return null;
                               }

                               @Override
                               public Iterator<Relevance> search(RelevanceRanker ranker, String[] terms) {
                                   return null;
                               }
                           },
                    new TextTerms() {
                        @Override
                        public int size() {
                            return pageTableList.get(docId).numberOfTerms;
                        }

                        @Override
                        public Iterable<String> words() {
                            return null;
                        }

                        @Override
                        public Iterable<String> unique() {
                            return null;
                        }

                        @Override
                        public int tf(String term) {
                            return 0;
                        }

                        @Override
                        public int maxtf() {
                            return 0;
                        }
                    },terms[i],tf[i],n[i]);
        }

        if(top10Results.size() < 10){
            top10Results.put(r,new TopResult(pageTableList.get(docId).url,null,terms,tf));
        }
        else if(r > top10Results.lastKey()){
            top10Results.remove(top10Results.lastKey());
            //top10Results.put(r,new TopResult(pageTableList.get(docId).url,getSnippet(docId),terms,tf));
            top10Results.put(r,new TopResult(pageTableList.get(docId).url,null,terms,tf));
        }

    }

    public void computeBM25Score(int docId,String term, int tf, Integer n){
        BM25 bm25 = new BM25();
        double r = bm25.rank(new Corpus() {
                               @Override
                               public long size() {
                                   return 0;
                               }

                               @Override
                               public int getNumDocuments() {
                                   return pageTableList.size();
                               }

                               @Override
                               public int getNumTerms() {
                                   return 0;
                               }

                               @Override
                               public long getNumBigrams() {
                                   return 0;
                               }

                               @Override
                               public int getAverageDocumentSize() {
                                   return averageDocumentSize.intValue();
                               }

                               @Override
                               public int getTermFrequency(String term) {
                                   return 0;
                               }

                               @Override
                               public int getBigramFrequency(Bigram bigram) {
                                   return 0;
                               }

                               @Override
                               public Iterator<String> getTerms() {
                                   return null;
                               }

                               @Override
                               public Iterator<Bigram> getBigrams() {
                                   return null;
                               }

                               @Override
                               public Iterator<Text> search(String term) {
                                   return null;
                               }

                               @Override
                               public Iterator<Relevance> search(RelevanceRanker ranker, String term) {
                                   return null;
                               }

                               @Override
                               public Iterator<Relevance> search(RelevanceRanker ranker, String[] terms) {
                                   return null;
                               }
                           },
                    new TextTerms() {
                        @Override
                        public int size() {
                            return pageTableList.get(docId).numberOfTerms;
                        }

                        @Override
                        public Iterable<String> words() {
                            return null;
                        }

                        @Override
                        public Iterable<String> unique() {
                            return null;
                        }

                        @Override
                        public int tf(String term) {
                            return 0;
                        }

                        @Override
                        public int maxtf() {
                            return 0;
                        }
                    },term,tf,n);

        if(top10Results.size() < 10){
            top10Results.put(r,new TopResult(pageTableList.get(docId).url,null,new String[]{term},new int[]{tf}));
        }
        else if(r > top10Results.lastKey()){
            top10Results.remove(top10Results.lastKey());
            //top10Results.put(r,new TopResult(pageTableList.get(docId).url,getSnippet(docId),terms,tf));
            top10Results.put(r,new TopResult(pageTableList.get(docId).url,null,new String[]{term},new int[]{tf}));
        }

    }


    public ListPointer openList(String term) throws IOException {
        if(lexicon.containsKey(term)){
            ListPointer lp = new ListPointer(term,lexicon.get(term)[2].intValue(),lexicon.get(term)[0],lexicon.get(term)[1]);
            lp.readLastAndSizeArrays(lexicon.get(term)[0]);
            return lp;
        }else {
            return null;
        }

    }

    public void closeList(){
        top10Results.clear();
    }

    public int[] uncompress(ListPointer lp) throws IOException {
        if (lp.uncompressed[lp.block]){
            return lp.temp;
        }
        lp.ind = 0;
        int bytesToSkip=0;
        for(int i=0;i<lp.block;i++){
            bytesToSkip+=lp.sizeDocId[i]+lp.sizeTf[i];
        }
        lp.randomAccessFile.seek(lp.realStartIndex+bytesToSkip);
        byte[] compressedDocIdBytes = new byte[lp.sizeDocId[lp.block]];
        lp.randomAccessFile.read(compressedDocIdBytes,0,lp.sizeDocId[lp.block]);

        ByteBuffer bb = ByteBuffer.wrap(compressedDocIdBytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int[] compressedDocId = new int[bb.capacity()/4];
        int index = 0;
        for (int i = 0; i < bb.capacity(); i+=4) {
            compressedDocId[index++] = bb.getInt();
        }
        IntegratedIntCompressor iic = new IntegratedIntCompressor();
        lp.uncompressed[lp.block] = true;
        return lp.temp = iic.uncompress(compressedDocId);
    }

    public int nextGEQ(ListPointer lp,int k) throws IOException {
        while (lp.last[lp.block] < k) {
            lp.block++; /* skip block */

            if(lp.block == lp.last.length) return Integer.MAX_VALUE;
        }

        int[] temp = uncompress(lp);

        while (temp[lp.ind] < k) {
            lp.ind++;
        }
        return(temp[lp.ind]);
    }

    public int getFreq(ListPointer lp) throws IOException {
        int bytesToSkip=0;
        for(int i=0;i<lp.block;i++){
            bytesToSkip+=lp.sizeDocId[i]+lp.sizeTf[i];
        }
        bytesToSkip+=lp.sizeDocId[lp.block];
        lp.randomAccessFile.seek(lp.realStartIndex+bytesToSkip);
        byte[] compressedTfBytes = new byte[lp.sizeTf[lp.block]];
        lp.randomAccessFile.read(compressedTfBytes,0,lp.sizeTf[lp.block]);

        ByteBuffer bb = ByteBuffer.wrap(compressedTfBytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        int[] compressedTf = new int[bb.capacity()/4];
        int index = 0;
        for (int i = 0; i < bb.capacity(); i+=4) {
            compressedTf[index++] = bb.getInt();
        }

        IntegratedIntCompressor iic = new IntegratedIntCompressor();
        return iic.uncompress(compressedTf)[lp.ind];

    }

    public void printResults(){
        if (top10Results.size() == 0) System.out.println("No Search Results Found");
        for(Double rank : top10Results.keySet()){
            System.out.println(top10Results.get(rank).url+" " +rank);
        }
    }

    public SortListPointersResult sortListPointersAndRemoveNull(ListPointer[] lp, String[] q, Integer[] docFreq){
        Arrays.sort(lp,new Comparator<ListPointer>() {
            @Override
            public int compare(ListPointer o1, ListPointer o2) {
                if(o1 == null && o2==null) return 0;
                else if(o1 == null && o2!=null) return 0 - o2.docFreq;
                else if (o1 != null && o2==null) return o1.docFreq - 0;
                return o1.docFreq - o2.docFreq;
            }
        });
        List<ListPointer> removedNull = new ArrayList<ListPointer>(10);
        List<String> terms = new ArrayList<String>(10);
        List<Integer> docFreqs = new ArrayList<>(10);
        for(ListPointer l : lp ){
            if(l!=null){
                removedNull.add(l);
                terms.add(l.term);
                docFreqs.add(l.docFreq);
            }
        }
        lp = new ListPointer[removedNull.size()];
        q = new String[terms.size()];
        docFreq = new Integer[docFreqs.size()];
        removedNull.toArray(lp);
        terms.toArray(q);
        docFreqs.toArray(docFreq);

        return new SortListPointersResult(lp,q,docFreq);
    }

    public int findMaxDocId(ListPointer lp){
        return lp.last[lp.numberOfChunks - 1];
    }

    public void executeQuery(String query) throws IOException {
        String[] q = query.toLowerCase().split(" ");
        int num = q.length;
        ListPointer[] lp = new ListPointer[num];
        for (int i = 0; i < num; i++){
            lp[i] = openList(q[i]);
        }
        Integer[] docFreq = new Integer[num];

        SortListPointersResult s = sortListPointersAndRemoveNull(lp,q,docFreq);
        lp = s.lp;
        q = s.q;
        docFreq = s.docFreq;

        num = lp.length;

        if(num == 0){
            return;
        }

        int maxDocID = findMaxDocId(lp[0]);

        //Algo for conjunctive Query Processing
        int did = 0;
        while (did <= maxDocID)
        {
            /* get next post from shortest list */
            did = nextGEQ(lp[0], did);
            int d=0;
            /* see if you find entries with same docID in other lists */
            for (int i=1; (i<num) && ((d=nextGEQ(lp[i], did)) == did); i++);

            if (d > did) did = d; /* not in intersection */

            else
            {
                int[] f = new int[num];
                /* docID is in intersection; now get all frequencies */
                for (int i=0; i<num; i++) {
                    f[i] = getFreq(lp[i]);
                }

                /* compute BM25 score from frequencies and other data */
                computeBM25Score(did,q,f,docFreq);
                did++; /* and increase did to search for next post */
            }
        }

        //Algo for disjunctive Query Processing
        if(top10Results.size()==0){
            System.out.println("No results found using conjunctive processing. Applying disjunctive processing...");
            for (int i = 0; i < num; i++){
                lp[i] = openList(q[i]);
            }

            for (int i=0;i<num;i++) {
                did = 0;
                maxDocID = findMaxDocId(lp[i]);
                while (did <= maxDocID) {
                    did = nextGEQ(lp[i], did);
                    computeBM25Score(did,q[i],getFreq(lp[i]),docFreq[i]);
                    did++;
                }
            }
        }

    }

    public static void main(String[] args) throws IOException {
        QuerySearch querySearch = new QuerySearch();
        querySearch.loadLexiconIntoMemory();
        querySearch.loadURLTableIntoMemory();
        while(true) {
            System.out.println("Enter Search Query :");
            Scanner in = new Scanner(System.in);
            String query = in.nextLine();

            if(query.toLowerCase().equals("exit")){
                break;
            }

            long querySearchStartTime = System.currentTimeMillis();
            //queryExecutionCall
            querySearch.executeQuery(query.trim());
            long querySearchEndTime = System.currentTimeMillis();

            querySearch.printResults();

            System.out.println("Query executed in: " + (querySearchEndTime - querySearchStartTime) + " ms");

            querySearch.closeList();


        }
    }
}
