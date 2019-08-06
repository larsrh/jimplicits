package info.hupel.jimplicits.logic;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class Term {

    protected Term() {}

    public abstract <A> A fold(
            Function<String, A> onVariable,
            BiFunction<String, List<A>, A> onApplication
    );

    public final boolean isApplication() {
        return fold(s -> false, (sym, list) -> true);
    }

    public final Term subst(Substitution s) {
        return fold(s, Application::new);
    }

    public final boolean isGround() {
        return fold(s -> false, (sym, list) -> list.stream().allMatch(b -> b));
    }

    public final Set<String> frees() {
        return fold(
                Set::of,
                (sym, sets) -> sets.stream().flatMap(Set::stream).collect(Collectors.toSet())
        );
    }

    protected abstract Optional<Substitution> doMatch(Term instance);

    public final Optional<Substitution> match(Term instance) {
        if (!instance.isGround())
            throw new IllegalArgumentException("Instance `" + instance + "` is not ground");

        return doMatch(instance);
    }

    public <A> List<A> resolve(List<Rule<A>> rules) {
        var solutions = new LinkedList<A>();
        for (var rule : rules) {
            var substitution = rule.getHead().match(this);
            if (substitution.isEmpty())
                continue;

            // this produces a List<List<A>>; the outer list is each condition, the inner list each solution for a condition
            var resolveds =
                    rule.getConditions().stream()
                            .map(term -> term.subst(substitution.get()).resolve(rules))
                            .collect(Collectors.toList());

            // the cartesian product 'inverses' these lists: we compute every combination
            var combinations = Lists.cartesianProduct(resolveds);

            for (var combination : combinations)
                solutions.add(rule.act(combination));
        }
        return solutions;
    }

    public static Term variable(String name) {
        return new Variable(name);
    }

    public static Term application(String symbol, List<Term> arguments) {
        return new Application(symbol, arguments);
    }

    public static final class Variable extends Term {

        private final String name;

        public Variable(String name) {
            Objects.requireNonNull(name);
            this.name = name;
        }

        @Override
        public <A> A fold(Function<String, A> onVariable, BiFunction<String, List<A>, A> onApplication) {
            return onVariable.apply(name);
        }

        public String getName() {
            return name;
        }

        @Override
        protected Optional<Substitution> doMatch(Term instance) {
            return Optional.of(new Substitution(Map.of(name, instance)));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Variable variable = (Variable) o;
            return name.equals(variable.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }

        @Override
        public String toString() {
            return name;
        }

    }

    public static final class Application extends Term {

        private final String symbol;
        private final List<Term> arguments;

        public Application(String symbol, List<Term> arguments) {
            Objects.requireNonNull(symbol);
            Objects.requireNonNull(arguments);
            this.symbol = symbol;
            this.arguments = new ArrayList<>(arguments);
        }

        @Override
        public <A> A fold(Function<String, A> onVariable, BiFunction<String, List<A>, A> onApplication) {
            var foldedArguments = arguments.stream().map(t -> t.fold(onVariable, onApplication)).collect(Collectors.toList());
            return onApplication.apply(symbol, foldedArguments);
        }

        @Override
        protected Optional<Substitution> doMatch(Term instance) {
            // instance is ground
            var that = (Application) instance;

            if (that.symbol.equals(this.symbol) && that.arguments.size() == this.arguments.size()) {
                return Streams.zip(this.arguments.stream(), that.arguments.stream(), Term::doMatch).reduce(
                        Optional.of(Substitution.EMPTY),
                        (subst1, subst2) -> {
                            if (subst1.isEmpty() || subst2.isEmpty())
                                return Optional.empty();

                            return subst1.get().merge(subst2.get());
                        }
                );
            }
            else {
                return Optional.empty();
            }
        }

        public String getSymbol() {
            return symbol;
        }

        public List<Term> getArguments() {
            return Collections.unmodifiableList(arguments);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Application that = (Application) o;
            return Objects.equals(symbol, that.symbol) &&
                    Objects.equals(arguments, that.arguments);
        }

        @Override
        public int hashCode() {
            return Objects.hash(symbol, arguments);
        }

        @Override
        public String toString() {
            var builder = new StringBuilder();
            builder.append(symbol);
            builder.append("(");
            var first = true;
            for (var argument : arguments)  {
                if (!first)
                    builder.append(", ");
                first = false;
                builder.append(argument);
            }
            builder.append(")");
            return builder.toString();
        }

    }

}
