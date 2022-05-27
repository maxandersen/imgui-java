package tool.generator.api.metadata

abstract class ApiArg(
    val name: String,
    val optional: Boolean,
    val default: String? = null,
) {
    protected open fun typeJava(): String = javaClass.simpleName
    protected open fun typeNative(): String = javaClass.simpleName

    open fun inSignatureJava(): String = "final ${typeJava()} $name"
    open fun inBodyJava(): String = name
    open fun inSignatureNative(): String = "${typeNative()} $name"
    open fun inBodyNative(): String = name

    // Used to represent arguments with a default value in the native body.
    // When rendered, such args only represent the native in-body side of the method.
    // They are ignored in signatures of all types.
    class Default(default: String) : ApiArg("", false, default) {
        override fun inBodyNative() = default!!
    }

    class Str(name: String, optional: Boolean, default: String?) : ApiArg(name, optional, default) {
        override fun typeJava() = "String"
        override fun typeNative() = "String"
    }

    abstract class Primitive(name: String, optional: Boolean, default: String?) : ApiArg(name, optional, default) {
        override fun typeJava() = super.typeNative().toLowerCase()
        override fun typeNative() = super.typeNative().toLowerCase()
    }
    class Int(name: String, optional: Boolean, default: String?) : Primitive(name, optional, default)
    class Bool(name: String, optional: Boolean, default: String?) : Primitive(name, optional, default) {
        override fun typeJava() = "boolean"
        override fun typeNative() = "boolean"
    }

    // To represent reference types.
    abstract class PrimitiveWrapper(name: String, optional: Boolean, default: String?) : ApiArg(name, optional, default) {
        override fun inBodyJava(): String = "$name == null ? new ${nativeType()}[1] : $name.getData()"
        override fun typeNative(): String = "${nativeType()}[]"
        override fun inBodyNative(): String = "&$name[0]"

        protected abstract fun nativeType(): String
    }
    class ImBoolean(name: String, optional: Boolean, default: String?) : PrimitiveWrapper(name, optional, default) {
        override fun nativeType() = "boolean"
    }

    // Structs are converted into the native pointer which is represented as a "long" type.
    class Struct(private val type: String, name: String, optional: Boolean, default: String?) : ApiArg(name, optional, default) {
        override fun typeJava(): String = type
        override fun inBodyJava(): String = "${name}.ptr"
        override fun typeNative(): String = "long"
        override fun inBodyNative(): String = "(${type.substringAfterLast('.')}*)$name"
    }

    interface AlternateArgJava {
        fun getAlternativeArgJava(): ApiArg
    }

    open class ImVec2(name: String, optional: Boolean, default: String?) : ApiArg(name, optional, default), AlternateArgJava {
        override fun inBodyJava() = "$name.x, $name.y"
        override fun inSignatureNative() = "float ${name}X, float ${name}Y"
        override fun inBodyNative() = "ImVec2(${name}X, ${name}Y)"

        override fun getAlternativeArgJava(): ApiArg {
            return object : ImVec2(name, optional, default) {
                override fun inSignatureJava() = "final float ${name}X, final float ${name}Y"
                override fun inBodyJava() = "${name}X, ${name}Y"
            }
        }
    }
}
