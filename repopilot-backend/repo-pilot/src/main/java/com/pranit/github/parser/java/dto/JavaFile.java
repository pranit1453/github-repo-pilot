package com.pranit.github.parser.java.dto;

import com.pranit.github.parser.constant.Language;
import lombok.Builder;

import java.util.List;

/**
 * Represents the parsed structure and relationships of a Java source file.
 *
 * @param path          path of the source file within the repository
 * @param packageName   package declaration of the source file
 * @param imports       imports declared in the source file
 * @param annotations   annotations declared at the file level
 * @param types         types declared in the source file
 * @param relationships relationships between types and their elements
 */
@Builder
public record JavaFile(
        String path,
        String packageName,
        List<Import> imports,
        List<Annotation> annotations,
        List<Type> types,
        List<Relationship> relationships) implements ParsedFile {

    @Override
    public Language language() {
        return Language.JAVA;
    }


    /**
     * Defines the supported relationships between Java types and elements.
     */
    public enum RelationshipKind {

        /**
         * Represents class or interface inheritance through {@code extends}.
         */
        EXTENDS,

        /**
         * Represents interface implementation through {@code implements}.
         */
        IMPLEMENTS,

        /**
         * Represents strong ownership where the target lifecycle depends on the source.
         */
        COMPOSITION,

        /**
         * Represents a weak ownership relationship between types.
         */
        AGGREGATION,

        /**
         * Represents a structural association between types.
         */
        ASSOCIATION,

        /**
         * Represents a dependency caused by usage of another type.
         */
        DEPENDENCY,

        /**
         * Represents a type or element being used by another element.
         */
        USES,

        /**
         * Represents method overriding through inheritance.
         */
        OVERRIDES,

        /**
         * Represents method or constructor calls between elements.
         */
        CALLS
    }

    /**
     * Represents a Java type such as a class, interface, enum, or record.
     *
     * @param name            name of the type
     * @param kind            kind of the type
     * @param modifiers       modifiers applied to the type
     * @param extendsType     type extended by the type
     * @param implementsTypes interfaces implemented by the type
     * @param annotations     annotations declared on the type
     * @param variables       variables declared within the type
     * @param methods         methods declared within the type
     * @param remaining       other elements that are not explicitly categorized
     */
    @Builder
    public record Type(
            String name,
            String kind,
            List<String> modifiers,
            String extendsType,
            List<String> implementsTypes,
            List<Annotation> annotations,
            List<Variable> variables,
            List<Method> methods,
            List<Element> remaining) {
    }

    /**
     * Represents a variable, field, or property declared within a Java type.
     *
     * @param name        name of the variable
     * @param type        declared type of the variable
     * @param modifiers   modifiers applied to the variable
     * @param annotations annotations declared on the variable
     */
    @Builder
    public record Variable(
            String name,
            String type,
            List<String> modifiers,
            List<Annotation> annotations) {
    }

    /**
     * Represents a method or constructor declared within a Java type.
     *
     * @param name        name of the method
     * @param kind        kind of the method, such as method or constructor
     * @param returnType  return type of the method
     * @param modifiers   modifiers applied to the method
     * @param annotations annotations declared on the method
     * @param parameters  parameters accepted by the method
     * @param throwsTypes exceptions declared by the method
     */
    @Builder
    public record Method(
            String name,
            String kind,
            String returnType,
            List<String> modifiers,
            List<Annotation> annotations,
            List<Parameter> parameters,
            List<String> throwsTypes) {
    }

    /**
     * Represents a parameter declared by a method or constructor.
     *
     * @param name        name of the parameter
     * @param type        declared type of the parameter
     * @param modifiers   modifiers applied to the parameter
     * @param annotations annotations declared on the parameter
     */
    @Builder
    public record Parameter(
            String name,
            String type,
            List<String> modifiers,
            List<Annotation> annotations) {
    }

    /**
     * Represents a Java source element that is not explicitly categorized.
     *
     * @param kind   kind of the source element
     * @param name   name of the source element, if available
     * @param source original source representation of the element
     */
    @Builder
    public record Element(
            String kind,
            String name,
            String source) {
    }

    /**
     * Represents an import declaration in a Java source file.
     *
     * @param name     fully qualified name of the imported type or member
     * @param isStatic whether the import is declared as static
     */
    @Builder
    public record Import(
            String name,
            boolean isStatic) {
    }

    /**
     * Represents an annotation declared in a Java source file.
     *
     * @param name      name of the annotation
     * @param arguments arguments supplied to the annotation
     */
    @Builder
    public record Annotation(
            String name,
            List<String> arguments) {
    }

    /**
     * Represents a relationship between Java types or their elements.
     *
     * @param source       source type or element
     * @param target       target type or element
     * @param kind         kind of relationship
     * @param name         relationship or member name, if applicable
     * @param sourceType   source element type, if applicable
     * @param targetType   target element type, if applicable
     * @param multiplicity multiplicity of the relationship, if applicable
     */
    @Builder
    public record Relationship(
            String source,
            String target,
            RelationshipKind kind,
            String name,
            String sourceType,
            String targetType,
            String multiplicity) {
    }
}