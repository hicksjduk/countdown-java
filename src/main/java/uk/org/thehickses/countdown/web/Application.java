package uk.org.thehickses.countdown.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application
{
    // private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args)
    {
        try
        {
            SpringApplication.run(Application.class, args);
        }
        catch (Throwable ex)
        {
            ex.printStackTrace();
        }
    }

}
