package core;

import java.util.ArrayList;
import java.lang.StringBuilder;

/**
 * Recipe
 * Class which handles the details of an individual recipe
 * 
 * @version 1.0
 */
public class Recipe {
    ArrayList<Material> inputs = new ArrayList<Material>();
    ArrayList<Material> outputs = new ArrayList<Material>();
    ArrayList<String> stations = new ArrayList<String>();
    double crafting_time;
    boolean has_req;
    boolean can_prod;
    String alt_name;
    boolean has_cycle = false; // Whether or not the recipe has a cycle (output is also input)

    /**
     * Constructor
     * 
     * @param inputs        List of Material inputs
     * @param outputs       List of Material outputs
     * @param stations      List of Stations which can be used
     * @param crafting_time Time taken for recipe completion with crafting speed 1
     * @param has_req       Whether the recipe has a station requirement
     * @param can_prod      Whether productivity modules can be used
     * @param alt_name      Identifying name for the recipe
     */
    public Recipe(ArrayList<Material> inputs, ArrayList<Material> outputs, ArrayList<String> stations,
            double crafting_time, boolean has_req, boolean can_prod, String alt_name) {
        this.inputs = inputs;
        this.outputs = outputs;
        this.stations = stations;
        this.crafting_time = crafting_time;
        this.has_req = has_req;
        this.can_prod = can_prod;
        this.alt_name = alt_name;
        for (Material material : inputs) { // Checks whether any of the inputs are also outputs
            if (hasOutput(material.name)) {
                this.has_cycle = true;
                return;
            }
        }
    }

    /**
     * Gets the amount of a given material is output by this recipe
     * 
     * @param material The material whose output quantity is being checked
     * @return double containing the amount of the material which is output
     */
    public double amountOutput(String material) {
        for (Material output : this.outputs) {
            if (output.name.equals(material)) {
                return output.quantity;
            }
        }
        return 0; // Returns 0 if not found
    }

    /**
     * Checks whether a given material is output by a recipe
     * 
     * @param material The material being queried
     * @return boolean, true if the recipe outputs the material, false otherwise
     */
    public boolean hasOutput(String material) {
        return amountOutput(material) != 0;
    }

    /**
     * Gets the amount of a given material is input to this recipe
     * 
     * @param material The material whose input quantity is being checked
     * @return double containing the amount of the material which is input
     */
    public double amountInput(String material) {
        for (Material input : this.inputs) {
            if (input.name.equals(material)) {
                return input.quantity;
            }
        }
        return 0; // Returns 0 if not found
    }

    /**
     * Checks whether a given material is input to a recipe
     * 
     * @param material
     * @return
     */
    public boolean hasInput(String material) {
        return amountInput(material) != 0;
    }

    /**
     * Checks whether the recipe can use the given station
     * 
     * @param station The station being searched for in the recipe
     * @return boolean, true if the recipe can use the station, false otherwise
     */
    public boolean hasStation(String station) {
        for (String stat : stations) {
            if (stat.equals(station)) {
                return true;
            }
        }
        return false; // Returns false if not found
    }

    /**
     * Creates a deep copy of this Recipe and removes any cycle
     * 
     * @return Deep copy of this Recipe with no cycle
     */
    public Recipe getNoCycleClone() {
        ArrayList<Material> new_inputs = new ArrayList<>();
        ArrayList<Material> new_outputs = new ArrayList<>();
        ArrayList<String> new_stations = new ArrayList<>();
        for (Material output : outputs) { // Copies all outputs
            Material new_output = new Material(output.quantity, output.name);
            if (hasInput(output.name)) { // Subtracts any input of the same resource
                new_output.quantity -= amountInput(output.name);
            }
            new_outputs.add(new_output);
        }
        for (Material input : inputs) { // Copies all inputs
            if (!hasOutput(input.name)) { // Skips inputs already in output
                Material new_input = new Material(input.quantity, input.name);
                new_inputs.add(new_input);
            }
        }
        for (String station : stations) { // Copies all stations
            new_stations.add(station);
        }
        return new Recipe(new_inputs, new_outputs, new_stations, crafting_time, has_req, can_prod, alt_name);
    }

    /**
     * Grabs a list of all ingredients and products involved in the Recipe
     * 
     * @return ArrayList of Materials involved in the Recipe
     */
    public ArrayList<Material> getMaterials() {
        ArrayList<Material> inputs_copy = new ArrayList<>(inputs);
        inputs_copy.addAll(outputs);
        return inputs_copy;
    }

    /**
     * Generates a String representation of the Recipe
     * 
     * @returns String representation of the Recipe
     */
    public String toString() {
        // Inputs, Outputs, and Crafting Time
        StringBuilder string = new StringBuilder(buildInputOutputString()); // Calls helper function

        // All Usable Stations
        boolean first = true; // Flag for first station
        for (String station : this.stations) {
            if (!has_req) { // Ommits the assembly machines being listed if there are no required stations
                if (station.equals("Assembly1") || station.equals("Assembly2") || station.equals("Assembly3")) {
                    continue;
                }
            }
            if (first) { // Says whether following stations are optional or required
                string.append(", ");
                if (has_req) {
                    string.append("needs ");
                } else {
                    string.append("can use ");
                }
                string.append(station);
                first = false;
            } else {
                string.append(", " + station);
            }
        }

        // Productivity tag
        if (can_prod) {
            string.append(". Uses productivity modules");
        }
        return string.toString();
    }

    /**
     * Helper function for the beginning of the toString() method
     * 
     * @return String containing the inputs, the outputs and the crafting time of
     *         the recipe
     */
    private String buildInputOutputString() {
        return buildInputOutputString(true);
    }

    /**
     * Helper function for the beginning of the toString() method
     * 
     * @param show_alt_name boolean controlling whether the recipe-specific name is
     *                      included
     * @return String containing the inputs, the outputs and the crafting time of
     *         the recipe
     */
    private String buildInputOutputString(boolean show_alt_name) {
        StringBuilder string = new StringBuilder();
        boolean first = true;
        for (Material output : this.outputs) { // Lists all outputs
            if (first) {
                string.append(output.toString());
                first = false;
            } else {
                string.append(", " + output.toString());
            }
            if (!alt_name.equals("default") && show_alt_name) { // Appends recipe-specific name if including them
                string.append("(" + alt_name + ")");
            }
        }

        string.append(" = ");

        first = true;
        for (Material input : this.inputs) { // Lists all outputs
            if (first) {
                string.append(input.toString());
                first = false;
            } else {
                string.append(", " + input.toString());
            }
        }

        // Crafting Time
        string.append(". Takes ");
        string.append(crafting_time);
        string.append(" seconds");
        return string.toString();
    }

    /**
     * Gets a simple version of the recipe specifying which station is being used
     * 
     * @param station Station being used
     * @return String containing a simple version of the recipe using a specific
     *         Station
     */
    public String toStringSpecific(Station station) {
        return buildInputOutputString(false) + ", using " + station.name + ".";
    }

    /**
     * Gets the recipe with the given station and adds the total productivity %,
     * crafting time and amount of each input
     * 
     * @param station        Station being used
     * @param times          Number of times the recipe is being completed
     * @param prod_mod_level Level of productivity module being applied (if
     *                       possible)
     * @return String containing simple version of the recipe using a specific
     *         Station and the productivity % and crafting speed
     */
    public String toStringSpecificVerbose(Station station, double times, int prod_mod_level) {
        StringBuilder string = new StringBuilder("Using Recipe: ");
        string.append(toStringSpecific(station)); // Append inputs, outputs, crafting speed and Station
        int percentage;
        if (!can_prod) { // Sets module level to 0 if productivity modules cannot be used
            prod_mod_level = 0;
        }
        double productivity = station.getProd(prod_mod_level); // Retrieves station productivity and adds module effects
        percentage = (int) (productivity * 100 - 100 + 0.5); // Converts to percentage
        string.append("\nProductivity: " + percentage + "%");
        double craft_speed = station.getSpeed(prod_mod_level);
        string.append(String.format(", Crafting Speed: %.3f.", craft_speed));
        boolean first_ingredient = true;
        if (times != 0) {
            for (Material material : inputs) {
                if (!first_ingredient) {
                    string.append(", ");
                } else {
                    string.append("\nInputs: ");
                    first_ingredient = false;
                }
                string.append(String.format("%.3f %s", material.quantity * times, material.name));
            }
        }
        return string.toString();
    }

    /**
     * Gets the crafting time for this station using a given Station and
     * productivity module level
     * 
     * @param station        Station being used
     * @param prod_mod_level Level of productivity module being applied (if
     *                       possible)
     * @return double containing the time to complete the recipe
     */
    public double getCraftingTime(Station station, int prod_mod_level) {
        if (!can_prod) { // Sets module level to 0 if productivity modules cannot be used
            prod_mod_level = 0;
        }
        return crafting_time / station.getSpeed(prod_mod_level);
    }
}