public class TopResult {
    public String url;
    public String snippet;
    public String[] terms;
    public int[] tf;

    public TopResult(String url, String snippet, String[] terms, int[] tf){
        this.url = url ;
        this.snippet = snippet;
        this.terms = terms;
        this.tf = tf;
    }
}
