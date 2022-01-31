package uk.org.thehickses.countdown.web;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.org.thehickses.countdown.Solver;
import uk.org.thehickses.countdown.Solver.Expression;

@RestController
public class Controller
{
    @Autowired
    Templater templater;
    
    @RequestMapping(path = "/")
    public String home(HttpServletRequest req) throws Exception
    {
        Model model = new Model();
        if (!req.getParameterNames()
                .hasMoreElements())
            return outputPage(model);
        String[] args = IntStream.range(0, 7)
                .mapToObj(i -> req.getParameter("num" + i))
                .filter(StringUtils::isNotEmpty)
                .toArray(String[]::new);
        try
        {
            Solver solver = Solver.instance(args);
            model.addMessage(String.format("Target: %d, numbers: %s", solver.getTarget(),
                    IntStream.of(solver.getNumbers())
                            .mapToObj(String::valueOf)
                            .collect(Collectors.joining(", "))));
            Expression solution = solver.solve();
            if (solution == null)
                model.addMessage("No solution found");
            else
                model.addMessage(String.format("%s = %d", solution, solution.value));
            return outputPage(model);
        }
        catch (IllegalArgumentException ex)
        {
            model.setInput(args)
                    .addMessage(ex.getMessage());
            return outputPage(model);
        }
    }

    private String outputPage(Model model) throws Exception
    {
        return templater.applyTemplate("index.ftlh", model);
    }

    public static class Model
    {
        private String[] input;
        private final List<String> messages = new ArrayList<>();

        public List<String> getMessages()
        {
            return messages;
        }

        public String[] getInput()
        {
            return input;
        }

        public Model setInput(String[] input)
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