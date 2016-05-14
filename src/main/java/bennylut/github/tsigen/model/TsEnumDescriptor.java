/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bennylut.github.tsigen.model;

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
