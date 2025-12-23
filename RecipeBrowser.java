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
            for (Material output : recipe.output)
            {
                if (output.material.equalsIgnoreCase(material))
                {
                    recipes.add(recipe);
                    break;
                }
            }
        }
        return recipes;
    }

    double quantIn(String material)
    {
        
        return 0;
    }
}
