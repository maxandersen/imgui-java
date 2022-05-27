package tool.generator.api.metadata

abstract class ApiMetadata {
    companion object {
        private val methodFactory = ApiMethodFactory()
    }

    lateinit var receiver: String

    var static: Boolean = false

    private val methods = mutableListOf<String>()

    fun method(name: String, result: ApiResult? = null, init: (ApiMethodDef.() -> Unit)? = null) {
        val methodDef = ApiMethodDef(receiver, name, static, result)
        if (init != null) {
            methodDef.init()
        }
        methods.addAll(methodFactory.create(methodDef))
    }

    fun render(): String {
        return buildString {
            appendLine(renderMethods())
        }.trim('\n')
    }

    private fun renderMethods(): String {
        return methods.joinToString("\n\n")
    }
}
