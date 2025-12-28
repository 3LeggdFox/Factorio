import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FactorioQueries 
{

    public static void main(String[] args)
    {
        Scanner scanInp = new Scanner(System.in);
        RecipeBrowser recipes = initialiseBrowser("examplefactory.txt", scanInp);
        System.out.println(recipes.quantIn("copper", "r_circuit"));
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
            ArrayList<Station> stations = new ArrayList<Station>();
            while (scanner.hasNextLine())
            {
                stations.add(Parser.parseStations(scanner.nextLine()));
            }
            scanner.close();

            file = new File(factory);
            scanner = new Scanner(file);
            ArrayList<Setting> settings = new ArrayList<Setting>();
            while (scanner.hasNextLine())
            {
                settings.add(Parser.parseFactory(scanner.nextLine()));
            }
            scanner.close();

            return new RecipeBrowser(recipes, settings, stations, factory, scanInp);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        return null;
    }
}
