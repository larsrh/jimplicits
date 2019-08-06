package info.hupel.jimplicits;

import info.hupel.jimplicits.logic.Term;
import org.junit.jupiter.api.Test;

import java.io.Closeable;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TypeTokenTests {

    @Test
    void testSimple() {
        var tok = new TypeToken<List<Integer>>() {};
        assertThat(tok.getTerm().isApplication()).isTrue();

        var app = (Term.Application) tok.getTerm();
        assertThat(app.getSymbol()).isEqualTo(List.class.getName());
        assertThat(app.getArguments()).size().isEqualTo(1);

        var inner = app.getArguments().get(0);
        assertThat(inner.isApplication()).isTrue();
        assertThat(((Term.Application) inner).getSymbol()).isEqualTo(Integer.class.getName());
        assertThat(((Term.Application) inner).getArguments()).size().isZero();
    }

    static class WithVariable<A> extends TypeToken<A> {}
    static class IndirectWithVariable extends WithVariable<Integer> {}

    @Test
    void doesNotSupportVariables() {
        assertThatThrownBy(WithVariable::new).hasMessageContaining("variable");
        assertThatThrownBy(IndirectWithVariable::new).hasMessageContaining("variable");
    }

    @Test
    void doesNotSupportWildcards() {
        assertThatThrownBy(() -> new TypeToken<List<?>>() {}).hasMessageContaining("unknown");
    }

    static class WithBoundVariable<A extends Closeable> extends TypeToken<A> {}

    @Test
    void doesNotSupportBounds() {
        assertThatThrownBy(WithBoundVariable::new).hasMessageContaining("bounds");
    }

}
