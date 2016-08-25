package com.janitovff.play.less.gradle.plugins

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.internal.file.SourceDirectorySetFactory
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.internal.service.ServiceRegistry
import org.gradle.language.base.internal.registry.LanguageTransform
import org.gradle.language.base.internal.registry.LanguageTransformContainer
import org.gradle.language.base.internal.SourceTransformTaskConfig
import org.gradle.language.base.LanguageSourceSet
import org.gradle.language.base.plugins.ComponentModelBasePlugin
import org.gradle.language.base.sources.BaseLanguageSourceSet
import org.gradle.model.Each
import org.gradle.model.Finalize
import org.gradle.model.ModelMap
import org.gradle.model.Mutate
import org.gradle.model.Path
import org.gradle.model.RuleSource
import org.gradle.platform.base.BinarySpec
import org.gradle.platform.base.ComponentType
import org.gradle.platform.base.TypeBuilder
import org.gradle.play.PlayApplicationSpec
import org.gradle.play.PlayApplicationBinarySpec

import com.janitovff.play.less.gradle.internal.language.css.DefaultCssSourceSet
import com.janitovff.play.less.gradle.language.css.CssSourceSet
import com.janitovff.play.less.gradle.language.less.LessSourceSet
import com.janitovff.play.less.gradle.tasks.LessCompile

public class PlayLessGradlePlugin implements Plugin<Project> {
    @Override
    public void apply(Project target) {
        target.getPluginManager().apply(ComponentModelBasePlugin)
    }

    static class Rules extends RuleSource {
        @ComponentType
        void registerLess(TypeBuilder<LessSourceSet> builder) {
        }

        @Finalize
        void createLessSourceSets(@Each PlayApplicationSpec playComponent) {
            playComponent.getSources().create("less", LessSourceSet.class,
                    new Action<LessSourceSet>() {
                @Override
                public void execute(LessSourceSet lessSourceSet) {
                    lessSourceSet.source.srcDir("app/assets")
                    lessSourceSet.source.include("**/*.less")
                }
            })
        }

        @Mutate
        void createGeneratedCssSourceSets(@Path("binaries")
                ModelMap<PlayApplicationBinarySpec> binaries,
                @Path("buildDir") File buildDir,
                final SourceDirectorySetFactory sourceDirectorySetFactory) {
            binaries.all(new Action<PlayApplicationBinarySpec>() {
                @Override
                public void execute(PlayApplicationBinarySpec binary) {
                    addLessSourceSetsToBinary(binary, buildDir,
                            sourceDirectorySetFactory)
                }
            })
        }

        private void addLessSourceSetsToBinary(PlayApplicationBinarySpec binary,
                File buildDir,
                final SourceDirectorySetFactory sourceDirectorySetFactory) {
            def lessSourceSets = binary.getInputs().withType(LessSourceSet)

            for (LessSourceSet lessSourceSet : lessSourceSets) {
                String cssSourceSetName = lessSourceSet.name + "Css"
                CssSourceSet cssSourceSet = BaseLanguageSourceSet.create(
                        CssSourceSet, DefaultCssSourceSet,
                        binary.getIdentifier().child(cssSourceSetName),
                        sourceDirectorySetFactory)

                File generatedSourceDirectory = binary.namingScheme
                        .getOutputDirectory(buildDir, "src")
                File generatedCssDirectory =
                        new File(generatedSourceDirectory, cssSourceSet.name)

                cssSourceSet.source.srcDir(generatedCssDirectory)

                binary.inputs.add(lessSourceSet)
                binary.inputs.add(cssSourceSet)
            }
        }

        @Mutate
        void registerLanguageTransform(LanguageTransformContainer languages) {
            languages.add(new Less())
        }
    }

    private static class Less
            implements LanguageTransform<LessSourceSet, CssSourceSet> {
        @Override
        public String getLanguageName() {
            return "less"
        }

        @Override
        public Class<LessSourceSet> getSourceSetType() {
            return LessSourceSet
        }

        @Override
        public Class<CssSourceSet> getOutputType() {
            return CssSourceSet
        }

        @Override
        public Map<String, Class<?> > getBinaryTools() {
            return Collections.emptyMap()
        }

        @Override
        public SourceTransformTaskConfig getTransformTask() {
            return new SourceTransformTaskConfig() {
                @Override
                public String getTaskPrefix() {
                    return "compile"
                }

                @Override
                public Class<? extends DefaultTask> getTaskType() {
                    return LessCompile.class
                }

                @Override
                public void configureTask(Task task, BinarySpec binarySpec,
                        LanguageSourceSet sourceSet,
                        ServiceRegistry serviceRegistry) {
                    PlayApplicationBinarySpec binary =
                            (PlayApplicationBinarySpec) binarySpec
                    LessSourceSet lessSourceSet = (LessSourceSet) sourceSet
                    LessCompile lessCompile = (LessCompile) task

                    CssSourceSet cssSourceSet =
                            getOutputSourceSetOf(lessSourceSet, binary)
                    File cssSourceDirectory = cssSourceSet.source.srcDirs[0]

                    lessCompile.source = sourceSet.source
                    lessCompile.outputDirectory =
                            new File(cssSourceDirectory, 'stylesheets')

                    binary.assets.builtBy(lessCompile)

                    for (File directory : cssSourceSet.source.srcDirs)
                        binary.assets.addAssetDir(directory)
                }

                private CssSourceSet getOutputSourceSetOf(
                        LessSourceSet inputSourceSet, BinarySpec binary) {
                    final String inputName = inputSourceSet.name
                    final String outputName = inputName + "Css"

                    def outputSourceSets = binary.inputs.matching {
                        return it.name.equals(outputName)
                    }

                    return outputSourceSets.iterator().next()
                }
            }
        }

        @Override
        public boolean applyToBinary(BinarySpec binary) {
            return binary instanceof PlayApplicationBinarySpec
        }
    }
}
