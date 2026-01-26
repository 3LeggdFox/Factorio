package core;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;

/**
 * FactoryQuery
 * Class to manage queries about the factory setting file(s)
 * 
 * @version 1.0
 */
public class FactoryQuery extends Query {
    String keyword;
    String target;
    String destination;
    boolean using_template = false;

    /**
     * Constructor
     * 
     * @param keyword The specific command relating to factories which will be
     *                executed
     * @param target  The target of the specified command
     */
    public FactoryQuery(String keyword, String target) {
        this.keyword = keyword;
        this.target = target;
    }

    /**
     * Wrapper method for queryFactory which performs the specified command
     * 
     * @param browser The RecipeBrowser object storing all relevant factory and file
     *                information
     */
    public void query(RecipeBrowser browser) {
        try { // try-catch to catch all IOExceptions thrown inside queryFactory
            queryFactory(browser);
        } catch (IOException e) {
            if (e instanceof FileAlreadyExistsException) { // Don't crash if file already exists
                String name = destination;
                if (name == null) {
                    name = target;
                }
                throw new QueryException("Error: Factory name '" + name + "' already exists.");
            } else {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    /**
     * Method which performs the specified command
     * 
     * @param browser The RecipeBrowser object storing all relevant factory and file
     *                information
     * @throws IOException
     */
    public void queryFactory(RecipeBrowser browser) throws IOException {
        // Prints the current factory if no command is given
        if (keyword == null) {
            int start = browser.factory.indexOf('/') + 1; // Removes directories from factory name
            int end = browser.factory.lastIndexOf('.'); // Removes extension from factory name
            System.out.println(browser.factory.substring(start, end));
            return;
        }
        // Uses target as destination and chooses the target to be the current factory
        if (keyword.equals("copy") || keyword.equals("rename")) {
            destination = target;
            int start = browser.factory.indexOf('/') + 1; // Removes directories from factory name
            int end = browser.factory.lastIndexOf('.'); // Removes extension from factory name
            target = browser.factory.substring(start, end);
        }
        // Asks user if they would like to use a template. Sets mode to copy if they
        // choose one
        if (keyword.equals("new")) {
            File directory = new File("factoryTemplates");
            File[] templates = directory.listFiles();
            ArrayList<String> file_names = new ArrayList<>(); // Gets all templates
            file_names.add("None."); // Adds option to not use template
            for (File template : templates) { // Prints all options
                String file_name = template.getName();
                int end = file_name.lastIndexOf('.'); // Removes file extension
                file_names.add(file_name.substring(0, end));
            }
            int counter = browser.giveOptions("Choose template: ", file_names, null);
            int choice = browser.getUserInt(0, counter);
            if (choice != 0) { // choice == 0 for no template
                destination = target;
                target = file_names.get(choice);
                keyword = "copy";
                using_template = true; // Flag to use template folder
            }
        }
        // Checks if 'list templates' was called or if just 'list' was called
        if (keyword.equals("list")) {
            if (target != null) { // can just call 'factory list'
                if (target.equals("templates")) {
                    using_template = true; // Flag to use template folder
                } else { // Case where called 'factory list *word*' where word != templates
                    throw new QueryException("Error: Unexpected word after 'factory list'.");
                }
            }
        }
        File src;
        File dest;
        switch (keyword) {
            case "new":
                // Creates new file
                File file = new File(RecipeBrowser.FACTORY_FOLDER + target + ".txt");
                if (file.createNewFile()) { // Returns true if successful
                    System.out.println("New factory '" + target + "' was created.");
                    try (FileWriter writer = new FileWriter(RecipeBrowser.FACTORY_FOLDER + target + ".txt")) {
                        writer.write("Assembly1 = yes"); // Writes a setting to ensure appending does not leave blank
                                                         // first line
                        writer.close();
                    }
                } else {
                    System.err.println("Error: Factory name '" + target + "' already exists.");
                }
                /* falls through */
            case "change":
                // Changes from current factory and sets startup factory to the targe factory
                try (FileWriter writer = new FileWriter(RecipeBrowser.CONFIG_FILE)) {
                    int end = browser.factory.lastIndexOf('.'); // Removes extension
                    String original = browser.factory.substring(0, end); // Stores original factory
                    String new_factory = target + ".txt";
                    browser.newFactory(new_factory);
                    writer.write(new_factory); // Overwrites new factory to start process on
                    System.out.println("Changed from factory '" + original + "' to '" + target + "'.");
                }
                break;
            case "rename":
                // Renames the current factory to the given name and updates config file
                try (FileWriter writer = new FileWriter("config.txt")) {
                    src = new File(RecipeBrowser.FACTORY_FOLDER + target + ".txt"); // Current file name
                    dest = new File(RecipeBrowser.FACTORY_FOLDER + destination + ".txt"); // New file name
                    if (src.renameTo(dest)) { // Returns true if successful
                        System.out.println("Factory renamed from '" + target + "' to '" + destination + "'.");
                    } else {
                        System.err.println("Error: Factory name '" + target + "' already exists.");
                        return;
                    }
                    String to_write = destination + ".txt";
                    browser.factory = to_write;
                    writer.write(to_write); // Update config file
                }
                break;
            case "copy":
                // Copies a target file to a given destination file name
                if (using_template) { // Opens template folder
                    src = new File(RecipeBrowser.TEMPLATE_FOLDER + target + ".txt");
                } else { // Otherwise source is itself
                    src = new File(RecipeBrowser.FACTORY_FOLDER + target + ".txt");
                }
                dest = new File(RecipeBrowser.FACTORY_FOLDER + destination + ".txt");
                if (!src.exists()) {
                    System.err.println("Error: Factory '" + target + "' not found.");
                    return;
                }
                Files.copy(src.toPath(), dest.toPath());
                System.out.println("Copied from factory '" + target + "' to '" + destination + "'.");
                break;
            case "list":
                // Lists the factories or the templates depending on input
                File directory;
                if (using_template) { // Grabs template directory contents
                    directory = new File("factoryTemplates");
                } else { // Grabs factories directery contents
                    directory = new File("factories");
                }
                File[] factories = directory.listFiles();
                for (File factory : factories) { // Lists contents
                    String name = factory.getName();
                    int end = name.lastIndexOf('.');
                    System.out.println(name.substring(0, end));
                }
                break;
            case "template":
                // Copies the current factory into the template folder under the given name
                src = new File(RecipeBrowser.FACTORY_FOLDER + browser.factory);
                dest = new File(RecipeBrowser.TEMPLATE_FOLDER + target + ".txt");
                Files.copy(src.toPath(), dest.toPath());
                int end = browser.factory.lastIndexOf('.');
                System.out.println(
                        "Factory '" + browser.factory.substring(0, end) + "' made into template '" + target + "'.");
                break;
        }
    }
}