package uk.org.thehickses.countdown;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.junit.platform.engine.Cucumber;
import uk.org.thehickses.countdown.Solver.Expression;

@Cucumber
public class SolverTest
{
    public static class TestContext
    {
        public int target;
        public int[] numbers;
        public Expression result;
    }

    private final TestContext context;

    // NB the test context is automatically injected by the pico-container integration - a different
    // instance for each test.
    public SolverTest(TestContext context)
    {
        this.context = context;
    }

    // This method defines a custom parameter type called "ints" (the name of the method), which can then be used in a
    // Cucumber expression to match a part of a Gherkin step, and be automatically converted by calling the method. The
    // "ints" type matches any substring that consists entirely of a list of non-negative integers, separated by commas
    // and/or whitespace, and converts that substring to an array of int.
    @ParameterType("\\d[\\s,\\d]*\\d")
    public int[] ints(String str)
    {
        return Stream.of(str.split("\\D+"))
                .mapToInt(Integer::parseInt)
                .toArray();
    }

    @When("I call the solver with target number {int} and numbers {ints}")
    public void callSolver(int target, int[] numbers)
    {
        context.target = target;
        context.numbers = numbers;
        context.result = new Solver(target, numbers).solve();
    }

    @Then("a solution is found whose value equals the target number and which uses {int} numbers")
    public void solutionFound(int count)
    {
        solutionFound(context.target, count);
    }

    @Then("a solution is found whose value equals {int} and which uses {int} numbers")
    public void solutionFound(int value, int count)
    {
        assertThat(context.result.value).isEqualTo(value);
        assertThat(context.result.numbers.length).isEqualTo(count);
        assertIsSubsetOf(context.numbers).accept(context.result.numbers);
    }

    private Consumer<int[]> assertIsSubsetOf(int[] superset)
    {
        Map<Integer, Integer> superOccurs = occurrenceCounts(superset);
        return subset -> occurrenceCounts(subset).entrySet()
                .forEach(e -> assertThat(e.getValue())
                        .isLessThanOrEqualTo(superOccurs.getOrDefault(e.getKey(), 0)));
    }

    private Map<Integer, Integer> occurrenceCounts(int[] arr)
    {
        return IntStream.of(arr)
                .boxed()
                .collect(Collectors.groupingBy(Function.identity(),
                        Collectors.reducing(0, e -> 1, Integer::sum)));
    }

    @Then("no solution is found")
    public void noSolutionFound()
    {
        assertThat(context.result).isNull();
    }
}
