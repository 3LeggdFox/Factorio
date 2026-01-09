
public class QuantInQuery extends Query {
    String input;
    String output;
    int prod_mod_level;

    public QuantInQuery(String input, String output, int prod_mod_level, boolean verbose) {
        if (prod_mod_level < 0 || prod_mod_level > 3)
        {
            throw new InvalidModuleLevelException(prod_mod_level);
        }
        this.input = input;
        this.output = output;
        this.prod_mod_level = prod_mod_level;
        this.verbose = verbose;
    }

    public void query(RecipeBrowser browser) {
        if (input.equals("all")) {
            boolean first = true;
            for (String base_ingredient : browser.baseIngredients(output)) {
                if (!first && verbose) {
                    System.out.println();
                } else {
                    first = false;
                }
                double result = browser.quantityIn(base_ingredient, output, prod_mod_level, verbose);
                System.out.print(base_ingredient + ": ");
                System.out.println(String.format("%.3f", result));
            }
        } else {
            double result = browser.quantityIn(input, output, prod_mod_level, verbose);
            System.out.print(input + ": ");
            System.out.println(String.format("%.3f", result));
        }
    }
}

