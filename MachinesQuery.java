public class MachinesQuery extends Query {
    double number_of_output;
    String output;
    int prod_mod_level;

    public MachinesQuery(String output, double number_of_output, int prod_mod_level, boolean verbose) {
        this.number_of_output = number_of_output;
        this.output = output;
        this.prod_mod_level = prod_mod_level;
        this.verbose = verbose;
    }

    public void query(RecipeBrowser browser) {
        boolean output_is_real = browser.all_materials.contains(output);
        if (!output_is_real) {
            System.err.println("Error: '" + output + "' not found in material list.");
            return;
        }
        browser.getMachinesIn(output, number_of_output, prod_mod_level, verbose);
    }
}
