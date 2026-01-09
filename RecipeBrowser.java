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
    HashMap<String, Integer> allMaterials;
    HashMap<String, HashMap<String, Double>> quantInCache = new HashMap<>();
    
    public RecipeBrowser(ArrayList<Recipe> recipes, HashMap<String, Setting> settings, HashMap<String, Station> stations, String factory, HashMap<String, Integer> allMaterials)
    {
        this(recipes, settings, stations, factory, allMaterials, new Scanner(System.in));
    }

    public RecipeBrowser(ArrayList<Recipe> recipes, HashMap<String, Setting> settings, HashMap<String, Station> stations, String factory, HashMap<String, Integer> allMaterials, Scanner scanner)
    {
        this.recipes = recipes;
        this.settings = settings;
        this.stations = stations;
        this.factory = factory;
        this.allMaterials = allMaterials;
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

    public double quantityIn(String input, String output, boolean verbose) throws InvalidMaterialException
    {
        if (allMaterials.get(output) == null)
        {
            throw new InvalidMaterialException(output);
        }
        try
        {
            double result = quantIn(input, output, verbose);
            quantInCache.clear();
            return result;
        } catch (InternalReferenceException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
            return 0;
        }
    }

    private double quantIn(String input, String output, boolean verbose) throws InternalReferenceException
    {
        HashMap<String, Double> map;
        map = quantInCache.get(input);
        if (map != null)
        {
            Double result = map.get(output);
            if (result != null)
            {
                return result;
            }
        }
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
        double productivity = getProd(station);
        if (verbose)
        {
            System.out.println(recipe.toStringSpecific(station, productivity));
        }
        double sum = 0;
        for (Material material : recipe.input)
        {
            sum += quantIn(input, material.material, verbose) * material.quantity / (recipe.amountOutput(output) * productivity);
        }
        
        map = quantInCache.get(input);
        if (map == null)
        {
            map = new HashMap<String, Double>();
        }
        map.put(output, sum);
        quantInCache.put(input, map);
        return sum;
    }

    public void listQuery(String item, boolean verbose)
    {
        ArrayList<Recipe> isInput = new ArrayList<Recipe>();
        ArrayList<Recipe> isOutput = new ArrayList<Recipe>();
        ArrayList<Recipe> isStation = new ArrayList<Recipe>();
        for (Recipe recipe : recipes)
        {
            if (recipe.hasInput(item))
            {
                isInput.add(recipe);
            }
            if (recipe.hasOutput(item))
            {
                isOutput.add(recipe);
            }
            if (recipe.hasStation(item))
            {
                isStation.add(recipe);
            }
        }
        boolean allEmpty = true;
        if (!isOutput.isEmpty())
        {
            System.out.println("Item is made in recipes:");
            for (Recipe recipe : isOutput)
            {
                System.out.println(recipe);
            }
            allEmpty = false;
            System.out.println();
        }
        if (!isInput.isEmpty())
        {
            System.out.println("Item is used in recipes:");
            for (Recipe recipe : isInput)
            {
                System.out.println(recipe);
            }
            allEmpty = false;
            System.out.println();
        }
        if (!isStation.isEmpty())
        {
            System.out.println("Item serves as station in recipes:");
            for (Recipe recipe : isStation)
            {
                System.out.println(recipe);
            }
            allEmpty = false;
            System.out.println();
        }
        if (allEmpty)
        {
            System.out.println("This item was not found in any recipes.");
        }
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

    public void query(String line) throws ParsingException, InvalidMaterialException
    {
        Query query = Parser.parseQuery(line, allMaterials);
        query.query(this);
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

class InvalidMaterialException extends Exception
{
    public InvalidMaterialException(String output)
    {
        super("Error: '" + output + "' not found in any recipes.");
    }
}