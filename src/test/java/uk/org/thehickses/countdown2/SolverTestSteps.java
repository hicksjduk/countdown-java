package uk.org.thehickses.countdown2;

import java.util.stream.Stream;

import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import uk.org.thehickses.countdown2.Solver.Expression;

import static org.assertj.core.api.Assertions.*;

public class SolverTestSteps
{
    public static class TestContext
    {
        public int target;
        public Expression result;
    }
    
    private final TestContext context;

    public SolverTestSteps(TestContext context)
    {
        this.context = context;
    }

    @ParameterType("\\d+(?:[\\s,]+\\d+)*")
    public int[] ints(String str)
    {
        return Stream.of(str.split("[\\s,]+")).mapToInt(Integer::parseInt).toArray(); 
    }
    
    @When("I call the solver with target number {int} and numbers {ints}")
    public void callSolver(int target, int[] numbers)
    {
        context.target = target;
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
        assertThat(context.result.numberCount).isEqualTo(count);
    }

    @Then("no solution is found")
    public void noSolutionFound()
    {
        assertThat(context.result).isNull();
    }
}
