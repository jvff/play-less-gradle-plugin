package com.janitovff.play.less.gradle.internal.language.less

import org.gradle.api.tasks.WorkResult

import com.github.sommeri.less4j.core.DefaultLessCompiler
import com.github.sommeri.less4j.Less4jException
import com.github.sommeri.less4j.LessCompiler.CompilationResult
import com.github.sommeri.less4j.LessCompiler.Problem

import com.janitovff.play.less.gradle.language.less.LessCompiler
import com.janitovff.play.less.gradle.language.less.LessCompileSpec

import java.nio.file.Path

public class Less4jCompiler implements LessCompiler {
    private DefaultLessCompiler compiler

    public Less4jCompiler() {
        compiler = new DefaultLessCompiler()
    }

    public WorkResult compile(LessCompileSpec spec) {
        for (File file : spec.source.files)
            compileFile(file, spec)

        return new WorkResult() {
            @Override
            public boolean getDidWork() {
                return true
            }
        }
    }

    private void compileFile(File lessFile, LessCompileSpec spec) {
        String css = compileLessFileIntoCssString(lessFile)
        File cssFile = getOutputCssFileFor(lessFile, spec)

        writeResultingCssToFile(css, cssFile)
    }

    private String compileLessFileIntoCssString(File lessFile) {
        CompilationResult result = safelyCompile(lessFile)
        List<Problem> problems = result.warnings

        if (!problems.isEmpty())
            throwCompilationException(lessFile, problems)

        return result.css
    }

    private CompilationResult safelyCompile(File lessFile) {
        try {
            return compiler.compile(lessFile)
        } catch (Less4jException cause) {
            String message = "Failed to compile Less stylesheet"

            throw new SourceTransformationException(message, cause)
        }
    }

    private void throwCompilationException(File lessFile,
            List<Problem> problems) {
        String message = "Failed to compile Less stylesheet: "
        String newline = System.lineSeparator()
        StringBuilder error = new StringBuilder(message)

        error.append(lessFile.name)
        error.append(newline)

        for (Problem problem : problems) {
            error.append(problem.message)
            error.append(newline)
        }

        throw new SourceTransformationException(error.toString())
    }

    private File getOutputCssFileFor(File lessFile, LessCompileSpec spec) {
        String cssFileName = lessFile.name.replaceFirst("[.]less\$", ".css")

        Path lessFilePath = lessFile.getParentFile().toPath();
        for (File srcDir : spec.getSrcDirs()) {
            Path srcPath = srcDir.toPath()
            if (lessFilePath.startsWith(srcPath)) {
                Path relative = srcPath.relativize(lessFilePath);
                Path outputPath = spec.destinationDirectory.toPath();
                Path outputSubFolderPath = outputPath.resolve(relative);
                File outputSubFolder = outputSubFolderPath.toFile();
                outputSubFolder.mkdirs();
                return new File(outputSubFolder, cssFileName);
            }
        }

        throw new RuntimeException("Could not resolve src root folder for matching less file '" + lessFile.getAbsolutePath() + "'");
    }

    private void writeResultingCssToFile(String css, File cssFile) {
        try {
            unsafelyWriteCssToFile(css, cssFile)
        } catch (IOException cause) {
            String mesage = "Failed to write output CSS file"

            throw new SourceTransformationException(message, cause)
        }
    }

    private void unsafelyWriteCssToFile(String css, File cssFile)
            throws IOException{
        cssFile.withWriter {
            it.write(css)
            it.flush()
        }
    }
}
