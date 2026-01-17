
public class QuantInQuery extends Query {
    String ingredient;
    String product;
    double amount;
    int prod_mod_level;

    public QuantInQuery(String ingredient, String product, double amount, int prod_mod_level, boolean verbose) {
        if (prod_mod_level < 0 || prod_mod_level > 3)
        {
            throw new InvalidModuleLevelException(prod_mod_level);
        }
        this.ingredient = ingredient;
        this.product = product;
        this.prod_mod_level = prod_mod_level;
        this.verbose = verbose;
        this.amount = amount;
    }

    public void query(RecipeBrowser browser) {
        if (ingredient.equals("all")) {
            browser.getAllIngredients(product, amount, prod_mod_level, verbose);
        } else if (ingredient.equals("base")) {
            browser.getBasicIngredients(product, amount, prod_mod_level, verbose);
        } else {
            double result = browser.quantIn(ingredient, product, amount, prod_mod_level, verbose);
            System.out.println(ingredient + ": " + result);
        }
    }
}

