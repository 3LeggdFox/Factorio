public class QuantInQuery extends Query
{
    String input;
    String output;
    
    public QuantInQuery(String input, String output, boolean verbose)
    {
        this.input = input;
        this.output = output;
        this.verbose = verbose;
    }
    
    public void query(RecipeBrowser browser) throws InvalidMaterialException
    {
        System.out.println(browser.quantityIn(input, output, verbose));
    }
}
