package com.janitovff.play.less.gradle.internal.language.less

import org.gradle.api.GradleException

public class SourceTransformationException extends GradleException {
    public SourceTransformationException(String message) {
        super(message)
    }

    public SourceTransformationException(String message, Throwable cause) {
        super(message, cause)
    }
}
