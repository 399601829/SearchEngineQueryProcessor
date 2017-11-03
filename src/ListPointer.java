import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Comparator;
import java.util.Random;

public class ListPointer {
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

    public ListPointer(String term,int docFreq,long startIndex,long endIndex) throws IOException {
        this.term = term;
        this.docFreq = docFreq;
        this.numberOfChunks = (int)Math.ceil(docFreq/128d);
        this.randomAccessFile = new RandomAccessFile("/Users/pravarsingh/IdeaProjects/IndexBuilder/CompressedInvertedIndex", "r");
        this.realStartIndex = startIndex;
        //readLastAndSizeArrays(startIndex);
        this.uncompressed = new boolean[numberOfChunks];
        this.block = 0;
    }

    public void readLastAndSizeArrays(long startIndex) throws IOException {
        byte[] lastArray = new byte[numberOfChunks*4];

        randomAccessFile.seek(startIndex);
        realStartIndex+=randomAccessFile.read(lastArray,0,numberOfChunks*4);

        ByteBuffer bb = ByteBuffer.wrap(lastArray);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        last = new int[lastArray.length/4];

        int index = 0;
        for (int i = 0; i < bb.capacity(); i+=4) {
            last[index++] = bb.getInt();
        }
        bb.clear();

        byte[] sizeDocIdBytes = new byte[numberOfChunks*4];
        realStartIndex+=randomAccessFile.read(sizeDocIdBytes,0,numberOfChunks*4);

        bb = ByteBuffer.wrap(sizeDocIdBytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        sizeDocId = new int[sizeDocIdBytes.length/4];

        index = 0;
        for (int i = 0; i < bb.capacity(); i+=4) {
            sizeDocId[index++] = bb.getInt();
        }
        bb.clear();

        byte[] sizeTfBytes = new byte[numberOfChunks*4];
        realStartIndex+=randomAccessFile.read(sizeTfBytes,0,numberOfChunks*4);

        bb = ByteBuffer.wrap(sizeTfBytes);
        bb.order(ByteOrder.LITTLE_ENDIAN);

        sizeTf = new int[sizeTfBytes.length/4];

        index = 0;
        for (int i = 0; i < bb.capacity(); i+=4) {
            sizeTf[index++] = bb.getInt();
        }
        bb.clear();

    }


}
