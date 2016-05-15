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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.github.bennylut.tsigen.TsGenerate;

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
