package com.janitovff.play.less.gradle.language.less

import org.gradle.api.file.FileCollection

public interface LessCompileSpec {
    FileCollection getSource()
    File getDestinationDirectory()

    void setSource(FileCollection source)
    void setDestinationDirectory(File destinationDirectory)
}
