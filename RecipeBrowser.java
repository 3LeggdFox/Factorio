import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class RecipeBrowser 
{
    ArrayList<Recipe> recipes;
    ArrayList<Setting> settings;
    String factory;
    Scanner scanner;
    
    public RecipeBrowser(ArrayList<Recipe> recipes, ArrayList<Setting> settings, String factory)
    {
        this.recipes = recipes;
        this.settings = settings;
        this.factory = factory;
        this.scanner = new Scanner(System.in);
    }

    public ArrayList<Recipe> findRecipes(String material)
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

    public double quantIn(String input, String output)
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
        Recipe recipe = null;
        if (recipes.size() > 1)
        { 
            for (Setting setting : settings)
            {
                if (setting.topic.equals(output))
                {
                    for (Recipe r : recipes)
                    {
                        if (r.altName.equals(setting.setting))
                        {
                            recipe = r;
                        } else if (r.altName == null && setting.setting.equals("default"))
                        {
                            recipe = r;
                        }
                    }
                }
            }
            if (recipe == null)
            {
                System.out.println("Decision must be made between:");
                int counter = 0;
                for (Recipe r : recipes)
                {
                    System.out.print(counter + ": ");
                    System.out.println(r);
                    counter++;
                }
                int userIn = scanner.nextInt();
                while (userIn < 0 && userIn >= counter)
                {
                    System.out.println("Please select a valid option (in the range [0," + (counter-1) + "].");
                }
                counter = 0;
                for (Recipe r: recipes)
                {
                    if (counter == userIn)
                    {
                        settings.add(new Setting(output, r.altName));
                        addLastSetting();
                        recipe = r;
                        break;
                    }
                    counter++;
                }
                
            }
        } else 
        {
            recipe = recipes.get(0);
        }
        double sum = 0;
        for (Material material : recipe.input)
        {
            sum += quantIn(input, material.material) * material.quantity / recipe.amountOutput(output);
        }
        return sum;
    }

    public void addLastSetting()
    {
        try (FileWriter writer = new FileWriter(factory, true))
        {
            writer.write("\n");
            writer.write(settings.get(settings.size()-1).toString());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
