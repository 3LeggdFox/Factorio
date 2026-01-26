package core;

/**
 * MachinesQuery
 * Class to manage queries about how many machines are needed to construct a
 * given number of a given item per second
 * 
 * @version 1.0
 */
public class MachinesQuery extends Query {
    double number_of_output; // Requisite output of the given output per second
    String output; // Given output
    int prod_mod_level; // Level of productivity module being used in all capable machines
    boolean verbose;

    /**
     * Constructor
     * 
     * @param output           The material being output
     * @param number_of_output The amount of the material to be output each second
     * @param prod_mod_level   The level of productivity module being used in all
     *                         capable machines
     * @param verbose          Flag to show more information
     */
    public MachinesQuery(String output, double number_of_output, int prod_mod_level, boolean verbose) {
        this.number_of_output = number_of_output;
        this.output = output;
        this.prod_mod_level = prod_mod_level;
        this.verbose = verbose;
    }

    /**
     * Checks if the query has a valid target and calls the RecipeBrowser function
     * which prints the results
     * 
     * @param browser The RecipeBrowser object storing all relevant factory and file
     *                information
     */
    public void query(RecipeBrowser browser) {
        boolean output_is_real = browser.all_materials.containsKey(output); // Checks that the output is a material
        if (!output_is_real) {
            throw new QueryException("Error: '" + output + "' not found in material list.");
        }
        browser.getMachinesIn(output, number_of_output, prod_mod_level, verbose);
    }
}
