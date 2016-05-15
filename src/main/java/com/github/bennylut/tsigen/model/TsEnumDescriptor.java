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
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;

/**
 *
 * @author bennyl
 */
public class TsEnumDescriptor implements TsDescriptor {

    private List<String> enumConstants = new ArrayList<>();
    private String enumName;

    public TsEnumDescriptor(Element e, ProcessingEnvironment processingEnv) {
        this.enumName = e.getSimpleName().toString();
        for (Element enclosed : e.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.ENUM_CONSTANT) {
                VariableElement enumConstant = MoreElements.asVariable(enclosed);
                this.enumConstants.add(enumConstant.getSimpleName().toString());
            }
        }
    }

    @Override
    public void write(StringBuilder sb) {
        sb.append("export enum ")
                .append(enumName)
                .append(" { ");

        for (String c : enumConstants) {
            sb.append("\n\t")
                    .append(c)
                    .append(" = <any> '")
                    .append(c)
                    .append("',");
        }

        sb.delete(sb.length() - 1, sb.length());
        sb.append("\n}\n");
    }

}
