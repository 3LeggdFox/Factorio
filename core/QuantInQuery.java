package core;

public class QuantInQuery extends Query {
    String ingredient;
    String product;
    double amount;
    int prod_mod_level;

    public QuantInQuery(String ingredient, String product, double amount, int prod_mod_level, boolean verbose) {
        if (prod_mod_level < 0 || prod_mod_level > 3) {
            throw new InvalidModuleLevelException(prod_mod_level);
        }
        this.ingredient = ingredient;
        this.product = product;
        this.prod_mod_level = prod_mod_level;
        this.verbose = verbose;
        this.amount = amount;
    }

    public void query(RecipeBrowser browser) {
        boolean ingredient_is_real = browser.all_materials.contains(ingredient);
        boolean product_is_real = browser.all_materials.contains(product);
        boolean all = ingredient.equals("all");
        boolean base = ingredient.equals("base");
        if (!product_is_real || (!ingredient_is_real && !all && !base)) {
            String offender = "";
            if (!ingredient_is_real && !all && !base) {
                offender = ingredient;
            }
            if (!product_is_real) {
                if (!ingredient_is_real && !all && !base) {
                    offender += "' and '";
                }
                offender += product;
            }
            System.err.println("Error: '" + offender + "' not found in material list.");
            return;
        }
        if (ingredient.equals("all")) {
            browser.getAllIngredients(product, amount, prod_mod_level, verbose);
        } else if (ingredient.equals("base")) {
            browser.getBasicIngredients(product, amount, prod_mod_level, verbose);
        } else {
            double result = browser.quantIn(ingredient, product, amount, prod_mod_level, verbose);
            System.out.println(String.format("%s: %.3f", ingredient, result));
        }
    }
}
