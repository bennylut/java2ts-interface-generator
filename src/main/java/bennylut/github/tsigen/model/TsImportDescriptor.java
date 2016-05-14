/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bennylut.github.tsigen.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import bennylut.github.tsigen.TsGenerate;

/**
 *
 * @author bennyl
 */
public class TsImportDescriptor implements TsDescriptor {

    private String fileName;
    private String imported;

    public TsImportDescriptor(String fileName, String imported) {
        this.fileName = fileName;
        this.imported = imported;
    }

    public static List<TsImportDescriptor> createImports(TsGenerate type) {
        List<TsImportDescriptor> result = new ArrayList<>();
        for (String imprt : type.imports()) {
            readImports(imprt, result);
        }

        return result;
    }

    private static void readImports(String imprt, List<TsImportDescriptor> into) {
        if (imprt.isEmpty()) {
            return;
        }
        
        int parenStart = imprt.indexOf("{");
        int parenEnd = imprt.indexOf("}");
        int fileStart = imprt.indexOf("'");
        int fileEnd = imprt.lastIndexOf("'");

        if (parenStart < 0 || parenEnd < 0 || parenEnd < parenStart) {
            throw new IllegalArgumentException("not a valid import statement: " + imprt);
        }

        String[] importElements = imprt.substring(parenStart + 1, parenEnd).trim().split("\\s*,\\s*");

        if (importElements.length == 0) {
            throw new IllegalArgumentException("not a valid import statement: " + imprt);
        }

        if (fileStart < 0 || fileStart == fileEnd) {
            throw new IllegalArgumentException("not a valid import statement: " + imprt);
        }

        String fileName = imprt.substring(fileStart + 1, fileEnd);

        for (String importElement : importElements) {
            into.add(new TsImportDescriptor(fileName, importElement));
        }

    }

    public TsImportDescriptor(TsGenerate interfaceAnnotation) {
        this(interfaceAnnotation.file(), interfaceAnnotation.name());
    }

    public TsImportDescriptor(TsInterfaceDescriptor interfaceDescriptor) {
        this(interfaceDescriptor.getFileName(), interfaceDescriptor.getName());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.fileName);
        hash = 29 * hash + Objects.hashCode(this.imported);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TsImportDescriptor other = (TsImportDescriptor) obj;
        if (!Objects.equals(this.fileName, other.fileName)) {
            return false;
        }
        if (!Objects.equals(this.imported, other.imported)) {
            return false;
        }
        return true;
    }

    @Override
    public void write(StringBuilder sb) {
        sb.append("import {").append(imported).append("} from '").append(fileName).append("'\n");
    }

    public boolean referenceFile(String fileName) {
        return this.fileName.equals(fileName);
    }

}
