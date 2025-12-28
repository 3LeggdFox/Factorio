import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class RecipeBrowser 
{
    ArrayList<Recipe> recipes;
    ArrayList<Setting> settings;
    ArrayList<Station> stations;
    String factory;
    Scanner scanner;
    
    public RecipeBrowser(ArrayList<Recipe> recipes, ArrayList<Setting> settings, ArrayList<Station> stations, String factory)
    {
        this(recipes, settings, stations, factory, new Scanner(System.in));
    }

    public RecipeBrowser(ArrayList<Recipe> recipes, ArrayList<Setting> settings, ArrayList<Station> stations, String factory, Scanner scanner)
    {
        this.recipes = recipes;
        this.settings = settings;
        this.stations = stations;
        this.factory = factory;
        this.scanner = scanner;
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

    public <T> int giveOptions(Iterable<T> list)
    {
        return giveOptions("Decision must be made between:", list);
    }

    public <T> int giveOptions(String heading, Iterable<T> list)
    {
        System.out.println(heading);
        int counter = 0;
        for (T element : list)
        {
            System.out.print(counter + ": ");
            System.out.println(element);
            counter++;
        }
        return counter;
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
        
        Recipe recipe;
        if (recipes.size() > 1)
        { 
            recipe = pickRecipe(output, recipes);
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

    public Recipe pickRecipe(String output, ArrayList<Recipe> recipes) {
        Setting setting = findSetting(output);
        Recipe recipe = null;
        if (setting == null) 
        {
            int counter = giveOptions(recipes);
            int userIn = getUserInt(0, counter);
            recipe = recipes.get(userIn);
            settings.add(new Setting(output, recipe.altName));
            addLastSetting();
        } else
        {
            for (Recipe r : recipes)
            {
                if (r.altName.equals(setting.setting) || (r.altName == null && setting.setting.equals("default")))
                {
                    recipe = r;
                    break;
                }
            }
        }
        return recipe;
    }

    public int getUserInt(int min, int max) 
    {
        int userIn = scanner.nextInt();
        while (userIn < min && userIn >= max)
        {
            System.out.println("Please select a valid option (in the range [" + min + "," + (max-1) + "]).");
        }
        return userIn;
    }

    public Setting findSetting(String topic)
    {
        for (Setting setting : this.settings)
        {
            if (setting.topic.equals(topic))
            {
                return setting;
            }
        }
        return null;
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
