package com.janitovff.play.less.gradle.tasks

import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

import com.janitovff.play.less.gradle.language.less.LessCompiler
import com.janitovff.play.less.gradle.language.less.LessCompileSpec
import com.janitovff.play.less.gradle.internal.language.less.DefaultLessCompileSpec
import com.janitovff.play.less.gradle.internal.language.less.Less4jCompiler

public class LessCompile extends SourceTask {
    @OutputDirectory
    File outputDirectory

    Set<File> srcDirs;

    @TaskAction
    void compile() {
        LessCompileSpec spec = new DefaultLessCompileSpec()

        spec.source = source
        spec.destinationDirectory = outputDirectory
        spec.srcDirs = srcDirs

        LessCompiler compiler = new Less4jCompiler()

        didWork = compiler.compile(spec).didWork
    }
}
