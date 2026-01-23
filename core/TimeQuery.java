package core;

public class TimeQuery extends Query {
    String material;
    int prod_mod_level;

    public TimeQuery(String material, int prod_mod_level, boolean verbose)
    {
        if (prod_mod_level < 0 || prod_mod_level > 3)
        {
            throw new InvalidModuleLevelException(prod_mod_level);
        }
        this.material = material;
        this.prod_mod_level = prod_mod_level;
        this.verbose = verbose;
    }

    public void query(RecipeBrowser browser)
    {
        Recipe recipe = browser.pickRecipe(material);
        if (recipe == null)
        {
            System.err.println("Error: '" + material + "' has no crafting recipe.");
            return;
        }
        Station station = browser.pickStation(recipe);
        double crafting_time = recipe.getCraftingTime(station, prod_mod_level);
        if (verbose)
        {
            System.out.println(recipe.toStringSpecificVerbose(station, prod_mod_level));
        }
        System.out.println(String.format("Time: %.3f seconds", crafting_time));
    }
}
