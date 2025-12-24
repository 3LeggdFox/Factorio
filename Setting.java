public class Setting 
{
    String topic;
    String setting;
    
    public Setting(String topic, String setting)
    {
        this.topic = topic;
        this.setting = setting;
    }

    public String toString()
    {
        return topic + " = " + setting;
    }
}
