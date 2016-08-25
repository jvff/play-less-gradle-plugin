package com.janitovff.play.less.gradle.language.less

import org.gradle.api.tasks.WorkResult

public interface LessCompiler {
    WorkResult compile(LessCompileSpec spec)
}
