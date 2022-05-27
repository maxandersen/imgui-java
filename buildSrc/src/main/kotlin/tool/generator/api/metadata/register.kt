package tool.generator.api.metadata

import org.reflections.Reflections

val registeredMetadata = findMetadata()

private const val PACKAGE_TO_PARSE_FOR_METADATA = "tool.generator.api.metadata.def"

private fun findMetadata(): Map<String, ApiMetadata> {
    val registeredMetadata = mutableMapOf<String, ApiMetadata>()
    Reflections(PACKAGE_TO_PARSE_FOR_METADATA).run {
        getSubTypesOf(ApiMetadata::class.java).map { it.newInstance() }.forEach {
            val packageName = it.javaClass.`package`.name.removePrefix("$PACKAGE_TO_PARSE_FOR_METADATA.")
            registeredMetadata[packageName] = it
        }
    }
    return registeredMetadata
}
