package info.hupel.jimplicits.logic;

import java.util.*;
import java.util.function.Function;

public final class Substitution implements Function<String, Term> {

    private final Map<String, Term> subst;

    public Substitution(Map<String, Term> subst) {
        this.subst = new HashMap<>(subst);
    }

    @Override
    public Term apply(String s) {
        return subst.getOrDefault(s, new Term.Variable(s));
    }

    public Term lift(Term t) {
        return t.subst(this);
    }

    public Map<String, Term> getSubst() {
        return Collections.unmodifiableMap(subst);
    }

    public Set<String> domain() {
        return Collections.unmodifiableSet(subst.keySet());
    }

    public Optional<Substitution> merge(Substitution that) {
        var subst = new HashMap<>(this.subst);
        for (var entry : that.subst.entrySet()) {
            if (subst.containsKey(entry.getKey())) {
                var old = subst.get(entry.getKey());
                if (!old.equals(entry.getValue()))
                    return Optional.empty();
            }
            else {
                subst.put(entry.getKey(), entry.getValue());
            }
        }
        return Optional.of(new Substitution(subst));
    }

    public static final Substitution EMPTY = new Substitution(Collections.emptyMap());

    public static Optional<Substitution> merge(List<Substitution> substitutions) {
        var merged = EMPTY;
        for (var subst : substitutions) {
            var newMerged = merged.merge(subst);
            if (newMerged.isPresent())
                merged = newMerged.get();
            else
                return Optional.empty();
        }
        return Optional.of(merged);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Substitution that = (Substitution) o;
        return Objects.equals(subst, that.subst);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subst);
    }

    @Override
    public String toString() {
        return subst.toString();
    }
}
