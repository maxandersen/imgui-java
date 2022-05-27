package tool.generator.api.task

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import tool.generator.api.metadata.ApiMetadata
import tool.generator.api.metadata.registeredMetadata
import java.io.File

/**
 * Task generates API by processing provided metadata.
 * To define a new metadata create a proper class in the "tool.generator.api.metadata.def" package.
 * Metadata file doesn't generate a java file, so it should be created at first.
 */
open class GenerateApi : DefaultTask() {
    @Internal
    override fun getGroup() = "build"
    @Internal
    override fun getDescription() = "Generate API for native binaries."

    private val genSrcDir = "src/main/java"
    private val genDstDir = "${project.buildDir}/generated/api"

    @TaskAction
    fun run() {
        logger.info("Generating API...")

        logger.info("Removing old generated files...")
        project.file(genDstDir).deleteRecursively()

        logger.info("Copying raw sources...")
        project.copy {
            from(genSrcDir)
            into(genDstDir)
        }

        logger.info("Processing raw sources...")

        for (sourceFile in project.file(genDstDir).walkTopDown()) {
            if (!sourceFile.isFile) {
                continue
            }

            registeredMetadata[fileToPackage(sourceFile)]?.let { apiMetadata ->
                logger.info(" + process: $sourceFile")
                processSourceFile(sourceFile, apiMetadata)
            }
        }
    }

    private fun fileToPackage(file: File): String {
        return file.relativeTo(File(genDstDir)).toString().replace("/", ".").removeSuffix(".java")
    }

    private fun processSourceFile(sourceFile: File, apiMetadata: ApiMetadata) {
        sourceFile.writeText(buildString {
            appendLine(sourceFile.readText().substringBeforeLast("}"))
            appendLine("    // GENERATED API: BEGIN")
            appendLine(apiMetadata.render())
            appendLine("    // GENERATED API: END")
            appendLine("}")
        })
    }
}
