package uk.org.thehickses.countdown.web;

import freemarker.template.Configuration;

public class TemplateConfig
{
    private static Configuration CONFIG = null;

    public static synchronized Configuration getConfiguration()
    {
        if (CONFIG == null)
        {
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);
            cfg.setClassLoaderForTemplateLoading(TemplateConfig.class.getClassLoader(),
                    "templates");
            CONFIG = cfg;
        }
        return CONFIG;
    }
}
