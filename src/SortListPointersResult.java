public class SortListPointersResult {
    public final ListPointer[] lp;
    public final String[] q;
    public final Integer[] docFreq;

    public SortListPointersResult(ListPointer[] lp, String[] q, Integer[] docFreq){
        this.lp = lp;
        this.q = q;
        this.docFreq = docFreq;
    }
}
