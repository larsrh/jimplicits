package info.hupel.jimplicits.logic;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class Rule<A> {

    private final Function<List<A>, A> action;
    private final Term head;
    private final List<Term> conditions;

    public Rule(Function<List<A>, A> action, Term head, List<Term> conditions) {
        if (!head.isApplication())
            throw new IllegalArgumentException("rule head must be an application");

        if (!head.frees().containsAll(conditions.stream().flatMap(cond -> cond.frees().stream()).collect(Collectors.toSet())))
            throw new IllegalArgumentException("extra free variables in conditions");

        this.action = action;
        this.head = head;
        this.conditions = List.copyOf(conditions);
    }

    public Term getHead() {
        return head;
    }

    public List<Term> getConditions() {
        return Collections.unmodifiableList(conditions);
    }

    public A act(List<A> children) {
        var copy = List.copyOf(children);

        if (copy.size() != conditions.size())
            throw new IllegalArgumentException();

        return action.apply(copy);
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(head);
        if (!conditions.isEmpty()) {
            builder.append(" :- ");
            builder.append(conditions);
        }
        builder.append(".");
        return builder.toString();
    }
}
