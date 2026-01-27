package core;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

public class Settings {
    HashMap<String, Setting> settings;
    RecipeBrowser browser;

    public Settings() {
        settings = new HashMap<>();
    }

    public Settings(Setting setting) {
        this();
        add(setting);
    }

    public void addBrowser(RecipeBrowser browser) {
        this.browser = browser;
    }

    public void add(Setting setting) {
        this.settings.put(setting.topic, setting);
    }

    public void writeSettings() {
        try (FileWriter writer = new FileWriter(RecipeBrowser.FACTORY_FOLDER + browser.factory)) {
            boolean first = true;
            for (Setting setting : settings.values()) {
                if (!first) {
                    writer.write("\n");
                } else {
                    first = false;
                }
                writer.write(setting.toString());
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void updateSetting(Setting setting) {
        Setting old_setting = settings.get(setting.topic);
        add(setting);
        if (checkCycle()) {
            add(old_setting);
            throw new CycleException(setting.topic);
        }
        writeSettings();
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

    private String getRecipeOrEmpty(String setting_name) {
        if (has(setting_name)) {
            return settings.get(setting_name).value;
        }
        return "";
    }

    public boolean has(String setting_name) {
        return settings.containsKey(setting_name);
    }

    public Setting get(String setting_name) {
        return settings.get(setting_name);
    }

    public String toString() {
        StringBuilder string = new StringBuilder();
        ArrayList<Setting> sorted = new ArrayList<>(settings.values());
        Collections.sort(sorted, Comparator.comparing(Setting::getTopic));
        for (Setting setting : sorted) {
            string.append(setting + "\n");
        }
        return string.toString();
    }

    public Collection<String> topics() {
        return settings.keySet();
    }

}
