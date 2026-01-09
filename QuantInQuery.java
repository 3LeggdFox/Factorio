public class QuantInQuery extends Query
{
    String input;
    String output;
    int prod_mod_level;

    public QuantInQuery(String input, String output, int prod_mod_level, boolean verbose)
    {
        this.input = input;
        this.output = output;
        this.prod_mod_level = prod_mod_level;
        this.verbose = verbose;
    }
    
    public void query(RecipeBrowser browser) throws InvalidMaterialException
    {
        System.out.println(browser.quantityIn(input, output, prod_mod_level, verbose));
    }
}
