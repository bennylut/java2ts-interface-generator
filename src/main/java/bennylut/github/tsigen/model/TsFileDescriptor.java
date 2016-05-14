/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bennylut.github.tsigen.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

/**
 *
 * @author bennyl
 */
public class TsFileDescriptor implements TsDescriptor {

    private String fileName;
    private List<TsInterfaceDescriptor> interfaces = new ArrayList<>();
    private List<TsEnumDescriptor> enums = new ArrayList<>();
    private Set<TsImportDescriptor> imports = new HashSet<>();

    public TsFileDescriptor(String fileName) {
        this.fileName = fileName;
    }

    public void addInterface(Element e, ProcessingEnvironment processingEnv) {
        this.interfaces.add(new TsInterfaceDescriptor(e, processingEnv, imports));
    }

    public void addEnum(Element e, ProcessingEnvironment processingEnv) {
        this.enums.add(new TsEnumDescriptor(e, processingEnv));
    }

    @Override
    public void write(StringBuilder sb) {
        for (TsImportDescriptor imp : imports) {
            if (!imp.referenceFile(fileName))
            imp.write(sb);
        }

        sb.append("\n");
        for (TsEnumDescriptor enu : enums) {
            enu.write(sb);
        }

        sb.append("\n");
        for (TsInterfaceDescriptor inter : interfaces) {
            inter.write(sb);
        }
    }

}
