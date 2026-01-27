package core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.InputMismatchException;

public class RecipeBrowser {
    private static final String CORE_FOLDER = "core/";
    private static final String DATA_FOLDER = CORE_FOLDER + "essentialFiles/";
    private static final String RECIPE_FILE = DATA_FOLDER + "recipes.txt";
    private static final String STATION_FILE = DATA_FOLDER + "stations.txt";
    private static final String BASE_RESOURCE_FILE = DATA_FOLDER + "base.txt";
    static final String CONFIG_FILE = DATA_FOLDER + "config.txt";
    static final String FACTORY_FOLDER = CORE_FOLDER + "factories/";
    static final String TEMPLATE_FOLDER = CORE_FOLDER + "factoryTemplates/";

    ArrayList<Recipe> recipes;
    Settings settings;
    String factory;
    Scanner stdin;
    HashMap<String, Station> stations;
    HashMap<String, Integer> all_materials;
    ArrayList<String> sorted_materials;
    HashSet<String> base_ingredients;
    boolean toggle_verbose = false;
    Recipe heavy_crack_recipe;
    Recipe light_crack_recipe;

    public RecipeBrowser(ArrayList<Recipe> recipes, Settings settings,
            HashMap<String, Station> stations, String factory, HashMap<String, Integer> all_materials,
            HashSet<String> base_ingredients, Scanner scanner) {
        this.recipes = recipes;
        this.settings = settings;
        this.settings.addBrowser(this);
        this.stations = stations;
        this.factory = factory;
        this.all_materials = all_materials;
        this.base_ingredients = base_ingredients;
        this.stdin = scanner;
        this.light_crack_recipe = getLightCrackingRecipe();
        this.heavy_crack_recipe = getHeavyCrackingRecipe();
        this.sorted_materials = new ArrayList<>(all_materials.keySet());
        Collections.sort(sorted_materials, Comparator.comparing((material) -> all_materials.get(material)));
    }

    public static RecipeBrowser initialiseBrowser(Scanner scanInp) {
        try {
            File file = new File(CONFIG_FILE);
            String factory;
            try (Scanner scanner = new Scanner(file)) {
                factory = scanner.nextLine();
                scanner.close();
            }
            file = new File(RECIPE_FILE);
            Scanner scanner = new Scanner(file);
            ArrayList<Recipe> recipes = new ArrayList<Recipe>();
            HashMap<String, Integer> all_materials = new HashMap<>();
            int line_number = 0;
            while (scanner.hasNextLine()) {
                Recipe recipe = Parser.parseRecipe(scanner.nextLine());
                for (Material output : recipe.outputs) {
                    if (all_materials.getOrDefault(output.name, 313) > line_number) {
                        all_materials.put(output.name, line_number);
                    }
                }
                for (Material input : recipe.inputs) {
                    if (all_materials.getOrDefault(input.name, 626) > line_number + 313) {
                        all_materials.put(input.name, line_number + 313);
                    }
                }
                recipes.add(recipe);
                line_number++;
            }
            scanner.close();

            for (String material : all_materials.keySet()) {
                all_materials.put(material, all_materials.get(material) % 313);
            }

            file = new File(STATION_FILE);
            scanner = new Scanner(file);
            HashMap<String, Station> stations = new HashMap<String, Station>();
            while (scanner.hasNextLine()) {
                Station station = Parser.parseStations(scanner.nextLine());
                stations.put(station.name, station);
            }
            scanner.close();

            file = new File(FACTORY_FOLDER + factory);
            scanner = new Scanner(file);
            Settings settings = new Settings();
            while (scanner.hasNextLine()) {
                Setting setting = Parser.parseSettings(scanner.nextLine());
                settings.add(setting);
            }
            scanner.close();

            file = new File(BASE_RESOURCE_FILE);
            scanner = new Scanner(file);
            HashSet<String> base_ingredients = new HashSet<>();
            while (scanner.hasNextLine()) {
                base_ingredients.add(scanner.nextLine());
            }
            scanner.close();
            RecipeBrowser browser = new RecipeBrowser(recipes, settings, stations, factory, all_materials,
                    base_ingredients, scanInp);
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

    public void newFactory(String new_factory) {
        File file = new File(FACTORY_FOLDER + new_factory);
        try (Scanner scanner = new Scanner(file)) {
            Settings new_settings = new Settings();
            while (scanner.hasNextLine()) {
                Setting setting = Parser.parseSettings(scanner.nextLine());
                new_settings.add(setting);
            }
            scanner.close();
            this.settings = new_settings;
            this.factory = new_factory;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private ArrayList<Recipe> findRecipes(String material) {
        ArrayList<Recipe> recipes = new ArrayList<Recipe>();
        for (Recipe recipe : this.recipes) {
            if (recipe.hasOutput(material)) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }

    public <T> void searchQuery(String search_string1, boolean logic_is_and, String search_string2,
            boolean has_second_argument, Iterable<String> searching_list, boolean is_setting) {
        int count = 0;
        for (String material : searching_list) {
            if (material.indexOf(search_string1) != -1) {
                if (has_second_argument && logic_is_and && material.indexOf(search_string2) == -1) {
                    continue;
                }
                if (is_setting) {
                    material = settings.get(material).toString();
                }
                System.out.println(material);
                count++;
            }
            if (has_second_argument && !logic_is_and && material.indexOf(search_string2) != -1) {
                if (is_setting) {
                    material = settings.get(material).toString();
                }
                System.out.println(material);
                count++;
            }
        }
        if (count == 0) {
            System.out.println("None.");
        }
    }

    public void getBasicIngredients(String output, double amount, int prod_mod_level, boolean verbose) {
        RecipePath path = new RecipePath(this, output, amount, prod_mod_level);
        if (verbose) {
            path.printRecipePath(prod_mod_level);
        }
        path.printIngredients("base");
    }

    public void getAllIngredients(String output, double amount, int prod_mod_level, boolean verbose) {
        RecipePath path = new RecipePath(this, output, amount, prod_mod_level);
        if (verbose) {
            path.printRecipePath(prod_mod_level);
        }
        path.printIngredients("all");
    }

    public void getMachinesIn(String output, double amount, int prod_mod_level, boolean verbose) {
        RecipePath path = new RecipePath(this, output, amount, prod_mod_level);
        path.printMachines(verbose);
    }

    public double quantIn(String ingredient, String product, double amount, int prod_mod_level, boolean verbose) {
        RecipePath path = new RecipePath(this, product, amount, prod_mod_level);
        if (verbose) {
            path.printRecipePath(prod_mod_level);
        }
        return path.get(ingredient);
    }

    private Recipe getLightCrackingRecipe() {
        for (Recipe recipe : recipes) {
            if (recipe.hasOutput("petrol") && recipe.alt_name.equals("cracking")) {
                return recipe;
            }
        }
        System.err.println("Error: No light oil cracking recipe.");
        System.exit(1);
        return null;
    }

    private Recipe getHeavyCrackingRecipe() {
        for (Recipe recipe : recipes) {
            if (recipe.hasOutput("light_oil") && recipe.alt_name.equals("default")) {
                return recipe;
            }
        }
        System.err.println("Error: No heavy oil cracking recipe.");
        System.exit(1);
        return null;
    }

    private <T> int giveOptions(Iterable<T> list, String material) {
        return giveOptions("Decision concerning '" + material + "' must be made between:", list, material);
    }

    public <T> int giveOptions(String heading, Iterable<T> list, String material) {
        System.out.println(heading);
        int counter = 0;
        for (T element : list) {
            System.out.print(counter + ": ");
            System.out.println(element);
            counter++;
        }
        if (base_ingredients.contains(material)) {
            System.out.print(counter + ": ");
            System.out.println("Basic Resource");
            counter++;
        }
        return counter;
    }

    boolean giveYesNo() {
        System.out.println("0: No.");
        System.out.println("1: Yes.");
        return getUserInt(0, 2) == 1;

    }

    public int getUserInt(int min, int max) {
        boolean first = true;
        int userIn = -1;
        do {
            if (!first) {
                System.out.println("Please select a valid option (in the range [" + min + "," + (max - 1) + "]).");
            } else {
                first = false;
            }
            System.out.print("Choice: ");
            try {
                userIn = stdin.nextInt();
            } catch (InputMismatchException e) {
                System.out.println("That is not an integer.");
            }
            stdin.nextLine();
        } while (userIn < min || userIn >= max);
        return userIn;
    }

    private String userHaveStation(String station) {
        System.out.println("Does this factory use " + station + "?");
        System.out.println("0: No.");
        System.out.println("1: Yes.");
        if (getUserInt(0, 2) == 1) {
            return "yes";
        }
        return "no";
    }

    public void listQuery(String item) {
        if (item.equals("recipes")) {
            for (Recipe recipe : recipes) {
                System.out.println(recipe);
            }
            return;
        }
        if (item.equals("settings")) {
            System.out.println(settings);
            return;
        }
        ArrayList<Recipe> isInput = new ArrayList<Recipe>();
        ArrayList<Recipe> isOutput = new ArrayList<Recipe>();
        ArrayList<Recipe> isStation = new ArrayList<Recipe>();
        for (Recipe recipe : recipes) {
            if (recipe.hasInput(item)) {
                isInput.add(recipe);
            }
            if (recipe.hasOutput(item)) {
                isOutput.add(recipe);
            }
            if (recipe.hasStation(item)) {
                isStation.add(recipe);
            }
        }
        boolean allEmpty = true;
        if (!isOutput.isEmpty()) {
            System.out.println("Item is made in recipes:");
            for (Recipe recipe : isOutput) {
                System.out.println(recipe);
            }
            allEmpty = false;
        }
        if (!isInput.isEmpty()) {
            if (!allEmpty) {
                System.out.println();
            }
            System.out.println("Item is used in recipes:");
            for (Recipe recipe : isInput) {
                System.out.println(recipe);
            }
            allEmpty = false;
        }
        if (!isStation.isEmpty()) {
            if (!allEmpty) {
                System.out.println();
            }
            System.out.println("Item serves as station in recipes:");
            for (Recipe recipe : isStation) {
                System.out.println(recipe);
            }
            allEmpty = false;
        }
        if (allEmpty) {
            System.out.println("This item was not found in any recipes.");
        }
    }

    private String userChooseRecipe(ArrayList<Recipe> list, String material) {
        int counter = giveOptions(list, material);
        int userIn = getUserInt(0, counter);
        if (userIn == list.size()) {
            return "basic";
        }
        return list.get(userIn).alt_name;
    }

    public Recipe pickRecipe(String output) {
        return pickRecipe(output, findRecipes(output));
    }

    private Recipe pickRecipe(String material, ArrayList<Recipe> recipe_options) {
        if (recipe_options.size() == 0) {
            return null;
        }
        if (recipe_options.size() == 1 && !base_ingredients.contains(material)) {
            return recipe_options.get(0);
        }
        Setting setting = settings.get(material);
        Recipe recipe = null;
        if (setting == null) {
            String alt_name = userChooseRecipe(recipe_options, material);
            setting = new Setting(material, alt_name);
            settings.updateSetting(setting);
        }
        if (setting.value.equals("basic")) {
            return null;
        }
        for (Recipe r : recipe_options) {
            if (r.alt_name.equals(setting.value) || (r.alt_name == null && setting.value.equals("default"))) {
                recipe = r;
                break;
            }
        }
        return recipe;
    }

    private String getRecipeOrEmpty(String setting_name) {
        if (settings.has(setting_name)) {
            return settings.get(setting_name).value;
        }
        return "";
    }

    public Station pickStation(Recipe recipe) {
        Station station = null;
        int highestPrio = -1;
        @SuppressWarnings("unchecked")
        ArrayList<String> allowedStations = (ArrayList<String>) recipe.stations.clone();
        for (String station_name : allowedStations) {
            Setting setting = settings.get(station_name);
            if (setting == null) {
                setting = new Setting(station_name, userHaveStation(station_name));
                settings.updateSetting(setting);
            }
            if (setting.value.equals("yes")) {
                Station search_station = stations.get(station_name);
                if (search_station == null) {
                    throw new StationNotFoundException("Error: Recipe uses station not found in station.txt.",
                            station_name, true);
                }
                if (search_station.priority > highestPrio) {
                    station = search_station;
                    highestPrio = search_station.priority;
                }
            }
        }
        if (station == null) {
            throw new StationNotFoundException("Error: Factory does not have any required station.", recipe.toString(),
                    false);
        }
        return station;
    }

    public void query(String line) {
        Query query = Parser.parseQuery(line, toggle_verbose);
        query.query(this);
    }

    public void initCheckCycle() {
        if (checkCycle()) {
            settings.updateSetting(new Setting("water", "basic"));
            settings.updateSetting(new Setting("coal", "basic"));
            settings.updateSetting(new Setting("metallic_chunk", "basic"));
            settings.updateSetting(new Setting("carbonic_chunk", "basic"));
            settings.updateSetting(new Setting("oxide_chunk", "basic"));
        }
    }

    private boolean checkCycle() {
        String water = getRecipeOrEmpty("water");
        String steam = getRecipeOrEmpty("steam");
        String acid = getRecipeOrEmpty("sulfuric_acid");
        String coal = getRecipeOrEmpty("coal");
        String carbon = getRecipeOrEmpty("carbon");
        String carbonic_chunk = getRecipeOrEmpty("carbonic_chunk");
        String metallic_chunk = getRecipeOrEmpty("metallic_chunk");
        String oxide_chunk = getRecipeOrEmpty("oxide_chunk");
        boolean cycle = false;
        if (water.equals("default") && steam.equals("boiling")) {
            cycle = true;
        }
        if (water.equals("default") && steam.equals("default") && acid.equals("default")) {
            cycle = true;
        }
        if (coal.equals("default") && carbon.equals("default")) {
            cycle = true;
        }
        if (carbonic_chunk.equals("metallic")) {
            if (metallic_chunk.equals("carbonic")) {
                cycle = true;
            } else if (metallic_chunk.equals("oxide") && oxide_chunk.equals("carbonic")) {
                cycle = true;
            }
        }
        if (carbonic_chunk.equals("oxide")) {
            if (oxide_chunk.equals("carbonic")) {
                cycle = true;
            } else if (oxide_chunk.equals("metallic") && metallic_chunk.equals("carbonic")) {
                cycle = true;
            }
        }
        if (metallic_chunk.equals("oxide")) {
            if (oxide_chunk.equals("metallic")) {
                cycle = true;
            }
        }
        return cycle;
    }

    public void updateSetting(String setting_name) {
        boolean setting_is_new = !settings.has(setting_name);
        String old_value = "ERROR";
        if (!setting_is_new) {
            old_value = settings.get(setting_name).value;
        }
        Setting new_setting;
        if (all_materials.containsKey(setting_name)) {
            ArrayList<Recipe> possible_recipes = findRecipes(setting_name);
            new_setting = new Setting(setting_name, userChooseRecipe(possible_recipes, setting_name));
            settings.updateSetting(new_setting);
        } else if (stations.containsKey(setting_name)) {
            Station station = stations.get(setting_name);
            new_setting = new Setting(setting_name, userHaveStation(station.name));
            settings.updateSetting(new_setting);
        } else {
            System.err.println("Error: No possible setting fits the name '" + setting_name + "'.");
            return;
        }
        if (setting_is_new) {
            System.out.println("Setting '" + settings.get(setting_name) + "' was added.");
            return;
        } else {
            System.out.println(
                    "Setting '" + setting_name + "' was updated from '" + old_value + "' to '" + new_setting.value
                            + "'.");
        }
    }
}

class StationNotFoundException extends QueryException {
    String searchStationName;
    boolean not_real_station;

    public StationNotFoundException(String message, String search_station_name, boolean not_real_station) {
        super(message);
        this.searchStationName = search_station_name;
        this.not_real_station = not_real_station;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());
        if (not_real_station) {
            builder.append("Searched for '" + searchStationName + "'.");
        } else {
            builder.append("\n" + searchStationName);
        }
        return builder.toString();
    }
}

class InvalidMaterialException extends QueryException {
    public InvalidMaterialException(String output) {
        super("Error: '" + output + "' not found in any recipes.");
    }
}

class CycleException extends QueryException {
    public CycleException(String material) {
        super("Error: New '" + material + "' setting created a cycle.");
    }

    public String getMessage() {
        String message = super.getMessage();
        message += "\nCyclic recipes include:\n\ncoal <-> carbon\nwater <-> steam (<-> sulfuric_acid)\nmetallic_chunk <-> carbonic_chunk <-> oxide_chunk";
        return message;
    }
}