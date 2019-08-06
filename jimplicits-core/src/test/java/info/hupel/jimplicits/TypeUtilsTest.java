package info.hupel.jimplicits;

import info.hupel.jimplicits.logic.Rule;
import info.hupel.jimplicits.logic.Term;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeUtilsTest {

    static class Dummy {
        protected static int t = 0;
        protected static int t() { return 0; }

        public int u = 0;
        public int u() { return 0; }
    }

    @Test
    void failOnProtected() {
        assertThatThrownBy(() -> TypeUtils.ruleOfField(Dummy.class.getDeclaredField("t"))).hasMessageContaining("public");
        assertThatThrownBy(() -> TypeUtils.ruleOfMethod(Dummy.class.getDeclaredMethod("t"))).hasMessageContaining("public");
    }

    @Test
    void failOnNonStatic() {
        assertThatThrownBy(() -> TypeUtils.ruleOfField(Dummy.class.getDeclaredField("u"))).hasMessageContaining("static");
        assertThatThrownBy(() -> TypeUtils.ruleOfMethod(Dummy.class.getDeclaredMethod("u"))).hasMessageContaining("static");
    }

    static class Proper {
        public static int x = 1;
        public static <A> Optional<A> x(List<A> y) { return y.stream().findFirst(); }
    }

    @Test
    void field() throws NoSuchFieldException {
        var rule = TypeUtils.ruleOfField(Proper.class.getDeclaredField("x"));
        assertThat(rule.getConditions()).isEmpty();
        assertThat(rule.getHead()).isEqualTo(Term.application("int", List.of()));
        assertThat(rule.act(List.of())).isEqualTo(1);
    }

    @Test
    void method() throws NoSuchMethodException {
        var rule = (Rule<Object>) TypeUtils.ruleOfMethod(Proper.class.getDeclaredMethod("x", List.class));
        assertThat(rule.getConditions()).size().isEqualTo(1);
        assertThat(rule.getConditions()).element(0).isEqualTo(Term.application(List.class.getName(), List.of(Term.variable("A"))));
        assertThat(rule.getHead()).isEqualTo(Term.application(Optional.class.getName(), List.of(Term.variable("A"))));
        assertThat(rule.act(List.of(List.of("foo")))).isEqualTo(Optional.of("foo"));
    }

}
