package info.hupel.jimplicits.logic;

import info.hupel.jimplicits.logic.test.LogicArbitraries;
import net.jqwik.api.*;

import static org.assertj.core.api.Assertions.assertThat;

class MatchTests {

    Arbitrary<String> name() {
        return Arbitraries.of("x", "y");
    }

    @Provide
    Arbitrary<Term> term() {
        return LogicArbitraries.term(name(), 3);
    }

    @Provide
    Arbitrary<Term> ground() {
        return term().filter(Term::isGround);
    }

    @Provide
    Arbitrary<Substitution> groundSubst() {
        return LogicArbitraries.substitution(name(), ground(), 2);
    }

    @Property
    void matchSelf(@ForAll("ground") Term instance) {
        assertThat(instance.match(instance)).contains(Substitution.EMPTY);
    }

    @Property(tries = 10000, maxDiscardRatio = 100)
    void substitutionUnifies(@ForAll("term") Term term, @ForAll("ground") Term instance) {
        var optionalSubst = term.match(instance);
        Assume.that(optionalSubst.isPresent());

        var subst = optionalSubst.get();

        assertThat(term.subst(subst)).isEqualTo(instance);
    }

    @Property
    void matchSubstituted(@ForAll("term") Term term, @ForAll("groundSubst") Substitution subst) {
        Assume.that(subst.domain().containsAll(term.frees()));

        var instance = term.subst(subst);

        assertThat(term.match(instance)).hasValueSatisfying(actual ->
                assertThat(actual.getSubst().entrySet()).isSubsetOf(subst.getSubst().entrySet())
        );
    }

}
