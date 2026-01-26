package core;

/**
 * QuantInQuery
 * Class to manage queries about the amounts of resources needed to craft other
 * resources
 * 
 * @version 1.0
 */
public class QuantInQuery extends Query {
    String ingredient;
    String product;
    double amount;
    int prod_mod_level;

    /**
     * Constructor
     * 
     * @param ingredient     The material which is used to make something
     * @param product        The material which is being made
     * @param amount         The amount of the material being made
     * @param prod_mod_level Level of productivity module being used in all capable
     *                       machines
     * @param verbose        Flag to show more information
     */
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

    /**
     * Gives information regarding the amount of an ingredient in some amount of a
     * product
     * 
     * @param browser The RecipeBrowser object storing all relevant factory and file
     *                information
     */
    public void query(RecipeBrowser browser) {
        // Checks if the ingredient and product are known materials
        boolean ingredient_is_real = browser.all_materials.containsKey(ingredient);
        boolean product_is_real = browser.all_materials.containsKey(product);
        // Checks if the ingredient is a special keyword
        boolean all = ingredient.equals("all");
        boolean base = ingredient.equals("base");
        // Prints an error if the keywords are not used and the ingredient and/or
        // product are not known
        if (!product_is_real || (!ingredient_is_real && !all && !base)) {
            String offender = ""; // Initialises error string
            if (!ingredient_is_real && !all && !base) { // Only print ingredient is not found if no keyword is used
                offender = ingredient;
            }
            if (!product_is_real) { // Add product to the string
                if (!ingredient_is_real && !all && !base) {
                    offender += "' and '"; // Add conjunction if necessary
                }
                offender += product;
            }
            throw new QueryException("Error: '" + offender + "' not found in material list.");
        }
        if (ingredient.equals("all")) { // Call specific function for keyword
            browser.getAllIngredients(product, amount, prod_mod_level, verbose);
        } else if (ingredient.equals("base")) { // Call specific function for keyword
            browser.getBasicIngredients(product, amount, prod_mod_level, verbose);
        } else {
            double result = browser.quantIn(ingredient, product, amount, prod_mod_level, verbose);
            System.out.println(String.format("%s: %.3f", ingredient, result)); // Format result
        }
    }
}
