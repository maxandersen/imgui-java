package tool.generator.api.metadata

class ApiMethodDef(
    val receiver: String,
    val name: String,
    val static: Boolean,
    val result: ApiResult?,
) {
    val args = mutableListOf<ApiArg>()

    fun args(vararg args: ApiArg) {
        this.args.addAll(args)
    }

    fun argStr(name: String, optional: Boolean = false, default: String? = null) = ApiArg.Str(name, optional, default)

    fun argInt(name: String, optional: Boolean = false, default: String? = null) = ApiArg.Int(name, optional, default)
    fun argBool(name: String, optional: Boolean = false, default: String? = null) = ApiArg.Bool(name, optional, default)

    fun argBoolPtr(name: String, optional: Boolean = false, default: String? = null) = ApiArg.ImBoolean(name, optional, default)

    fun argImVec2(name: String, optional: Boolean = false, default: String? = null) = ApiArg.ImVec2(name, optional, default)

    fun argStruct(type: String, name: String, optional: Boolean = false, default: String? = null) = ApiArg.Struct(type, name, optional, default)
}
