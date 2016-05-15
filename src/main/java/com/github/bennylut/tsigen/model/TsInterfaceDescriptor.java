/* 
 * The MIT License
 *
 * Copyright 2016 bennyl.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.github.bennylut.tsigen.model;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import com.github.bennylut.tsigen.TsIgnore;
import com.github.bennylut.tsigen.TsGenerate;

/**
 *
 * @author bennyl
 */
public class TsInterfaceDescriptor implements TsDescriptor {

    private String name;
    private String fileName;
    private String superType = null;
    private List<TsPropertyDescriptor> properties = new ArrayList<>();

    public TsInterfaceDescriptor(Element e, ProcessingEnvironment processingEnv, Set<TsImportDescriptor> imports) {
        TsGenerate tsi = e.getAnnotation(TsGenerate.class);
        if (tsi == null) {
            throw new IllegalArgumentException("element does not contain " + TsGenerate.class + " annotation");
        }

        extractSuperType(e, processingEnv, imports);
        this.name = tsi.name();
        this.fileName = tsi.file();

        imports.addAll(TsImportDescriptor.createImports(tsi));
        parseFields(e, processingEnv, imports);
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    @Override
    public void write(StringBuilder sb) {
        sb.append("export interface ").append(name);
        if (superType != null) {
            sb.append(" extends ").append(superType);
        }

        sb.append(" {\n");
        for (TsPropertyDescriptor p : properties) {
            sb.append("\t");
            p.write(sb);
        }
        sb.append("}\n");
    }

    private void parseFields(Element e, ProcessingEnvironment processingEnv, Set<TsImportDescriptor> imports) {
        for (Element enclosed : e.getEnclosedElements()) {
            switch (enclosed.getKind()) {
                case FIELD:
                    if (enclosed.getAnnotation(TsIgnore.class) == null && !enclosed.getModifiers().contains(Modifier.STATIC)) {
                        this.properties.add(new TsPropertyDescriptor(MoreElements.asVariable(enclosed), processingEnv, imports));
                    }
                    break;
            }
        }
    }

    private void extractSuperType(Element e, ProcessingEnvironment processingEnv, Set<TsImportDescriptor> imports) {
        if (e.getKind() != ElementKind.CLASS) {
            throw new IllegalArgumentException("given element is not a class");
        }

        TypeElement typeElement = (TypeElement) e;
        TypeMirror superTypeMirror = typeElement.getSuperclass();
        if (superTypeMirror.getKind() != TypeKind.DECLARED) {
            throw new IllegalArgumentException("super class is not a declared class");
        }

        Element declaredSuperElement = MoreTypes.asElement(superTypeMirror);
        TsGenerate superAnnotation = declaredSuperElement.getAnnotation(TsGenerate.class);
        if (superAnnotation != null) {
            this.superType = superAnnotation.name();
            imports.add(new TsImportDescriptor(superAnnotation.file(), superAnnotation.name()));
        }
    }

}
