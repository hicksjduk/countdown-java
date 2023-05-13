package uk.org.thehickses.countdown.web;

import java.io.StringWriter;

import freemarker.template.Configuration;

public class Templater
{
    private final Configuration cfg = new Configuration(Configuration.VERSION_2_3_31);

    public Templater(String templateDirectory)
    {
        cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), templateDirectory);
    }

    public String applyTemplate(String templateName, Object model) throws Exception
    {
        var sw = new StringWriter();
        cfg.getTemplate(templateName)
                .process(model, sw);
        return sw.toString();
    }
}
