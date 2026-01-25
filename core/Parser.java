package core;

import java.util.ArrayList;

/**
 * Parser
 * Class for parsing text files line by line
 * 
 * @version 1.0
 */
public class Parser {
    int position = 0; // Records where in the line is currently being parsed
    String line;

    /**
     * Constructor
     * 
     * @param line The line which will be parsed
     */
    private Parser(String line) {
        this.line = line;
    }

    /**
     * Parse a line of the recipe format
     * 
     * @param line The recipe line to be parsed
     * @return Recipe object containing the information from the parsed line
     */
    public static Recipe parseRecipe(String line, int line_number) {
        /*
         * Format:
         * <number_of_output1> <output1>[, <number_of_output2> <output2>[...]] =
         * <number_of_input1> <input1>[, <number_of_output2> <output2>[...]]. Takes
         * <crafting_time> seconds[, needs <RequiredStation1>[,
         * <RequiredStation2>[...]]]/[, can use <OptionalStation1>[,
         * <OptionalStation2>[...]]][. Uses productivity modules]
         */

        Parser parser = new Parser(line);

        // Collect Outputs
        ArrayList<Material> outputs = new ArrayList<Material>();
        String material;
        double quantity;
        String alt_name = "default"; // defaults to "default" if no alternative name found
        ArrayList<String> stations = new ArrayList<String>();
        boolean has_req = false; // defaults to no requirements if no required stations found
        boolean can_prod = false; // defaults to not using productivity modules
        do { // Must have at least one output
            quantity = parser.getNumber();
            material = parser.getWord();
            outputs.add(new Material(quantity, material));
            if (parser.tryEat('(')) { // Checks for alternative recipe name
                String name = parser.getWord();
                if (!alt_name.equals("default")) {
                    if (!alt_name.equals(name)) { // Checks if different alternative recipe names are used for single
                                                  // recipe
                        throw new ParsingException("Error: Alternate Recipe name not consistent.", line,
                                parser.position);
                    }
                }
                alt_name = name;
                parser.eat(')');
            }
        } while (parser.tryEat(',')); // Collects all outputs

        if (outputs.isEmpty()) { // Check for no outputs
            throw new ParsingException("Error: No outputs found. Impossible Recipe.", line, parser.position);
        }

        // Collect Inputs
        parser.eat('=');
        ArrayList<Material> inputs = new ArrayList<Material>();
        do { // Must have at least one input
            quantity = parser.getNumber();
            material = parser.getWord();
            inputs.add(new Material(quantity, material));
        } while (parser.tryEat(',')); // Collects all inputs

        if (inputs.isEmpty()) // Check for no inputs
        {
            throw new ParsingException("Error: No inputs found. Impossible Recipe.", line, parser.position);
        }

        // Collect Crafting Time
        parser.eat('.');
        parser.eatWord("Takes");
        double crafting_time = parser.getNumber();
        parser.eatWord("seconds");
        if (parser.tryEat(',')) { // Checks if there are station requirements/options
            // Collect Alternate Stations
            if (parser.tryEatWord("can")) {
                parser.eatWord("use");
                do {
                    stations.add(parser.getWord());
                } while (parser.tryEat(',')); // Collect all alternatives
            }

            // Collect Required Stations
            if (parser.tryEatWord("needs")) {
                has_req = true;
                do {
                    stations.add(parser.getWord());
                } while (parser.tryEat(',')); // Collect all required stations
            }

            // Check if can use productivity modules
            if (parser.tryEat('.')) {
                parser.eatWord("Uses");
                parser.eatWord("productivity");
                parser.eatWord("modules");
                can_prod = true;
            }
        }

        if (!has_req) { // Adds the assembly machines as a default
            stations.add("Assembly1");
            stations.add("Assembly2");
            stations.add("Assembly3");
        }

        return new Recipe(inputs, outputs, stations, crafting_time, has_req, can_prod, alt_name, line_number);
    }

    /**
     * Parse a line of the setting format
     * 
     * @param line The setting line to parsed
     * @return Setting object containing the information from the parsed line
     */
    public static Setting parseSettings(String line) {
        /* Format: <setting_topic> = <value> */
        Parser parser = new Parser(line);
        String topic = parser.getWord();
        parser.eat('=');
        String setting = parser.getNext();
        return new Setting(topic, setting);
    }

    /**
     * Parse a line of the station format
     * 
     * @param line The station line to be parsed
     * @return Station object containing the information from the parsed line
     */
    public static Station parseStations(String line) {
        /*
         * Format:
         * <StationName>: <module_slots> modules, <base_productivity>% productivity,
         * <crafting_speed> crafting speed, <priority> prio
         */
        Parser parser = new Parser(line);
        String station_name = parser.getWord();
        parser.eat(':');
        int modules = (int) (parser.getNumber(true) + 0.5);
        parser.eatWord("modules");
        parser.eat(',');
        double productivity_bonus = parser.getNumber();
        parser.eat('%');
        parser.eatWord("productivity");
        parser.eat(',');
        double crafting_speed = parser.getNumber();
        parser.eatWord("crafting");
        parser.eatWord("speed");
        parser.eat(',');
        int priority = (int) (parser.getNumber(true) + 0.5);
        parser.eatWord("prio");
        return new Station(station_name, modules, productivity_bonus, crafting_speed, priority);
    }

    /**
     * Parse a line of the query format
     * 
     * @param line           The query line to be parsed
     * @param toggle_verbose Override verbosity tag
     * @return Query object containing the information from the parsed line
     */
    public static Query parseQuery(String line, boolean toggle_verbose) {
        /*
         * Format: Varies. View help function
         * Generally:
         * [verbose] <command> <parameter1> [<paramater2> [...]]
         */
        Parser parser = new Parser(line);
        boolean verbose_key = parser.tryEatWord("verbose"); // Gets verbose key
        boolean verbose = verbose_key || toggle_verbose; // Overrides verbose if toggle_verbose is true
        String first_word = parser.tryGetWord(); // Try to get command
        Double number = 0.0;
        boolean is_empty = false;
        if (first_word == null) { // If command is not a word check if it is a number
            number = parser.tryGetNumber();
            if (number == null) { // If no command, let switch run to default
                first_word = "default";
                is_empty = true; // Flag for empty command
            } else {
                first_word = "math"; // Number command means math query
            }
        }

        String topic;
        String command;
        String material;
        int prod_mod_level;
        String output;
        switch (first_word) { // Run based on command
            case "get": // [verbose] get <material1>[/all/base] in [<amount=1>] <material2> [prod
                        // <prod_mod_level>]
                String input = parser.getWord();
                parser.eatWord("in");
                number = parser.tryGetNumber();
                if (number == null) { // Default to amount = 1
                    number = 1.0;
                }
                output = parser.getWord();
                prod_mod_level = 0;
                if (parser.tryEatWord("prod")) { // Default to prod_mod_level = 0
                    prod_mod_level = (int) (parser.getNumber(true) + 0.5); // +0.5 to avoid fp inaccuracy rounding down
                }
                parser.checkExcess();
                return new QuantInQuery(input, output, number, prod_mod_level, verbose);
            case "machines": // [verbose] machines in [<amount=1>] <material> [prod <prod_mod_level>]
                parser.eatWord("in");
                number = parser.tryGetNumber();
                if (number == null) { // Default to amount = 1
                    number = 1.0;
                }
                output = parser.getWord();
                prod_mod_level = 0;
                if (parser.tryEatWord("prod")) { // Default to prod_mod_level = 0
                    prod_mod_level = (int) (parser.getNumber(true) + 0.5); // +0.5 to avoid fp inaccuracy rounding down
                }
                parser.checkExcess();
                return new MachinesQuery(output, number, prod_mod_level, verbose);
            case "list": // list <material>[/settings]
                material = parser.getWord();
                parser.checkExcess();
                return new ListQuery(material);
            case "setting": // setting <setting_topic]
                topic = parser.getWord();
                parser.checkExcess();
                return new SettingQuery(topic, verbose);
            case "time": // [verbose] time for <material> [prod <prod_mod_level>]
                material = parser.getWord();
                parser.eatWord("for");
                prod_mod_level = 0;
                if (parser.tryEatWord("prod")) { // Default to prod_mod_level = 0
                    prod_mod_level = (int) (parser.getNumber(true) + 0.5); // +0.5 to avoid fp inaccuracy rounding down
                }
                parser.checkExcess();
                return new TimeQuery(material, prod_mod_level, verbose);
            case "update": // update <setting_topic>
                topic = parser.getWord();
                parser.checkExcess();
                return new UpdateSettingQuery(topic, verbose);
            case "search": // search <material1/setting1> [<logical_operator> <material2/setting2>]
                material = parser.getWord();
                String operator = parser.tryGetWord();
                if (operator != null && !operator.equals("or") && !operator.equals("and")) { // Ensure and/or operator
                    throw new ParsingException("Error: Expected 'and' or 'or', found '" + operator + "'.", line,
                            parser.position);
                }
                String material2 = parser.tryGetWord();
                parser.checkExcess();
                return new SearchQuery(material, operator, material2);
            case "math": // <number1> <operation> <number2>
                char operation = parser.increment();
                double number2 = parser.getNumber();
                parser.checkExcess();
                return new MathQuery(number, operation, number2);
            case "factory": // factory [<command> <target>]
                command = parser.tryGetWord();
                String target = null;
                if (command != null) {
                    if (command.equals("list")) { // list command does not require a target
                        target = parser.tryGetWord();
                    } else {
                        target = parser.getWord();
                    }
                }
                parser.checkExcess();
                return new FactoryQuery(command, target);
            case "help": // help [<command>]
                command = parser.tryGetWord();
                parser.checkExcess();
                return new HelpQuery(command);
            case "exit": // exit
                parser.checkExcess();
                System.out.println("Cheers.");
                System.exit(0);
            default:
                if (is_empty && verbose_key) {
                    return new ToggleVerboseQuery();
                }
                throw new ParsingException("Error: Command not recognised.", line, parser.position - 1);
        }
    }

    /**
     * Checks if the current position's character is part of a word
     * 
     * @return boolean: true if character is part of a word, false if not or end of
     *         line
     */
    private boolean isWord() {
        if (line.length() <= position) {
            return false;
        }
        char character = line.charAt(position);
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || character == '_'
                || isNumber(); // Includes a-z, A-Z, _, 0-9
    }

    /**
     * Checks if the current position's character is part of a number
     * 
     * @return boolean: true if character is part of a number, false if not or end
     *         of line
     */
    private boolean isNumber() {
        if (position >= line.length()) {
            return false;
        }
        char character = line.charAt(position);
        return (character >= '0' && character <= '9'); // Includes only 0-9, no decimal point
    }

    /**
     * Checks if the parser has reached the end of the string (or if whitespaces
     * only remain)
     * 
     * @return boolean: true if the parser is at the end of the string, false
     *         otherwise
     */
    private boolean isEnd() {
        trim();
        return position >= line.length();
    }

    /**
     * Checks if the parser has reached the end of the line. Throws error if it has
     * not
     * 
     */
    private void checkExcess() {
        if (!isEnd()) {
            throw new ParsingException("Error: Too many arguments.", line, position);
        }
    }

    /**
     * Grabs the next string in the line. Throws exception if no word is found
     * 
     * @return String containing the next non-whitespaces in the line
     */
    private String getWord() {
        trim(); // Removes excess whitespaces
        int initPos = position; // Tracks progress from start
        if (!isNumber()) { // Word cannot start with number
            while (isWord()) { // Go to end of word
                position++;
            }
        }
        if (initPos == position) { // Checks if word is empty
            throw new ParsingException("Error: Expected word.",
                    line, position);
        }
        return line.substring(initPos, position);
    }

    /**
     * Tries to grab the next word in the line. Does not throw exception if no word is found
     * 
     * @return String of the next non-whitespaces in the line, null if no word is found
     */
    private String tryGetWord() {
        trim(); // Removes excess whitespaces
        int initPos = position; // Tracks progress from start
        if (!isNumber()) { // Cannot start with number
            while (isWord()) { // Go to end of word
                position++;
            }
        }
        if (initPos == position) { // Checks if word is empty
            return null;
        }
        return line.substring(initPos, position);
    }

    /**
     * Grabs the next double/int in the line. Throws exception if no number is found
     * 
     * @return double of the next non-whitespaces in the line
     */
    private double getNumber() {
        return getNumber(false);
    }

    /**
     * Grabs the next double/int in the line. Throws exception if no number is found
     * @param int_only boolean controlling whether decimal point is accepted/read
     * @return double of the next non-whitespaces in the line
     */
    private double getNumber(boolean int_only) {
        trim(); // Removes whitespaces
        int initPos = position; // Tracks progress from start
        while (isNumber()) { // Goes to first decimal point/end of number
            position++;
        }
        if (!int_only) { // Checks if decimal point allowed
            if (tryEat('.')) {
                while (isNumber()) { // Gets decimal places
                    position++;
                }
            }
        }
        if (initPos == position) { // Checks if empty
            throw new ParsingException(
                    "Error: Expected number.", line,
                    position);
        }
        return Double.parseDouble(line.substring(initPos, position));
    }

    /**
     * Tries to grab the next number in the line. Does not throw exception if no number is found
     * 
     * @return Double of the next non-whitespaces in the line, null if no numbers found
     */
    private Double tryGetNumber() {
        return tryGetNumber(false);
    }

    /**
     * Tries to grab the next double/int in the line. Does not throw exception if no double/int is found
     * @param int_only boolean controlling whether decimal point is accepted/read
     * @return Double of the next non-whitespaaces in the line, null if no numbers found
     */
    private Double tryGetNumber(boolean int_only) {
        trim(); // Removes whitespaces
        int initPos = position; // Tracks progress from start
        while (isNumber()) { // Goes to first decimal point/end of number
            position++;
        }
        if (!int_only) { // Checks if decimal point allowed
            if (tryEat('.')) {
                while (isNumber()) { // Gets decimal places
                    position++;
                }
            }
        }
        if (initPos == position) { // Checks if empty
            return null;
        }
        return Double.parseDouble(line.substring(initPos, position));
    }

    private String getNext() {
        trim();
        int initPos = position;
        while (isWord() || isNumber()) {
            position++;
        }
        if (initPos == position) {
            throw new ParsingException(
                    "Error: Either finding word/number at end of String, or finding word/number on non-word/number char.",
                    line, position);
        }
        return line.substring(initPos, position);
    }

    private void trim() {
        while (line.length() > position && line.charAt(position) == ' ') {
            position++;
        }
        return;
    }

    private void eat(char expected) {
        trim();
        if (position >= line.length()) {
            throw new ParsingException("Error: Expected '" + expected + "', found end of string.", line, position);
        }
        if (expected != line.charAt(position++)) {
            throw new ParsingException("Error: Expected '" + expected + "', found '" + line.charAt(position - 1) + "'.",
                    line, position - 1);
        }
        return;
    }

    private boolean tryEat(char expected) {
        trim();
        if (position == line.length()) {
            return false;
        }
        boolean result = expected == line.charAt(position);
        if (result) {
            position++;
        }
        return result;
    }

    private void eatWord(String expected) {
        String word = getWord();
        if (!expected.equals(word)) {
            throw new ParsingException("Error: Expected '" + expected + "' found '" + word + "'.", line, position - 1);
        }
    }

    private char increment() {
        trim();
        if (position >= line.length()) {
            throw new ParsingException("Error: Expected character.", line, position);
        }
        return line.charAt(position++);
    }

    private boolean tryEatWord(String expected) {
        trim();
        int initPos = position;
        if (!isNumber()) {
            while (isWord()) {
                position++;
            }
        }
        if (initPos == position) {
            return false;
        }
        position = initPos;
        String word = null;
        try {
            word = getWord();
        } catch (ParsingException e) {
            System.out.println("You done fucked up m8.");
        }
        boolean result = expected.equals(word);
        if (!result) {
            position = initPos;
        }
        return result;
    }
}

class ParsingException extends QueryException {
    String offender;
    int position;

    public ParsingException(String message, String offender, int position) {
        super(message);
        this.offender = offender;
        this.position = position;
    }

    public String getMessage() {
        StringBuilder builder = new StringBuilder(super.getMessage());
        builder.append("\n" + offender + "\n");
        String positionString = "";
        if (position >= 0) {
            positionString = " ".repeat(position) + "^";
        }
        builder.append(positionString);
        return builder.toString();
    }
}