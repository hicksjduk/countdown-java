package uk.org.thehickses.countdown.web;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import freemarker.template.Template;
import uk.org.thehickses.countdown.Solver;
import uk.org.thehickses.countdown.Solver.Expression;

@RestController
public class Controller
{
    @RequestMapping(path = "/", method = RequestMethod.GET)
    public String home() throws Exception
    {
        return outputPage(new Model());
    }

    @RequestMapping(path = "/", method = RequestMethod.POST)
    public String home(HttpServletRequest req) throws Exception
    {
        Model model = new Model();
        String numbers = req.getParameter("numbers");
        try
        {
            String[] args = numbers.matches("\\s*") ? new String[0] : numbers.split("\\s+");
            Solver solver = Solver.instance(args);
            model.addMessage(String.format("Target: %d, numbers: %s", solver.getTarget(),
                    Arrays.toString(solver.getNumbers())));
            Expression solution = solver.solve();
            if (solution == null)
                model.addMessage("No solution found");
            else
                model.addMessage(String.format("%s = %d", solution, solution.value));
            return outputPage(model);
        }
        catch (IllegalArgumentException ex)
        {
            model.setInput(numbers)
                    .addMessage(ex.getMessage());
            return outputPage(model);
        }
    }

    private String outputPage(Model model) throws Exception
    {
        try (StringWriter sw = new StringWriter())
        {
            template("index.ftlh").process(model, sw);
            return sw.toString();
        }
    }

    private Template template(String file) throws Exception
    {
        return TemplateConfig.getConfiguration()
                .getTemplate(file);
    }

    public static class Model
    {
        private String input;
        private final List<String> messages = new ArrayList<>();

        public List<String> getMessages()
        {
            return messages;
        }

        public String getInput()
        {
            return input;
        }

        public Model setInput(String input)
        {
            this.input = input;
            return this;
        }

        public Model addMessage(String str)
        {
            messages.add(str);
            return this;
        }
    }
}