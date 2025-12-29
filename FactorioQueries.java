import java.util.ArrayList;
import java.util.HashMap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FactorioQueries 
{

    public static void main(String[] args)
    {
        Scanner scanInp = new Scanner(System.in);
        RecipeBrowser recipes = initialiseBrowser("examplefactory.txt", scanInp);

        while (true)
        {
            System.out.print("Command: ");
            try
            {
                String nextLine = scanInp.nextLine();
                recipes.query(nextLine);
            } catch (ParsingException e)
            {
                System.err.println(e.getMessage());
            } catch (InvalidMaterialException e)
            {
                System.err.println(e.getMessage());
            }
        }            
    }

    static RecipeBrowser initialiseBrowser(String factory, Scanner scanInp)
    {
        try
        {
            File file = new File("recipes.txt");
            Scanner scanner = new Scanner(file);
            ArrayList<Recipe> recipes = new ArrayList<Recipe>();
            HashMap<String, Integer> allMaterials = new HashMap<String, Integer>();
            while (scanner.hasNextLine())
            {
                Recipe recipe = Parser.parseRecipe(scanner.nextLine());
                for (Material input : recipe.input)
                {
                    allMaterials.put(input.material, 1);
                }
                for (Material output : recipe.output)
                {
                    allMaterials.put(output.material, 1);
                }
                recipes.add(recipe);
            }
            scanner.close();

            file = new File("stations.txt");
            scanner = new Scanner(file);
            HashMap<String, Station> stations = new HashMap<String, Station>();
            while (scanner.hasNextLine())
            {
                Station station = Parser.parseStations(scanner.nextLine());
                stations.put(station.name, station);
            }
            scanner.close();

            file = new File(factory);
            scanner = new Scanner(file);
            HashMap<String, Setting> settings = new HashMap<String,Setting>();
            while (scanner.hasNextLine())
            {
                Setting setting = Parser.parseSettings(scanner.nextLine());
                settings.put(setting.topic, setting);
            }
            scanner.close();

            return new RecipeBrowser(recipes, settings, stations, factory, allMaterials, scanInp);
        } catch (FileNotFoundException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
            return null;
        } catch (ParsingException e)
        {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(1);
            return null;
        }
    }
}
