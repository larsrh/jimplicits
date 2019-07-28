package info.hupel.jimplicits.logic.test;

import info.hupel.jimplicits.logic.Substitution;
import info.hupel.jimplicits.logic.Term;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;

public final class LogicArbitraries {

    private LogicArbitraries() {}

    public static Arbitrary<Term> variable(Arbitrary<String> name) {
        return name.map(Term::variable);
    }

    public static Arbitrary<Term> term(Arbitrary<String> varName, Arbitrary<String> symName, int depth) {
        return Arbitraries.recursive(
                () -> variable(varName),
                term -> Combinators.combine(symName, term.list().ofMinSize(0).ofMaxSize(2)).as(Term::application),
                depth
        );
    }

    public static Arbitrary<Term> term(Arbitrary<String> name, int depth) {
        return term(name.map(n -> "v" + n), name.map(n -> "f" + n), depth);
    }

    public static Arbitrary<Substitution> substitution(Arbitrary<String> name, Arbitrary<Term> term, int maxSize) {
        return Arbitraries.maps(name, term).ofMinSize(0).ofMaxSize(maxSize).map(Substitution::new);
    }

}
