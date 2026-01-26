package core;

/**
 * Query
 * Abstract parent class for all query classes
 * 
 * @version 1.0
 */
public abstract class Query {
    boolean verbose;

    /**
     * Method which performs the class-specific query
     * 
     * @param browser The RecipeBrowser object storing all relevant factory and file
     *                information
     */
    public void query(RecipeBrowser browser) {
    };
}