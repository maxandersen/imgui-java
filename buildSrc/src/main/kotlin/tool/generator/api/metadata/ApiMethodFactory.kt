package tool.generator.api.metadata

class ApiMethodFactory {
    fun create(methodDef: ApiMethodDef): Collection<String> {
        val methods = mutableListOf<String>()

        collectArgGroups(methodDef.args).forEach { argGroup ->
            methods.add(createMethodsJava(methodDef, argGroup))
            createAltMethodsJava(methodDef, argGroup)?.let(methods::add)
            methods.add(createMethodsNative(methodDef, argGroup))
        }

        return methods
    }

    private fun createAltMethodsJava(methodDef: ApiMethodDef, argGroup: ArgGroup): String? {
        val subGroup = mutableListOf<ApiArg>()

        for (apiArg in argGroup.args) {
            subGroup += if (apiArg is ApiArg.AlternateArgJava) {
                apiArg.getAlternativeArgJava()
            } else {
                apiArg
            }
        }

        return if (subGroup == argGroup.args) {
            return null
        } else {
            createMethodsJava(methodDef, ArgGroup(subGroup))
        }
    }

    private fun createMethodsJava(methodDef: ApiMethodDef, argGroup: ArgGroup): String {
        val argsInSign = argGroup.renderJavaInSignature()
        val argsInBody = argGroup.renderJavaInBody()

        val methodMods = if (methodDef.static) "public static" else "public"
        val methodName = methodDef.name.decapitalize()
        val methodSignCall = "$methodName($argsInSign)"
        val methodCallNative = "n${methodDef.name}($argsInBody)"

        if (methodDef.result == null) {
            return """
            |    $methodMods void $methodSignCall {
            |        $methodCallNative;
            |    }
            """.trimMargin()
        }

        val methodSign = "$methodMods ${methodDef.result.type} $methodSignCall"

        if (methodDef.result is ApiResult.Struct) {
            if (methodDef.result.static) {
                val staticFieldName = "_gen_${methodName}_${argGroup.size}"

                return """
                |    private static final ${methodDef.result.type} $staticFieldName = new ${methodDef.result.type}(0);
                |    $methodSign {
                |        ${staticFieldName}.ptr = $methodCallNative;
                |        return ${staticFieldName};
                |    }
                """.trimMargin()
            } else {
                return """
                |    $methodSign {
                |        return new ${methodDef.result.type}($methodCallNative);
                |    }
                """.trimMargin()
            }
        }

        if (methodDef.result is ApiResult.ImVec) {
            val dstWithArgsInSign = "dst${if (argsInSign.isNotEmpty()) ", $argsInSign" else ""}"
            val dstWithArgsInBody = "dst${if (argsInBody.isNotEmpty()) ", $argsInBody" else ""}"

            var methods = """
            |    $methodSign {
            |        final ${methodDef.result.type} dst = new ${methodDef.result.type}();
            |        n${methodDef.name}($dstWithArgsInBody);
            |        return dst;
            |    }
            |
            |    $methodMods void $methodName(final ${methodDef.result.type} $dstWithArgsInSign) {
            |        n${methodDef.name}($dstWithArgsInBody);
            |    }
            |
            |    $methodMods float ${methodName}X($argsInSign) {
            |        return n${methodDef.name}X($argsInBody);
            |    }
            |    $methodMods float ${methodName}Y($argsInSign) {
            |        return n${methodDef.name}Y($argsInBody);
            |    }
            """.trimMargin()

            if (methodDef.result is ApiResult.ImVec4) {
                methods += """
                |
                |    $methodMods float ${methodName}Z($argsInSign) {
                |       return n${methodDef.name}Z($argsInBody);
                |    }
                |    $methodMods float ${methodName}W($argsInSign) {
                |       return n${methodDef.name}W($argsInBody);
                |    }
                """.trimMargin()
            }

            return methods
        }

        return """
        |    $methodMods ${methodDef.result.type} $methodSignCall {
        |        return $methodCallNative;
        |    }
        """.trimMargin()
    }

    private fun createMethodsNative(methodDef: ApiMethodDef, argGroup: ArgGroup): String {
        val argsInSign = argGroup.renderNativeInSignature()
        val argsInBody = argGroup.renderNativeInBody()

        val methodMods = if (methodDef.static) "private static native" else "private native"
        val methodName = "n${methodDef.name}"
        val methodSignCall = "n${methodDef.name}($argsInSign)"
        val methodCall = "${methodDef.receiver}${methodDef.name}($argsInBody)"

        if (methodDef.result == null) {
            return """
            |    $methodMods void $methodSignCall; /*
            |        $methodCall;
            |    */
            """.trimMargin()
        }

        if (methodDef.result is ApiResult.Struct) {
            return """
            |    $methodMods long $methodSignCall; /*
            |        return (intptr_t)${if (methodDef.result.isRef) "&" else ""}${methodCall};
            |    */
            """.trimMargin()
        }

        if (methodDef.result is ApiResult.ImVec) {
            val dstWithArgsInSign = "dst${if (argsInSign.isNotEmpty()) ", $argsInSign" else ""}"
            val resultCpyMethodName = "Jni::${methodDef.result.javaClass.simpleName}Cpy"

            var methods = """
            |     $methodMods void $methodName(${methodDef.result.type} $dstWithArgsInSign); /*
            |        $resultCpyMethodName(env, $methodCall, dst);
            |     */
            |
            |     $methodMods float ${methodName}X($argsInSign); /*
            |        return $methodCall.x;
            |     */
            |     $methodMods float ${methodName}Y($argsInSign); /*
            |        return $methodCall.y;
            |     */
            """.trimMargin()

            if (methodDef.result is ApiResult.ImVec4) {
                methods += """
                |
                |    $methodMods float ${methodName}Z($argsInSign); /*
                |        return $methodCall.z;
                |     */
                |     $methodMods float ${methodName}W($argsInSign); /*
                |        return $methodCall.w;
                |     */
                """.trimMargin()
            }

            return methods
        }

        if (methodDef.result is ApiResult.Str) {
            return """
            |    $methodMods ${methodDef.result.type} $methodSignCall; /*
            |        return env->NewStringUTF($methodCall);
            |    */
            """.trimMargin()
        }

        return """
        |    $methodMods ${methodDef.result.type} $methodSignCall; /*
        |        return $methodCall;
        |    */
        """.trimMargin()
    }

    /**
     * Collect all available args into subgroups. That's required to handle optional args,
     * which a basically create a new arguments group without them.
     */
    private fun collectArgGroups(args: Collection<ApiArg>): Collection<ArgGroup> {
        val argGroups = mutableListOf<ArgGroup>()
        val group = mutableListOf<ApiArg>()
        for (arg in args) {
            // When the argument is optional we add a new group, which ends when the optional arg starts.
            // For example, if the "arg2" is optional we'll have: (arg1, arg2) -> (arg1) and (arg1, arg2)
            if (arg.optional) {
                argGroups.add(ArgGroup(group.toList()))
            }
            group.add(arg)
        }
        argGroups.add(ArgGroup(group))

        // Empty arguments is also a group.
        // While rendering it we will iterate through the empty list and won't render any args at all.
        if (argGroups.isEmpty()) {
            argGroups.add(ArgGroup())
        }

        // Process default arguments.
        argGroups.toList().forEach { argGroup ->
            argGroup.args.forEachIndexed { idx, arg ->
                arg.default?.let { defaultValue ->
                    // Ignore the default arg if it is optional and the last in the args list.
                    if (arg.optional && idx == argGroup.size - 1) {
                        return@let
                    }

                    val argsWithDefault = argGroup.args.toMutableList()
                    argsWithDefault[idx] = ApiArg.Default(defaultValue)
                    argGroups.add(ArgGroup(argsWithDefault))
                }
            }
        }

        return argGroups
    }

    private class ArgGroup(val args: Collection<ApiArg> = emptyList()) {
        val size: Int
            get() = args.size

        fun renderJavaInSignature(): String {
            return args.filter { it !is ApiArg.Default }.joinToString(transform = ApiArg::inSignatureJava)
        }

        fun renderJavaInBody(): String {
            return args.filter { it !is ApiArg.Default }.joinToString(transform = ApiArg::inBodyJava)
        }

        fun renderNativeInSignature(): String {
            return args.filter { it !is ApiArg.Default }.joinToString(transform = ApiArg::inSignatureNative)
        }

        fun renderNativeInBody(): String {
            return args.joinToString(transform = ApiArg::inBodyNative)
        }
    }
}
