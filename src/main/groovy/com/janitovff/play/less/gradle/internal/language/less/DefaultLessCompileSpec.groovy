package com.janitovff.play.less.gradle.internal.language.less

import org.gradle.api.file.FileCollection

import com.janitovff.play.less.gradle.language.less.LessCompileSpec

public class DefaultLessCompileSpec implements LessCompileSpec {
    FileCollection source
    File destinationDirectory
    Set<File> srcDirs
}
