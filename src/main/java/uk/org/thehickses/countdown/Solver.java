package uk.org.thehickses.countdown;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solver
{
    private static final Logger LOG = LoggerFactory.getLogger(Solver.class);

    public static void main(String[] args)
    {
        try
        {
            int[] numArgs = validate(args);
            if (numArgs.length == 1)
            {
                Random rand = new Random();
                int target = rand.nextInt(900) + 100;
                int[] numbers = selectRandomNumbers(rand, numArgs[0]);
                new Solver(target, numbers).solve();
            }
            else
            {
                new Solver(numArgs[0], ArrayUtils.subarray(numArgs, 1, numArgs.length)).solve();
            }
        }
        catch (ValidationException ex)
        {
            LOG.error(ex.getLocalizedMessage());
        }
    }

    @SuppressWarnings("serial")
    private static class ValidationException extends RuntimeException
    {
        public ValidationException(String message)
        {
            super(message);
        }
    }

    private static int[] validate(String[] args) throws ValidationException
    {
        if (Stream.of(args).anyMatch(a -> !a.matches("\\d+")))
            throw new ValidationException("All arguments must be numbers");
        if (args.length != 1 && args.length != 7)
            throw new ValidationException(
                    "Must specify either one number (the number of large numbers to select) "
                            + "or seven numbers, of which the first is the target");
        int[] numArgs = Stream.of(args).mapToInt(Integer::parseInt).toArray();
        if (numArgs.length == 1)
        {
            if (numArgs[0] < 0 || numArgs[0] > 4)
                throw new ValidationException(
                        "Number of large numbers must be in the range 0 to 4 inclusive");
        }
        else
        {
            int target = numArgs[0];
            int[] numbers = ArrayUtils.subarray(numArgs, 1, numArgs.length);
            if (target < 100 || target > 999)
                throw new ValidationException(
                        "Target number must be in the range 100 to 999 inclusive");
            Map<Integer, Integer> occurrences = new HashMap<>();
            IntStream.of(numbers).forEach(n ->
                {
                    if (n < 1 || n > 100 || (n > 10 && n % 25 != 0))
                        throw new ValidationException(
                                "Source numbers must be in the range 1 to 10, "
                                        + "or 25, 50, 75 or 100");
                    int occ = occurrences.getOrDefault(n, 0) + 1;
                    if (n <= 10 && occ > 2)
                        throw new ValidationException(
                                "Small source numbers (<=10) cannot appear more than twice");
                    if (n > 10 && occ > 1)
                        throw new ValidationException(
                                "Large source numbers (>10) cannot appear more than once");
                    occurrences.put(n, occ);
                });
        }
        return numArgs;
    }

    private static int[] selectRandomNumbers(Random rand, int largeCount)
    {
        List<Integer> large = Stream.of(25, 50, 75, 100).collect(Collectors.toList());
        List<Integer> small = Stream
                .of(1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 10, 10)
                .collect(Collectors.toList());
        return IntStream.range(0, 6).map(i ->
            {
                List<Integer> numbers = i < largeCount ? large : small;
                int index = rand.nextInt(numbers.size());
                return numbers.remove(index);
            }).toArray();
    }

    private final int target;
    private final int[] numbers;

    public Solver(int target, int[] numbers)
    {
        this.target = target;
        this.numbers = numbers;
    }

    public Expression solve()
    {
        LOG.info("-------------------------------------------------------------------");
        LOG.info("Target: {}, numbers: {}", target, Arrays.toString(numbers));
        Expression answer = permute(IntStream.of(numbers).mapToObj(Expression::new))
                .parallel()
                .flatMap(this::expressions)
                .filter(e -> e.differenceFrom(target) <= 10)
                .reduce(null, evaluator(target));
        if (answer == null)
            LOG.info("No solution found");
        else
            LOG.info("Best solution is {} = {}", answer, answer.value);
        LOG.info("-------------------------------------------------------------------");
        return answer;
    }

    private Stream<Expression[]> permute(Stream<Expression> exprs)
    {
        Expression[] items = exprs.toArray(Expression[]::new);
        if (items.length == 1)
            return Stream.generate(() -> items).limit(1);
        Predicate<Expression> used = usedChecker();
        return IntStream
                .range(0, items.length)
                .filter(i -> !used.test(items[i]))
                .boxed()
                .flatMap(i ->
                    {
                        Stream<Expression> others = IntStream
                                .range(0, items.length)
                                .filter(j -> j != i)
                                .mapToObj(j -> items[j]);
                        Stream<Expression[]> suffixes = Stream
                                .concat(Stream.generate(() -> new Expression[0]).limit(1),
                                        permute(others));
                        return suffixes.map(s -> ArrayUtils.addFirst(s, items[i]));
                    });
    }

    private Predicate<Expression> usedChecker()
    {
        Set<Integer> usedValues = new HashSet<>();
        return value -> !usedValues.add(value.value);
    }

    private Stream<Expression> expressions(Expression[] permutation)
    {
        if (permutation.length == 1)
            return Stream.of(permutation);
        return IntStream.range(1, permutation.length).boxed().flatMap(i ->
            {
                Stream<Expression> leftOperands = expressions(
                        ArrayUtils.subarray(permutation, 0, i));
                return leftOperands.flatMap(leftOperand ->
                    {
                        Combiner[] combiners = combiners(leftOperand).toArray(Combiner[]::new);
                        Stream<Expression> rightOperands = expressions(
                                ArrayUtils.subarray(permutation, i, permutation.length));
                        return rightOperands
                                .flatMap(rightOperand -> Stream
                                        .of(combiners)
                                        .map(c -> c.apply(rightOperand))
                                        .filter(Objects::nonNull));
                    });
            }).filter(usedChecker().negate());
    }

    private BinaryOperator<Expression> evaluator(int target)
    {
        Comparator<Expression> comp = Comparator
                .nullsLast(Comparator
                        .comparingInt((Expression e) -> e.differenceFrom(target))
                        .thenComparingInt(e -> e.numbers.length));
        return (expr1, expr2) -> comp.compare(expr1, expr2) <= 0 ? expr1 : expr2;
    }

    private static interface Priority
    {
        public static int LOW = 0;
        public static int HIGH = 1;
        public static int ATOMIC = 2;
    }

    public static class Expression
    {
        public final int value;
        public final int[] numbers;
        private final int priority;
        private final Supplier<String> toString;

        public Expression(int number)
        {
            value = number;
            numbers = IntStream.of(number).toArray();
            priority = Priority.ATOMIC;
            toString = () -> String.format("%d", number);
        }

        public Expression(Expression leftOperand, Operator operator, Expression rightOperand)
        {
            value = operator.evaluate(leftOperand, rightOperand);
            numbers = IntStream
                    .concat(IntStream.of(leftOperand.numbers), IntStream.of(rightOperand.numbers))
                    .toArray();
            priority = operator.priority;
            toString = () -> Stream
                    .of(parenthesisedIfNecessary(leftOperand,
                            leftOperand.priority < operator.priority), operator.symbol,
                            parenthesisedIfNecessary(rightOperand,
                                    rightOperand.priority < operator.priority
                                            || (rightOperand.priority == operator.priority
                                                    && !operator.commutative)))
                    .collect(Collectors.joining(" "));
        }

        private static String parenthesisedIfNecessary(Expression expr, boolean parenthesise)
        {
            return String.format(parenthesise ? "(%s)" : "%s", expr);
        }

        public String toString()
        {
            return toString.get();
        }

        public int differenceFrom(int number)
        {
            return Math.abs(number - value);
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(numbers);
            result = prime * result + priority;
            result = prime * result + ((toString == null) ? 0 : toString.hashCode());
            result = prime * result + value;
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Expression other = (Expression) obj;
            if (!Arrays.equals(numbers, other.numbers))
                return false;
            if (priority != other.priority)
                return false;
            if (toString == null)
            {
                if (other.toString != null)
                    return false;
            }
            else if (!toString.equals(other.toString))
                return false;
            if (value != other.value)
                return false;
            return true;
        }
    }

    public static enum Operator
    {
        ADD("+", Priority.LOW, true, (a, b) -> a + b),
        SUBTRACT("-", Priority.LOW, false, (a, b) -> a - b),
        MULTIPLY("*", Priority.HIGH, true, (a, b) -> a * b),
        DIVIDE("/", Priority.HIGH, false, (a, b) -> a / b);

        public final String symbol;
        public final int priority;
        public final boolean commutative;
        private final IntBinaryOperator evaluator;

        private Operator(String symbol, int priority, boolean commutative,
                IntBinaryOperator evaluator)
        {
            this.symbol = symbol;
            this.priority = priority;
            this.commutative = commutative;
            this.evaluator = evaluator;
        }

        public int evaluate(Expression a, Expression b)
        {
            return evaluator.applyAsInt(a.value, b.value);
        }
    }

    @FunctionalInterface
    private static interface Combiner extends UnaryOperator<Expression>
    {
    }

    @FunctionalInterface
    private static interface CombinerCreator extends Function<Expression, Combiner>
    {
    }

    private static Combiner addCombiner(Expression expr1)
    {
        return expr2 -> new Expression(expr1, Operator.ADD, expr2);
    }

    private static Combiner subtractCombiner(Expression expr1)
    {
        if (expr1.value < 3)
            return null;
        return expr2 ->
            {
                if (expr1.value <= expr2.value || expr1.value == expr2.value * 2)
                    return null;
                return new Expression(expr1, Operator.SUBTRACT, expr2);
            };
    }

    private static Combiner multiplyCombiner(Expression expr1)
    {
        if (expr1.value == 1)
            return null;
        return expr2 ->
            {
                if (expr2.value == 1)
                    return null;
                return new Expression(expr1, Operator.MULTIPLY, expr2);
            };
    }

    private static Combiner divideCombiner(Expression expr1)
    {
        if (expr1.value == 1)
            return null;
        return expr2 ->
            {
                if (expr2.value == 1 || expr1.value % expr2.value != 0
                        || expr1.value == expr2.value * expr2.value)
                    return null;
                return new Expression(expr1, Operator.DIVIDE, expr2);
            };
    }

    private static Stream<CombinerCreator> combiners()
    {
        return Stream
                .of(Solver::addCombiner, Solver::subtractCombiner, Solver::multiplyCombiner,
                        Solver::divideCombiner);
    }

    private Stream<Combiner> combiners(Expression expr1)
    {
        return combiners().map(c -> c.apply(expr1)).filter(Objects::nonNull);
    }
}
