package uk.org.thehickses.countdown;

import static org.assertj.core.api.Assertions.*;

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

    public SolverTest(TestContext context)
    {
        this.context = context;
    }

    @ParameterType("\\d+(?:[\\s,]+\\d+)*")
    public int[] ints(String str)
    {
        return Stream.of(str.split("\\D+")).mapToInt(Integer::parseInt).toArray();
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
        assertThat(IntStream.of(context.result.numbers).noneMatch(n ->
            {
                for (int i = 0; i < context.numbers.length; i++)
                {
                    if (context.numbers[i] == n)
                    {
                        context.numbers[i] = 0;
                        return false;
                    }
                }
                return true;
            }));
    }

    @Then("no solution is found")
    public void noSolutionFound()
    {
        assertThat(context.result).isNull();
    }
}
