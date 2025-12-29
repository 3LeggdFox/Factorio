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
        try 
        {
            System.out.println(recipes.quantIn("copper", "r_circuit"));
        } catch (InternalReferenceException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    static RecipeBrowser initialiseBrowser(String factory, Scanner scanInp)
    {
        try
        {
            File file = new File("recipes.txt");
            Scanner scanner = new Scanner(file);
            ArrayList<Recipe> recipes = new ArrayList<Recipe>();
            while (scanner.hasNextLine())
            {
                recipes.add(Parser.parseRecipe(scanner.nextLine()));
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

            return new RecipeBrowser(recipes, settings, stations, factory, scanInp);
        } catch (FileNotFoundException e)
        {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (ParsingException e)
        {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.err.println("Error: This should be unreachable.");
        return null;
    }
}
