
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
        if (input.equals("all"))
        {
            boolean first = true;
            for (String base_ingredient : browser.baseIngredients(output))
            {
                if (!first && verbose)
                {
                    System.out.println();
                } else 
                {
                    first = false;
                }
                double result = browser.quantityIn(base_ingredient, output, prod_mod_level, verbose);
                System.out.print(base_ingredient + ": ");
                System.out.println(String.format("%.3f", result));
            }
        }
        else 
        {
            double result = browser.quantityIn(input, output, prod_mod_level, verbose);
            System.out.print("Result: ");
            System.out.println(String.format("%.3f", result));
        }
    }
}
