/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bennylut.github.tsigen.processor;

import bennylut.github.tsigen.model.TsFileDescriptor;
import com.google.auto.common.BasicAnnotationProcessor;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.SetMultimap;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.tools.Diagnostic;
import bennylut.github.tsigen.TsGenerate;
import java.util.HashMap;

/**
 *
 * @author bennyl
 */
@AutoService(Processor.class)
public class TsiAnnotationProcessor extends BasicAnnotationProcessor {

    private static final String TSI_PATH_OPTION = "tsi.basepath";

    private File tsiPath;

    @Override
    protected Iterable<? extends ProcessingStep> initSteps() {
        String tsiPathString = processingEnv.getOptions().get(TSI_PATH_OPTION);
        if (tsiPathString == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "missing tsi.basepath argument (-Atsi.basepath=base/path/for/generated/interfaces)");
            return Collections.EMPTY_SET;
        }

        //attempt to resolve relative base path
        if (tsiPathString.startsWith("@")) {
            File currentDirectory = new File(System.getProperty("user.dir")).getAbsoluteFile();
            int sleshIndex = tsiPathString.indexOf("/");
            if (sleshIndex < 0) {
                sleshIndex = tsiPathString.length();
            }

            String relativePart = tsiPathString.substring(1, sleshIndex);
            File travestedDirectory = new File(currentDirectory.getAbsolutePath());
            while (travestedDirectory != null && !travestedDirectory.getName().equals(relativePart)) {
                travestedDirectory = travestedDirectory.getParentFile();
            }

            if (travestedDirectory == null) {
                throw new UnsupportedOperationException("could not resolve relative path: " + tsiPathString + " from current directory: " + currentDirectory);
            }

            tsiPathString = new File(travestedDirectory, tsiPathString.substring(sleshIndex + 1)).getAbsolutePath();
        }

        this.tsiPath = new File(tsiPathString);
        return ImmutableSet.of(new InterfaceGenerationProcessingStep());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedOptions() {
        return ImmutableSet.of(TSI_PATH_OPTION);
    }

    private class InterfaceGenerationProcessingStep implements BasicAnnotationProcessor.ProcessingStep {

        Map<String, TsFileDescriptor> files = new HashMap<>();

        @Override
        public Set<? extends Class<? extends Annotation>> annotations() {
            return ImmutableSet.of(TsGenerate.class);
        }

        @Override
        public Set<Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {

            elementsByAnnotation.get(TsGenerate.class).forEach(this::generate);

            for (Map.Entry<String, TsFileDescriptor> file : files.entrySet()) {
                StringBuilder fileContent = new StringBuilder();
                file.getValue().write(fileContent);
                final File output = new File(tsiPath, file.getKey());
                output.getParentFile().mkdirs();

                try {
                    Files.write(fileContent.toString(), output, Charset.defaultCharset());
                } catch (IOException ex) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "cannot write typescript file " + output);
                }
            }

            return Collections.EMPTY_SET;
        }

        private void generate(Element e) {
            TsFileDescriptor file = files.computeIfAbsent(e.getAnnotation(TsGenerate.class).file(), TsFileDescriptor::new);

            if (e.getKind() == ElementKind.ENUM) {
                file.addEnum(e, processingEnv);
            } else {
                file.addInterface(e, processingEnv);
            }

        }

    }

}
