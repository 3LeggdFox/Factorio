import java.util.ArrayList;

public class RecipeBrowser 
{
    ArrayList<Recipe> recipes;
    
    public RecipeBrowser(ArrayList<Recipe> recipes)
    {
        this.recipes = recipes;
    }

    ArrayList<Recipe> findRecipes(String material)
    {
        ArrayList<Recipe> recipes = new ArrayList<Recipe>();
        for (Recipe recipe : this.recipes)
        {
            if (recipe.hasOutput(material))
            {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    double quantIn(String input, String output)
    {
        if (input.equals(output))
        {
            return 1;
        }
        ArrayList<Recipe> recipes = findRecipes(output);
        if (recipes.isEmpty())
        {
            return 0;
        }
        Recipe recipe = recipes.get(0);
        double sum = 0;
        for (Material material : recipe.input)
        {
            sum += quantIn(input, material.material) * material.quantity / recipe.amountOutput(output);
        }
        return sum;
    }
}
