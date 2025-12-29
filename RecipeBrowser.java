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

    public int moduleLevel(String moduleType)
    {
        System.out.println("What level of " + moduleType + " module does this factory have?");
        System.out.println("0: None.");
        System.out.println("1: Level 1.");
        System.out.println("2: Level 2.");
        System.out.println("3: Level 3.");
        return getUserInt(0, 3);
    }

    public double quantIn(String input, String output) throws InternalReferenceException
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
            sum += quantIn(input, material.material) * material.quantity / (recipe.amountOutput(output) * getProd(station));
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
            addNewSetting(new Setting(output, recipe.altName));
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

    public Station pickStation(Recipe recipe) throws InternalReferenceException
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
                setting = new Setting(setting_name, Integer.toString(hasStation(station_name)));
                addNewSetting(setting);
            }
            if (setting.setting.equals("1"))
            {
                Station searchStation = stations.get(station_name);
                if (searchStation == null)
                {
                    throw new InternalReferenceException("Error: Recipe uses station not found in station.txt.", searchStation);
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

    public double getProd(Station station)
    {
        String moduleString = "prodModLevel";
        Setting moduleSetting = settings.get(moduleString);
        if (moduleSetting == null)
        {
            moduleSetting = new Setting(moduleString, Integer.toString(moduleLevel("production")));
            addNewSetting(moduleSetting);
        }
        return station.getProd(Integer.parseInt(moduleSetting.setting));
    }

    public void addNewSetting(Setting setting)
    {
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

class InternalReferenceException extends Exception
{
    Station searchStationName;
    public InternalReferenceException(String message, Station searchStationName)
    {
        super(message);
        this.searchStationName = searchStationName;
    }

    public String getMessage()
    {
        StringBuilder builder = new StringBuilder(super.getMessage());
        builder.append("Searched for '" + searchStationName + "'.");
        return builder.toString();
    }
}