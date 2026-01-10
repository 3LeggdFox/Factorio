import java.lang.Math;

public class MachinesQuery extends Query {
    double number_of_output;
    String output;
    int prod_mod_level;

    public MachinesQuery(double number_of_output, String output, int prod_mod_level, boolean verbose) {
        this.number_of_output = number_of_output;
        this.output = output;
        this.prod_mod_level = prod_mod_level;
        this.verbose = verbose;
    }

    public void query(RecipeBrowser browser) {
        boolean first = true;
        for (String must_craft : browser.nonBaseIngredients(output)) {
            if (verbose && !first) {
                System.out.println();
            } else {
                first = false;
            }
            Recipe recipe = browser.pickRecipe(must_craft);
            Station station = browser.pickStation(recipe);
            double crafting_time = recipe.getCraftingTime(station, prod_mod_level);
            double amount = browser.quantityIn(must_craft, output, prod_mod_level, false);
            double machines = number_of_output * amount * crafting_time;
            if (verbose) {
                System.out.println("Using Recipe: " + recipe.toStringSpecificVerbose(station, prod_mod_level));
                System.out.println(String.format(must_craft + ": %.3f %ss", machines, station.name));
            } else {
                machines = Math.ceil(machines);
                System.out.println(String.format(must_craft + ": %.0f %ss", machines, station.name));
            }
        }
    }
}
