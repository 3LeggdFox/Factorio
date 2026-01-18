import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.HashMap;
import java.util.HashSet;

public class RecipeBrowser {
    ArrayList<Recipe> recipes;
    HashMap<String, Setting> settings;
    String factory;
    Scanner stdin;
    HashMap<String, Station> stations;
    HashSet<String> all_materials;
    HashSet<String> base_ingredients;
    boolean toggle_verbose = false;
    HashMap<Recipe, Double> steps = new HashMap<>();
    ArrayList<Recipe> recipe_order = new ArrayList<>();
    HashMap<String, Double> resources = new HashMap<>();
    HashMap<String, Double> all_resources = new HashMap<>();
    ArrayList<String> required_resources = new ArrayList<>();
    HashSet<String> cycle_check = new HashSet<>();

    public RecipeBrowser(ArrayList<Recipe> recipes, HashMap<String, Setting> settings,
            HashMap<String, Station> stations, String factory, HashSet<String> allMaterials, HashSet<String> base_ingredients, Scanner scanner) {
        this.recipes = recipes;
        this.settings = settings;
        this.stations = stations;
        this.factory = factory;
        this.all_materials = allMaterials;
        this.base_ingredients = base_ingredients;
        this.stdin = scanner;
    }

    public static RecipeBrowser initialiseBrowser(String factory, Scanner scanInp) {
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

            file = new File("factories/" + factory);
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

    public void newFactory(String new_factory) {
        File file = new File("factories/" + new_factory);
        try (Scanner scanner = new Scanner(file)) {
            HashMap<String, Setting> new_settings = new HashMap<String, Setting>();
            while (scanner.hasNextLine()) {
                Setting setting = Parser.parseSettings(scanner.nextLine());
                new_settings.put(setting.topic, setting);
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

    public void getMachinesIn(String output, double amount, int prod_mod_level, boolean verbose) {
        getRecipesIn(output, amount, prod_mod_level);
        boolean first = true;
        for (Recipe recipe : steps.keySet()) {
            if (!first) {
                System.out.println();
            } else {
                first = false;
            }
            Station station = pickStation(recipe);
            double crafting_time = recipe.getCraftingTime(station, prod_mod_level);
            double machines = steps.get(recipe) * crafting_time;
            if (verbose) {
                String station_string = recipe.toStringSpecificVerbose(station, prod_mod_level);
                System.out.println(String.format("%s\nNeed: %.3f %ss", station_string, machines, station.name));
            } else {
                machines = Math.ceil(machines);
                System.out.println(String.format("Using Recipe: %s\nNeed: %.0f %ss", recipe.toStringSpecific(station), machines, station.name));
            }
        }
        reset();
    }

    public <T> void searchQuery(String search_string1, boolean logic_is_and, String search_string2, boolean has_second_argument, Iterable<String> searching_list, boolean is_setting) {
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
        getRecipesIn(output, amount, prod_mod_level);
        if (verbose) {
            printRecipePath(prod_mod_level);
        }
        for (String material : resources.keySet()) {
            double quantity = resources.get(material);
            if (quantity != 0) {
                System.out.println(String.format("%s: %.3f", material, quantity));
            }
        }
        reset();
    }

    public void getAllIngredients(String output, double amount, int prod_mod_level, boolean verbose) {
        getRecipesIn(output, amount, prod_mod_level);
        if (verbose) {
            printRecipePath(prod_mod_level);
        }
        for (String material : all_resources.keySet()) {
            double quantity = all_resources.get(material);
            if (quantity != 0) {
                System.out.println(String.format("%s: %.3f", material, quantity));
            }
        }
        reset();
    }

    public double quantIn(String ingredient, String product, double amount, int prod_mod_level, boolean verbose) {
        getRecipesIn(product, amount, prod_mod_level);
        double result = all_resources.getOrDefault(ingredient, 0.0);
        if (verbose) {
            printRecipePath(prod_mod_level);
        }
        reset();
        return result;
    }

    private void clearUnecessary(String output, double amount, int prod_mod_level) {
        Recipe recipe = pickRecipe(output);
        if (recipe == null) {
            return;
        }
        Station station = pickStation(recipe);
        double productivity = getProductivity(recipe, station, prod_mod_level);
        double undos = 0;
        boolean can_undo = true;
        Recipe store_recipe = recipe;
        if (recipe.has_cycle) {
            recipe = recipe.getNoCycleClone();
        }
        while (true) {
            for (Material material : recipe.outputs) {
                double this_amount = amount * material.quantity / recipe.amountOutput(output);
                double current_amount = resources.get(material.name);
                if (current_amount - (this_amount * (undos + 1)) > 0)
                {
                    can_undo  = false;
                }
            }
            if (can_undo) {
                undos++;
            } else {
                break;
            }
        }
        double recipe_count = steps.getOrDefault(store_recipe, 0.0);
        if (undos >= recipe_count) {
            undos = recipe_count;
            recipe_order.remove(recipe);
        }
        steps.put(store_recipe, recipe_count - undos);
        for (int i = 0; i < undos; ++i) {
            for (Material material : recipe.outputs) {
                double this_amount = amount * material.quantity / recipe.amountOutput(output);
                double current_amount = resources.get(material.name);
                resources.put(material.name, current_amount - this_amount);
                all_resources.put(material.name, all_resources.getOrDefault(material.name, 0.0) - this_amount);
            }
            for (Material material : recipe.inputs) {
                double this_amount = amount * material.quantity / (recipe.amountOutput(output) / productivity);
                double current_amount = resources.get(material.name);
                resources.put(material.name, current_amount + this_amount);
                all_resources.put(material.name, all_resources.get(material.name) + this_amount);
                if (current_amount + this_amount < 0) {
                    clearUnecessary(material.name, current_amount + this_amount, prod_mod_level);
                }
            }
        }
    }

    private void printRecipePath(int prod_mod_level) {
        for (Recipe recipe : recipe_order) {
            Station station = pickStation(recipe);
            System.out.println(recipe.toStringSpecificVerbose(station, prod_mod_level));
        }
    }

    private void getRecipesIn(String output, double amount, int prod_mod_level) {
        resources.put(output, amount);
        all_resources.put(output, amount);
        required_resources.add(output);
        while (!required_resources.isEmpty()) {
            String material = required_resources.get(0);
            double still_need = resources.get(material);
            addRecipes(material, still_need, prod_mod_level);
        }
        for (String material : resources.keySet()) {
            double quantity = resources.get(material);
            if (quantity < 0) {
                clearUnecessary(material, quantity, prod_mod_level);
            }
        }
    }

    private void addRecipes(String output, double amount, int prod_mod_level) {
        Recipe recipe = pickRecipe(output);
        if (recipe == null) {
            required_resources.remove(output);
            return;
        }
        if (!steps.containsKey(recipe)) {
            recipe_order.add(recipe);
        }
        Station station = pickStation(recipe);
        double productivity = getProductivity(recipe, station, prod_mod_level);
        Recipe store_recipe = recipe;
        if (recipe.has_cycle) {
            recipe = recipe.getNoCycleClone();
        }
        double recipe_completions = amount / (recipe.amountOutput(output) * productivity);
        double recipe_count = steps.getOrDefault(store_recipe, 0.0) + recipe_completions;
        steps.put(store_recipe, recipe_count);
        for (Material material : recipe.inputs) {
            double this_amount = recipe_completions * material.quantity;
            if (resources.containsKey(material.name)) {
                double current = resources.get(material.name);
                resources.put(material.name, current + this_amount);
                all_resources.put(material.name, all_resources.get(material.name) + this_amount);
                if (current <= 0 && current + this_amount > 0) {
                    required_resources.add(material.name);
                }
            } else {
                resources.put(material.name, this_amount);
                all_resources.put(material.name, this_amount);
                required_resources.add(material.name);
            }
        }
        for (Material material : recipe.outputs) {
            double this_amount = -material.quantity * amount / recipe.amountOutput(output);
            if (resources.containsKey(material.name)) {
                double current = resources.get(material.name);
                resources.put(material.name, current + this_amount);
                if (current + this_amount <= 0) {
                    required_resources.remove(material.name);
                }
            } else {
                resources.put(material.name, this_amount);
            }
        }
    }

    private void reset() {
        steps.clear();
        resources.clear();
        all_resources.clear();
        recipe_order = new ArrayList<>();
    }

    private double getProductivity(Recipe recipe, Station station, int prod_mod_level) {
        if (recipe.can_prod) {
            return station.getProd(prod_mod_level);
        }
        return station.getProd(0);

    }

    private <T> int giveOptions(Iterable<T> list, boolean is_base_resource, String material) {
        return giveOptions("Decision concerning '" + material + "' must be made between:", list, is_base_resource);
    }

    public <T> int giveOptions(String heading, Iterable<T> list, boolean is_base_resource) {
        System.out.println(heading);
        int counter = 0;
        for (T element : list) {
            System.out.print(counter + ": ");
            System.out.println(element);
            counter++;
        }
        if (is_base_resource) {
            System.out.print(counter + ": ");
            System.out.println("Basic Resource");
            counter++;
        }
        return counter;
    }

    public boolean giveYesNo() {
        System.out.println("0: No.");
        System.out.println("1: Yes.");
        return getUserInt(0, 1) == 1;

    }

    public int getUserInt(int min, int max) {
        System.out.print("Choice: ");
        int userIn = stdin.nextInt();
        stdin.nextLine();
        while (userIn < min && userIn >= max) {
            System.out.println("Please select a valid option (in the range [" + min + "," + (max - 1) + "]).");
            System.out.print("Choice: ");
            userIn = stdin.nextInt();
            stdin.nextLine();
        }
        return userIn;
    }

    private int hasStation(String station) {
        System.out.println("Does this factory use " + station + "?");
        System.out.println("0: No.");
        System.out.println("1: Yes.");
        return getUserInt(0, 1);
    }

    private int moduleLevel(String moduleType) {
        System.out.println("What level of " + moduleType + " module does this factory have?");
        System.out.println("0: None.");
        System.out.println("1: Level 1.");
        System.out.println("2: Level 2.");
        System.out.println("3: Level 3.");
        return getUserInt(0, 3);
    }

    public void listQuery(String item, boolean verbose) {
        if (item.equals("recipes")) {
            for (Recipe recipe : recipes) {
                System.out.println(recipe);
            }
            return;
        }
        if (item.equals("settings")) {
            for (Setting setting : settings.values()) {
                System.out.println(setting);
            }
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
            System.out.println();
        }
        if (!isInput.isEmpty()) {
            System.out.println("Item is used in recipes:");
            for (Recipe recipe : isInput) {
                System.out.println(recipe);
            }
            allEmpty = false;
            System.out.println();
        }
        if (!isStation.isEmpty()) {
            System.out.println("Item serves as station in recipes:");
            for (Recipe recipe : isStation) {
                System.out.println(recipe);
            }
            allEmpty = false;
            System.out.println();
        }
        if (allEmpty) {
            System.out.println("This item was not found in any recipes.");
        }
    }

    private Recipe pickRecipe(String output, ArrayList<Recipe> recipe_options) {
        if (recipe_options.size() == 0) {
            return null;
        }
        if (recipe_options.size() == 1 && !base_ingredients.contains(output)) {
            return recipe_options.get(0);
        }
        Setting setting = settings.get(output);
        Recipe recipe = null;
        if (setting == null) {
            int counter = giveOptions(recipe_options, base_ingredients.contains(output), output);
            int userIn = getUserInt(0, counter);
            if (userIn == recipe_options.size()) {
                addNewSetting(new Setting(output, "basic"));
                return null;
            } else {
                recipe = recipe_options.get(userIn);
            }
            addNewSetting(new Setting(output, recipe.alt_name));
        } else {
            if (setting.value.equals("basic")) {
                return null;
            }
            for (Recipe r : recipe_options) {
                if (r.alt_name.equals(setting.value) || (r.alt_name == null && setting.value.equals("default"))) {
                    recipe = r;
                    break;
                }
            }
        }
        return recipe;
    }

    private String chooseRecipe(String material) {
        ArrayList<Recipe> recipe_options = findRecipes(material);
        int counter = giveOptions(recipe_options, base_ingredients.contains(material), material);
        int userIn = getUserInt(0, counter);
        String recipe_name;
        if (userIn == recipe_options.size()) {
            recipe_name = "basic";
        } else {
            recipe_name = recipe_options.get(userIn).alt_name;
        }
        addNewSetting(new Setting(material, recipe_name), false);
        return recipe_name;
    }

    private String getRecipeOrChoose(String setting_name) {
        if (settings.containsKey(setting_name)) {
            return settings.get(setting_name).topic;
        }
        return chooseRecipe(setting_name);
    }

    public Recipe pickRecipe(String output) {
        return pickRecipe(output, findRecipes(output));
    }

    public Station pickStation(Recipe recipe) {
        Station station = null;
        int highestPrio = -1;
        ArrayList<String> allowedStations = (ArrayList<String>) recipe.stations.clone();
        for (String station_name : allowedStations) {
            String setting_name = "has" + station_name;
            Setting setting = settings.get(setting_name);
            if (setting == null) {
                setting = new Setting(setting_name, Integer.toString(hasStation(station_name)));
                addNewSetting(setting);
            }
            if (setting.value.equals("1")) {
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
            throw new StationNotFoundException("Error: Factory does not have a required Station.", recipe.toString(), false);
        }
        return station;
    }

    public double getProd(Station station, int prod_mod_level) {
        String moduleString = "prodModLevel";
        Setting moduleSetting = settings.get(moduleString);
        if (moduleSetting == null) {
            moduleSetting = new Setting(moduleString, Integer.toString(moduleLevel("production")));
            addNewSetting(moduleSetting);
        }
        return station.getProd(Integer.parseInt(moduleSetting.value));
    }

    public void query(String line) throws ParsingException, InvalidMaterialException {
        Query query = Parser.parseQuery(line, toggle_verbose);
        query.query(this);
    }

    public void initCheckCycle() {
        if (checkCycle()) {
            addNewSetting(new Setting("water", "basic"));
            addNewSetting(new Setting("coal", "basic"));
        }
    }

    private boolean checkCycle() {
        String water = getRecipeOrChoose("water");
        String steam = getRecipeOrChoose("steam");
        String acid = getRecipeOrChoose("sulfuric_acid");
        String coal = getRecipeOrChoose("coal");
        String carbon = getRecipeOrChoose("carbon");
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
        return cycle;
    }

    private void addNewSetting(Setting setting) {
        addNewSetting(setting, true);
    }

    private void addNewSetting(Setting setting, boolean check_cycle) {
        Setting initial = settings.get(setting.topic);
        settings.put(setting.topic, setting);
        if (check_cycle) {
            if (checkCycle()) {
                settings.put(setting.topic, initial);
                throw new CycleException(setting.topic);
            }
        }
        try (FileWriter writer = new FileWriter(factory, true)) {
            writer.write("\n");
            writer.write(setting.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void changeSetting(Setting setting) {
        if (!settings.containsKey(setting.topic)) {
            System.out
                    .println("The setting '" + setting.topic + "' was not found. Would you like to add this setting?");
            if (giveYesNo()) {
                addNewSetting(setting);
                System.out.println("Setting '" + setting.toString() + "' was added.");
            }
        } else {
            String old_value = settings.get(setting.topic).value;
            settings.put(setting.topic, setting);
            System.out.println(
                    "Setting '" + setting.topic + "' was updated from '" + old_value + "' to '" + setting.value + "'.");
        }

        try (FileWriter writer = new FileWriter(factory)) {
            boolean first = true;
            for (Setting set : settings.values()) {
                if (!first) {
                    writer.write("\n");
                } else {
                    first = false;
                }
                writer.write(set.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
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
            builder.append(searchStationName);
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
}