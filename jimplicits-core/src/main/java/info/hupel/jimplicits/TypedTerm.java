package info.hupel.jimplicits;

import info.hupel.jimplicits.logic.Rule;
import info.hupel.jimplicits.logic.Term;

import java.util.List;

public final class TypedTerm<T> {

    private final Term term;

    TypedTerm(Term term) {
        this.term = term;
    }

    @SuppressWarnings("unchecked")
    List<T> resolve(List<Rule<?>> rules) {
        return (List<T>) term.resolve((List) rules);
    }

    @Override
    public String toString() {
        return term.toString();
    }

}
