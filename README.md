# Play-Less Gradle plugin

Gradle plugin to be used with the Play! framework Gradle plugin that allows Less
stylesheets to be compiled into CSS stylesheets and included in the web
application.

## Usage

Using the new plugin mechanism (requires Gradle 2.1 or later):

    plugins {
        id "com.janitovff.play-less" version "0.0.2"
    }

Using the common plugin mechanism:

    buildscript {
        repositories {
            maven {
                url "https://plugins.gradle.org/m2/"
            }
        }
        dependencies {
            classpath "gradle.plugin.com.janitovff:play-less-gradle-plugin:0.0.2"
        }
    }

    apply plugin: "com.janitovff.play-less"

## Details

The [Less4j](https://github.com/SomMeri/less4j) compiler is used to compile the
Less stylesheets. By default, the source set is configured to load files from
"app/assets" that have the ".less" extension. This can be configured by using a
custom `less` source set:

    model {
        components {
            play {
                sources {
                    less {
                        source {
                            srcDir 'src/app/less'
                            include '**/*.less'
                        }
                    }
                }
            }
        }
    }
