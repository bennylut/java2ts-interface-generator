/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bennylut.github.tsigen.model;

import bennylut.github.tsigen.util.MirrorUtil;
import static bennylut.github.tsigen.util.MirrorUtil.getTypeMirror;
import com.google.auto.common.MoreTypes;
import static com.google.auto.common.MoreTypes.isTypeOf;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import bennylut.github.tsigen.TsGenerate;
import bennylut.github.tsigen.TsType;

/**
 *
 * @author bennyl
 */
public class TsPropertyDescriptor implements TsDescriptor {

    private static TypeMirror MAP_TYPE;
    private static TypeMirror COLLECTION_TYPE;
    private static TypeMirror LOCAL_DATE_TYPE;
    private static TypeMirror NUMBER_TYPE;

    private String name;
    private String type;

    public TsPropertyDescriptor(VariableElement field, ProcessingEnvironment processingEnv, Set<TsImportDescriptor> imports) {
        if (MAP_TYPE == null) {
            initializeConstantTypes(processingEnv.getElementUtils(), processingEnv.getTypeUtils());
        }

        this.name = field.getSimpleName().toString();
        this.type = inferTypeFromAnnotation(field, imports);
        if (type == null) {
            TypeMirror fieldType = field.asType();
            this.type = guessType(fieldType, processingEnv, imports);
        }
    }

    private static void initializeConstantTypes(Elements elements, Types types) {
        MAP_TYPE = types.erasure(getTypeMirror(Number.class, elements));
        COLLECTION_TYPE = types.erasure(getTypeMirror(Collection.class, elements));
        LOCAL_DATE_TYPE = types.erasure(getTypeMirror(LocalDate.class, elements));
        NUMBER_TYPE = types.erasure(getTypeMirror(Number.class, elements));
    }

    @Override
    public void write(StringBuilder sb) {
        sb.append(name).append(": ").append(type).append(";\n");
    }

    private String inferTypeFromAnnotation(Element field, Set<TsImportDescriptor> imports) {
        TsType annotatedType = field.getAnnotation(TsType.class);
        if (annotatedType != null) {
            return annotatedType.value();
        }

        return null;
    }

    private String guessType(TypeMirror fieldType, ProcessingEnvironment processingEnv, Set<TsImportDescriptor> imports) {

        if (fieldType.getKind() == TypeKind.ARRAY) {
            ArrayType arrayType = MoreTypes.asArray(fieldType);
            return guessType(arrayType.getComponentType(), processingEnv, imports) + "[]";
        }

        if (MirrorUtil.isNummericType(fieldType)) {
            return "number";
        }

        if (fieldType.getKind() == TypeKind.DECLARED) {

            Elements elementUtil = processingEnv.getElementUtils();
            Types typeUtils = processingEnv.getTypeUtils();

            if (isTypeOf(String.class, fieldType)
                    || isTypeOf(char.class, fieldType)
                    || isTypeOf(Character.class, fieldType)
                    || isTypeOf(CharSequence.class, fieldType)) {
                return "string";
            }

            if (isTypeOf(boolean.class, fieldType) || isTypeOf(Boolean.class, fieldType)) {
                return "boolean";
            }

            DeclaredType declaredFieldType = MoreTypes.asDeclared(fieldType);
            if (typeUtils.isAssignable(fieldType, COLLECTION_TYPE)) {
                List<? extends TypeMirror> typeArguments = declaredFieldType.getTypeArguments();
                if (typeArguments.size() != 1) {
                    return "any";
                }

                return guessType(typeArguments.get(0), processingEnv, imports) + "[]";
            }

            if (typeUtils.isAssignable(fieldType, MAP_TYPE)) {
                List<? extends TypeMirror> typeArguments = declaredFieldType.getTypeArguments();
                if (typeArguments.size() != 2) {
                    return "any";
                }

                String keyType = guessType(typeArguments.get(0), processingEnv, imports);
                if (keyType.equals("string") || keyType.equals("number")) {
                    String valueType = guessType(typeArguments.get(1), processingEnv, imports);
                    return "{[x:" + keyType + "]:" + valueType + "}";
                }

                return "any";
            }

            if (typeUtils.isAssignable(fieldType, LOCAL_DATE_TYPE)) {
                return "number";
            }

            Element fieldTypeElement = MoreTypes.asTypeElement(fieldType);
            TsGenerate interfaceAnnotation = fieldTypeElement.getAnnotation(TsGenerate.class);
            if (interfaceAnnotation == null) {
                return "any";
            }
            imports.add(new TsImportDescriptor(interfaceAnnotation));
            return interfaceAnnotation.name();
        }

        return "any";
    }

}
