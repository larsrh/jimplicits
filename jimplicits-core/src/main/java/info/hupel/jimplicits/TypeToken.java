package info.hupel.jimplicits;

import info.hupel.jimplicits.logic.Term;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class TypeToken<T> {

    private final Type self;
    private final Term term;

    public TypeToken() {
        Class<?> clazz = this.getClass();
        while (!clazz.getSuperclass().equals(TypeToken.class))
            clazz = clazz.getSuperclass();

        var type = (ParameterizedType) clazz.getAnnotatedSuperclass().getType();
        this.self = type.getActualTypeArguments()[0];

        this.term = TypeUtils.termOfType(self);
        if (!term.isGround())
            throw new IllegalArgumentException("self type contains type variables");
    }

    public Type getType() {
        return self;
    }

    public Term getTerm() {
        return term;
    }

    public TypedTerm<T> getTyped() {
        return new TypedTerm<>(term);
    }

    @Override
    public String toString() {
        return "TypeToken{" +
                "self=" + self +
                '}';
    }

}
