import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;

public class RecipeBrowser 
{
    ArrayList<Recipe> recipes;
    HashMap<String, Setting> settings;
    String factory;
    Scanner scanner;
    HashMap<String, Station> stations;
    
    public RecipeBrowser(ArrayList<Recipe> recipes, HashMap<String, Setting> settings, HashMap<String, Station> stations, String factory)
    {
        this(recipes, settings, stations, factory, new Scanner(System.in));
    }

    public RecipeBrowser(ArrayList<Recipe> recipes, HashMap<String, Setting> settings, HashMap<String, Station> stations, String factory, Scanner scanner)
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

    public int getUserInt(int min, int max) 
    {
        int userIn = scanner.nextInt();
        while (userIn < min && userIn >= max)
        {
            System.out.println("Please select a valid option (in the range [" + min + "," + (max-1) + "]).");
            userIn = scanner.nextInt();
        }
        return userIn;
    }

    public int hasStation(String station)
    {
        System.out.println("Does this factory use " + station + "?");
        System.out.println("0: No.");
        System.out.println("1: Yes.");
        return getUserInt(0, 1);
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
        Station station = pickStation(recipe);
        double sum = 0;
        for (Material material : recipe.input)
        {
            sum += quantIn(input, material.material) * material.quantity / (recipe.amountOutput(output) * station.getProd());
        }
        return sum;
    }

    public Recipe pickRecipe(String output, ArrayList<Recipe> recipes) 
    {
        Setting setting = settings.get(output);
        Recipe recipe = null;
        if (setting == null) 
        {
            int counter = giveOptions(recipes);
            int userIn = getUserInt(0, counter);
            recipe = recipes.get(userIn);
            addNewSetting(output, recipe.altName);
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

    public Station pickStation(Recipe recipe)
    {
        Station station = null;
        int highestPrio = -1;
        ArrayList<String> allowedStations = (ArrayList<String>) recipe.stations.clone();
        if (!recipe.hasReq)
        {
            allowedStations.add("Assembly1");
            allowedStations.add("Assembly2");
            allowedStations.add("Assembly3");
        }
        for (String station_name : allowedStations)
        {
            String setting_name = "has" + station_name;
            Setting setting = settings.get(setting_name);
            if (setting == null)
            {
                addNewSetting(setting_name, Integer.toString(hasStation(station_name)));
                setting = settings.get(setting_name);
            }
            if (setting.setting.equals("1"))
            {
                Station searchStation = stations.get(station_name);
                if (searchStation == null)
                {
                    System.err.println("Error: Recipe uses station not found in station.txt.");
                    return null;
                }
                if (searchStation.priority > highestPrio)
                {
                    station = searchStation;
                    highestPrio = searchStation.priority;
                }
            }
            
        }
        return station;
    }

    public void addNewSetting(String topic, String setting_name)
    {
        Setting setting = new Setting(topic, setting_name);
        settings.put(setting.topic, setting);
        try (FileWriter writer = new FileWriter(factory, true))
        {
            writer.write("\n");
            writer.write(setting.toString());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
