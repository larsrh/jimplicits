package info.hupel.jimplicits;

import com.google.common.collect.Streams;
import info.hupel.jimplicits.logic.Rule;
import info.hupel.jimplicits.logic.Term;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class TypeUtils {

    private TypeUtils() {}

    private static boolean isPublicStatic(int modifiers) {
        return Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers);
    }

    private static void ensurePublicStatic(Member member) {
        if (!Modifier.isPublic(member.getModifiers()))
            throw new IllegalArgumentException("member is not public");
        if (!Modifier.isStatic(member.getModifiers()))
            throw new IllegalArgumentException("member is not static");
    }

    public static Rule<?> ruleOfMethod(Method method) {
        ensurePublicStatic(method);
        var returnTerm = termOfType(method.getAnnotatedReturnType().getType());
        var argumentTerms = Arrays.stream(method.getAnnotatedParameterTypes()).map(at -> termOfType(at.getType())).collect(Collectors.toList());
        return new Rule<>(
                list -> {
                    try {
                        return method.invoke(null, list.toArray());
                    }
                    catch (IllegalAccessException | InvocationTargetException ex) {
                        throw new RuntimeException(ex);
                    }
                },
                returnTerm,
                argumentTerms
        );
    }

    public static Rule<?> ruleOfField(Field field) {
        ensurePublicStatic(field);
        var term = termOfType(field.getAnnotatedType().getType());
        return new Rule<>(list -> {
            try {
                return field.get(null);
            }
            catch (IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }, term, List.of());
    }

    public static Rule<?> ruleOfMember(Member member) {
        if (member instanceof Field)
            return ruleOfField((Field) member);
        else if (member instanceof Method)
            return ruleOfMethod((Method) member);
        else
            throw new IllegalArgumentException("unknown member");
    }

    public static List<Rule<?>> rulesOfClass(Class<?> clazz) {
        var fields = Arrays.stream(clazz.getDeclaredFields()).filter(f -> f.isAnnotationPresent(Instance.class));
        var methods = Arrays.stream(clazz.getDeclaredMethods()).filter(f -> f.isAnnotationPresent(Instance.class));

        return Streams
                .<Member> concat(fields, methods)
                // Bug: JDK-8058112
                //.filter(m -> m.isAnnotationPresent(Instance.class))
                .filter(m -> isPublicStatic(m.getModifiers()))
                .map(TypeUtils::ruleOfMember)
                .collect(Collectors.toList());
    }

    public static Term termOfType(Type type) {
        if (type instanceof ParameterizedType) {
            var ptype = (ParameterizedType) type;
            return Term.application(
                    ptype.getRawType().getTypeName(),
                    Arrays.stream(ptype.getActualTypeArguments()).map(TypeUtils::termOfType).collect(Collectors.toList())
            );
        }
        else if (type instanceof TypeVariable) {
            var vtype = (TypeVariable<?>) type;
            var bounds = vtype.getBounds();
            if (bounds.length > 1 || !bounds[0].equals(Object.class))
                throw new IllegalArgumentException("cannot convert type with bounds");
            return Term.variable(vtype.getName());
        }
        else if (type instanceof Class) {
            var clazz = (Class<?>) type;
            return Term.application(clazz.getName(), List.of());
        }
        else {
            throw new IllegalArgumentException("unknown type structure");
        }
    }

}
