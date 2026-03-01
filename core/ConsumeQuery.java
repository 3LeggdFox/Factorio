package core;

/**
 * ConsumeQuery
 * Class to manage queries about how many machines are needed to consume a
 * given number of a given item to make another given item per second
 * 
 * @version 1.1
 */
public class ConsumeQuery extends Query {
    double number_of_input;
    String input;
    String output;
    int prod_mod_level;
    boolean verbose;

    /**
     * Constructor
     * 
     * @param number_of_input The amount of input to consume
     * @param input           The input to consume
     * @param output          The output to make from the input
     * @param prod_mod_level  The level of productivity module being used in all
     *                        capable machines
     * @param verbose         Flag to show more information
     */
    public ConsumeQuery(double number_of_input, String input, String output, int prod_mod_level, boolean verbose) {
        this.number_of_input = number_of_input;
        this.input = input;
        this.output = output;
        this.prod_mod_level = prod_mod_level;
        this.verbose = verbose;
    }

    public void query(RecipeBrowser browser) {
        boolean materials_are_real = browser.all_materials.containsKey(output) && browser.all_materials.containsKey(input); // Checks that the output is a material
        if (!materials_are_real) {
            throw new QueryException("Error: '" + output + "' not found in material list.");
        }
        double result = number_of_input/browser.quantIn(input, output, 1, prod_mod_level, false);
        if (verbose) {
            (new MachinesQuery(output, result, prod_mod_level, true)).query(browser);
            System.out.println();
        }
        System.out.println(String.format("Result: %.3f %ss", result, output));
        
    }
}
