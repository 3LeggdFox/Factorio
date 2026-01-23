package core;

public class ListQuery extends Query {
    String material;

    public ListQuery(String material, boolean verbose) {
        this.material = material;
        this.verbose = verbose;
    }

    public void query(RecipeBrowser browser) {
        browser.listQuery(material, verbose);
    }
}
