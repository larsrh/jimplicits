package info.hupel.jimplicits;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class RuleTests {

    static class Rules {

        @Instance
        public static <A> List<A> listMaker(A a) {
            return List.of(a);
        }

        @Instance
        public static <A> Optional<A> optionalMaker(A a) {
            return Optional.of(a);
        }

        @Instance
        public static <A> List<A> emptyListMaker() {
            return List.of();
        }

        @Instance
        public static String base = "base-string";

    }

    @Test
    void testSimple() {
        var rules = TypeUtils.rulesOfClass(Rules.class);
        var expected = new TypeToken<List<Optional<String>>>() {};

        var objects = expected.getTyped().resolve(rules);

        assertThat(objects).containsExactlyInAnyOrder(
                List.of(),
                List.of(Optional.of(Rules.base))
        );
    }

    @Test
    void testNested() {
        var rules = TypeUtils.rulesOfClass(Rules.class);
        var expected = new TypeToken<List<List<String>>>() {};

        var objects = expected.getTyped().resolve(rules);

        assertThat(objects).containsExactlyInAnyOrder(
                List.of(),
                List.of(List.of()),
                List.of(List.of(Rules.base))
        );
    }

}
