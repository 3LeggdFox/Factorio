import java.io.File;
import java.io.FileWriter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;

public class FactoryQuery extends Query {
    String keyword;
    String target;
    String destination;
    boolean using_template = false;

    public FactoryQuery(String keyword, String target) {
        this.keyword = keyword;
        this.target = target;
    }

    public void query(RecipeBrowser browser) {
        try {
            queryFactory(browser);
        } catch (IOException e) {
            if (e instanceof FileAlreadyExistsException) {
                String name = destination;
                if (name == null) {
                    name = target;
                }
                System.err.println("Error: Factory name '" + name + "' already exists.");
            } else {
                e.printStackTrace();
                System.exit(1);
            }
        }
    }

    public void queryFactory(RecipeBrowser browser) throws IOException {
        if (keyword == null) {
            int start = browser.factory.indexOf('/') + 1;
            int end = browser.factory.lastIndexOf('.');
            System.out.println(browser.factory.substring(start, end));
            return;
        }
        if (keyword.equals("copy") || keyword.equals("rename")) {
            destination = target;
            int start = browser.factory.indexOf('/') + 1;
            int end = browser.factory.lastIndexOf('.');
            target = browser.factory.substring(start, end);
        }
        if (keyword.equals("new")) {
            File directory = new File("factoryTemplates");
            File[] templates = directory.listFiles();
            ArrayList<String> file_names = new ArrayList<>();
            file_names.add("None.");
            for (File template : templates) {
                String file_name = template.getName();
                int end = file_name.lastIndexOf('.');
                file_names.add(file_name.substring(0, end));
            }
            int counter = browser.giveOptions("Choose template: ", file_names, null);
            int choice = browser.getUserInt(0, counter);
            if (choice != 0) {
                destination = target;
                target = file_names.get(choice);
                keyword = "copy";
                using_template = true;
            }
        }
        if (keyword.equals("list")) {
            if (target != null) {
                if (target.equals("templates")) {
                using_template = true;
                } else {
                    System.err.println("Error: Unexpected word after 'factory list'.");
                    return;
                }
            }
        }
        File src;
        File dest;
        switch (keyword) {
            case "new":
                File file = new File("factories/" + target + ".txt");
                if (file.createNewFile()) {
                    System.out.println("New factory '" + target + "' was created.");
                    try (FileWriter writer = new FileWriter("factories/" + target + ".txt")) {
                        writer.write("Assembly1 = yes");
                        writer.close();
                    }
                } else {
                    System.err.println("Error: Factory name '" + target + "' already exists.");
                }
                break;
            case "change":
                try (FileWriter writer = new FileWriter("config.txt")) {
                    int end = browser.factory.lastIndexOf('.');
                    String original = browser.factory.substring(0, end);
                    String new_factory = target + ".txt";
                    browser.newFactory(new_factory);
                    writer.write(new_factory);
                    System.out.println("Changed from factory '" + original + "' to '" + target + "'.");
                }
                break;
            case "rename":
                try (FileWriter writer = new FileWriter("config.txt")) {
                    src = new File("factories/" + target + ".txt");
                    dest = new File("factories/" + destination + ".txt");
                    if (src.renameTo(dest)) {
                        System.out.println("Factory renamed from '" + target + "' to '" + destination + "'.");
                    } else {
                        System.err.println("Error: Factory name '" + target + "' already exists.");
                        return;
                    }
                    String to_write = destination + ".txt";
                    browser.factory = to_write;
                    writer.write(to_write);
                }
                break;
            case "copy":
                if (using_template) {
                    src = new File("factoryTemplates/" + target + ".txt");
                } else {
                    src = new File("factories/" + target + ".txt");
                }
                dest = new File("factories/" + destination + ".txt");
                if (!src.exists()) {
                    System.err.println("Error: Factory '" + target + "' not found.");
                    return;
                }
                Files.copy(src.toPath(), dest.toPath());
                System.out.println("Copied from factory '" + target + "' to '" + destination + "'.");
                break;
            case "list":
                File directory;
                if (using_template) {
                    directory = new File("factoryTemplates");
                } else {
                    directory = new File("factories");
                }
                File[] factories = directory.listFiles();
                for (File factory : factories) {
                    String name = factory.getName();
                    int end = name.lastIndexOf('.');
                    System.out.println(name.substring(0, end));
                }
                break;
            case "template":
                src = new File("factories/" + browser.factory);
                dest = new File("factoryTemplates/" + target + ".txt");
                Files.copy(src.toPath(), dest.toPath());
                int end = browser.factory.lastIndexOf('.');
                System.out.println("Factory '" + browser.factory.substring(0, end) + "' made into template '" + target + "'.");
                break;
        }
    }
}