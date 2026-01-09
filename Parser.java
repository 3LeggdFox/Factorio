import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    int position;
    String line;

    private Parser(String line) {
        this(0, line);
    }

    private Parser(int position, String line) {
        this.position = position;
        this.line = line;
    }

    public static Recipe parseRecipe(String line) throws ParsingException
    {
        Parser parser = new Parser(line);

        // Collect Outputs
        ArrayList<Material> outputs = new ArrayList<Material>();
        String material;
        double quantity;
        String alt_name = "default";
        ArrayList<String> stations = new ArrayList<String>();
        boolean has_req = false;
        boolean can_prod = true;
        do     
        {
            quantity = parser.getNumber();
            material = parser.getWord();
            outputs.add(new Material(quantity, material));
            if (parser.tryEat('('))
            {
                alt_name = parser.getWord();
                parser.eat(')');
            }
        } while (parser.tryEat(','));

        if (outputs.isEmpty()) // Check for syntax errors
        {
            throw new ParsingException("Error: No outputs found. Impossible Recipe.", line, parser.position);
        }

        // Collect Inputs
        parser.eat('=');
        ArrayList<Material> inputs = new ArrayList<Material>();
        do      
        {
            quantity = parser.getNumber();
            material = parser.getWord();
            inputs.add(new Material(quantity, material));
        } while (parser.tryEat(','));

        if (inputs.isEmpty()) // Check for syntax errors
        {
            throw new ParsingException("Error: No inputs found. Impossible Recipe.", line, parser.position);
        }

        // Collect Crafting Time
        parser.eat('.');
        parser.eatWord("Takes");
        double crafting_time = parser.getNumber();
        parser.eatWord("seconds");
        if (parser.tryEat(','))
        {
            // Collect Alternate Stations
            if (parser.tryEatWord("can"))
            {
                parser.eatWord("use");
                do 
                {
                    stations.add(parser.getWord());
                } while (parser.tryEat(','));
            }

            // Collect Required Stations
            if (parser.tryEatWord("needs"))
            {
                has_req = true;
                do 
                {
                    stations.add(parser.getWord());
                } while (parser.tryEat(','));
            }
            
            // Check if can use productivity modules
            if (parser.tryEatWord("no"))
            {
                parser.eatWord("productivity");
                parser.eatWord("modules");
                can_prod = false;
            }
        }

        if (!has_req)
        {
            stations.add("Assembly1");
            stations.add("Assembly2");
            stations.add("Assembly3");
        }

        return new Recipe(inputs, outputs, stations, crafting_time, has_req, can_prod, alt_name);
    }

    public static Setting parseSettings(String line) throws ParsingException {
        Parser parser = new Parser(line);
        String topic = parser.getWord();
        parser.eat('=');
        String setting = parser.getNext();
        return new Setting(topic, setting);
    }

    public static Station parseStations(String line) throws ParsingException {
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

    public static Query parseQuery(String line, HashMap<String, Integer> allMaterials) throws ParsingException {
        Parser parser = new Parser(line);
        boolean verbose = parser.tryEatWord("verbose");
        String firstWord = parser.getWord();
        String topic;
        switch (firstWord) {
            case "get":
                String input = parser.getWord();
                parser.eatWord("in");
                String output = parser.getWord();
                int prod_mod_level = 0;
                if (parser.tryEatWord("prod"))
                {
                    prod_mod_level = (int) (parser.getNumber(true) + 0.5);
                }
                return new QuantInQuery(input, output, prod_mod_level, verbose);
            case "list":
                String material = parser.getWord();
                return new ListQuery(material, verbose);
            case "setting":
                topic = parser.getWord();
                return new SettingQuery(topic, verbose);
            case "update":
                topic = parser.getWord();
                parser.eat('=');
                String setting = parser.getWord();
                return new UpdateSettingQuery(topic, setting, verbose);
            case "help":
                return new HelpQuery(verbose);
            case "exit":
                System.out.println("Cheers.");
                System.exit(0);
            default:
                throw new ParsingException("Error: Command not recognised.", line, parser.position - 1);
        }
    }

    private boolean isWord() {
        if (line.length() <= position) {
            return false;
        }
        char character = line.charAt(position);
        return (character >= 'a' && character <= 'z') || (character >= 'A' && character <= 'Z') || character == '_'
                || isNumber();
    }

    private boolean isNumber() {
        if (line.length() <= position) {
            return false;
        }
        char character = line.charAt(position);
        return (character >= '0' && character <= '9');
    }

    private String getWord() throws ParsingException {
        trim();
        int initPos = position;
        while (isWord()) {
            position++;
        }
        if (initPos == position) {
            throw new ParsingException("Error: Expected word.",
                    line, position);
        }
        return line.substring(initPos, position);
    }

    private double getNumber() throws ParsingException
    {
        return getNumber(false);
    }

    private double getNumber(boolean int_only) throws ParsingException {
        trim();
        int initPos = position;
        while (isNumber()) {
            position++;
        }
        if (!int_only)
        {
            if (tryEat('.'))
            {
                while (isNumber())
                {
                    position++;
                }
            }
        }
        if (initPos == position) {
            throw new ParsingException(
                    "Error: Expected number.", line,
                    position);
        }
        return Double.parseDouble(line.substring(initPos, position));
    }

    private String getNext() throws ParsingException {
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

    private void eat(char expected) throws ParsingException {
        trim();
        if (position >= line.length()) {
            throw new ParsingException("Error: Expected '" + expected + "' found end of string.", line, position);
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
        if (result)
        {
            position++;
        }
        return result;
    }

    private void eatWord(String expected) throws ParsingException {
        String word = getWord();
        if (!expected.equals(word)) {
            throw new ParsingException("Error: Expected '" + expected + "' found '" + word + "'.", line, position - 1);
        }
    }

    private boolean tryEatWord(String expected) {
        trim();
        int initPos = position;
        while (isWord()) {
            position++;
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
        if (!result)
        {
            position = initPos;
        }
        return result;
    }
}

class ParsingException extends Exception {
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
        String positionString = " ".repeat(position) + "^";
        builder.append(positionString);
        return builder.toString();
    }
}