
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
        double result = browser.quantityIn(input, output, prod_mod_level, verbose);
        System.out.print("Result: ");
        System.out.println(String.format("%.3f", result));
    }
}
