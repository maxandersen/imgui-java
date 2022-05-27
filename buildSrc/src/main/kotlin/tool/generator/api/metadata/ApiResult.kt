package tool.generator.api.metadata

abstract class ApiResult(val type: String) {
    abstract class Primitive(type: String) : ApiResult(type)
    class Bool : Primitive("boolean")
    class Float : Primitive("float")

    class Str : ApiResult("String")

    class Struct(type: String, val static: Boolean = false, val isRef: Boolean = false) : ApiResult(type)

    abstract class ImVec(type: String) : ApiResult(type)
    class ImVec2 : ImVec("imgui.ImVec2")
    class ImVec4 : ImVec("imgui.ImVec4")
}
