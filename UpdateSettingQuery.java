public class UpdateSettingQuery extends Query {
    Setting new_setting;

    public UpdateSettingQuery(String topic, String setting, boolean verbose) {
        this.new_setting = new Setting(topic, setting);
        this.verbose = verbose;
    }

    public void query(RecipeBrowser browser) {
        browser.changeSetting(new_setting);
    }
}
