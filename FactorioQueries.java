import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class FactorioQueries {
    public static void main(String[] args) {
        Scanner scanInp = new Scanner(System.in);
        RecipeBrowser recipes = initialiseBrowser("examplefactory.txt", scanInp);
        while (true) {
            System.out.print("Command: ");
            try {
                String nextLine = scanInp.nextLine();
                recipes.query(nextLine);
            } catch (QueryException e)
            {
                System.err.println(e.getMessage());
            }
        }
    }

    static RecipeBrowser initialiseBrowser(String factory, Scanner scanInp) {
        try {
            File file = new File("recipes.txt");
            Scanner scanner = new Scanner(file);
            ArrayList<Recipe> recipes = new ArrayList<Recipe>();
            HashSet<String> allMaterials = new HashSet<>();
            while (scanner.hasNextLine()) {
                Recipe recipe = Parser.parseRecipe(scanner.nextLine());
                for (Material input : recipe.inputs) {
                    allMaterials.add(input.name);
                }
                for (Material output : recipe.outputs) {
                    allMaterials.add(output.name);
                }
                recipes.add(recipe);
            }
            scanner.close();

            file = new File("stations.txt");
            scanner = new Scanner(file);
            HashMap<String, Station> stations = new HashMap<String, Station>();
            while (scanner.hasNextLine()) {
                Station station = Parser.parseStations(scanner.nextLine());
                stations.put(station.name, station);
            }
            scanner.close();

            file = new File(factory);
            scanner = new Scanner(file);
            HashMap<String, Setting> settings = new HashMap<String, Setting>();
            while (scanner.hasNextLine()) {
                Setting setting = Parser.parseSettings(scanner.nextLine());
                settings.put(setting.topic, setting);
            }
            scanner.close();

            file = new File("base.txt");
            scanner = new Scanner(file);
            HashSet<String> base_ingredients = new HashSet<>();
            while (scanner.hasNextLine()) {
                base_ingredients.add(scanner.nextLine());
            }
            scanner.close();
            RecipeBrowser browser = new RecipeBrowser(recipes, settings, stations, factory, allMaterials, base_ingredients, scanInp);
            browser.initCheckCycle();
            return browser;
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return null;
        } catch (QueryException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(1);
            return null;
        }
    }
}
