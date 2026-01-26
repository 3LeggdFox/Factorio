package core;

/**
 * ListQuery
 * Class to manage queries regarding listing factory or recipe information
 * 
 * @version 1.0
 */
public class ListQuery extends Query {
    String material;

    /**
     * Constructor
     * 
     * @param material The material whose recipes are being listed (can be 'all' or
     *                 'settings')
     */
    public ListQuery(String material) {
        this.material = material;
    }

    /**
     * Calls the RecipeBrowser's function to print the list of recipes/settings
     * 
     * @param browser The RecipeBrowser object storing all relevant factory and file
     *                information
     */
    public void query(RecipeBrowser browser) {
        browser.listQuery(material);
    }
}