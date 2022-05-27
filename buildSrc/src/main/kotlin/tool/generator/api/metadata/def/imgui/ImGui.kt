package tool.generator.api.metadata.def.imgui

import tool.generator.api.metadata.ApiMetadata
import tool.generator.api.metadata.ApiResult

class ImGui : ApiMetadata() {
    init {
        receiver = "ImGui::"
        static = true

        // Context creation and access
        method("CreateContext", ApiResult.Struct("imgui.internal.ImGuiContext")) {
            args(argStruct("imgui.ImFontAtlas", "sharedFontAtlas", true))
        }
        method("DestroyContext") {
            args(argStruct("imgui.internal.ImGuiContext", "ctx", true))
        }
        method("GetCurrentContext", ApiResult.Struct("imgui.internal.ImGuiContext", static = true))
        method("SetCurrentContext") {
            args(argStruct("imgui.internal.ImGuiContext", "ctx"))
        }

        // Main
        method("GetIO", ApiResult.Struct("imgui.ImGuiIO", static = true, isRef = true))
        method("GetStyle", ApiResult.Struct("imgui.ImGuiStyle", static = true, isRef = true))
        method("NewFrame")
        method("EndFrame")
        method("Render")
        method("GetDrawData", ApiResult.Struct("imgui.ImDrawData", static = true))

        // Demo, Debug, Information
        method("ShowDemoWindow") { args(argBoolPtr("open", true)) }
        method("ShowMetricsWindow") { args(argBoolPtr("open", true)) }
        method("ShowStackToolWindow") { args(argBoolPtr("open", true)) }
        method("ShowStyleEditor") { args(argStruct("imgui.ImGuiStyle", "ref", true)) }
        method("ShowStyleSelector", ApiResult.Bool()) { args(argStr("label")) }
        method("ShowFontSelector") { args(argStr("label")) }
        method("ShowUserGuide")
        method("GetVersion", ApiResult.Str())

        // Styles
        method("StyleColorsDark") { args(argStruct("imgui.ImGuiStyle", "dst", true)) }
        method("StyleColorsLight") { args(argStruct("imgui.ImGuiStyle", "dst", true)) }
        method("StyleColorsClassic") { args(argStruct("imgui.ImGuiStyle", "dst", true)) }

        // Windows
        method("Begin", ApiResult.Bool()) { args(
            argStr("name"),
            argBoolPtr("pOpen", true, "NULL"),
            argInt("flags", true)
        )}
        method("End")

        // Child Windows
        method("BeginChild", ApiResult.Bool()) { args(
            argStr("strId"),
            argImVec2("size", true, "ImVec2(0, 0)"),
            argBool("border", true, "false"),
            argInt("flags", true)
        )}
        method("BeginChild", ApiResult.Bool()) { args(
            argInt("id"),
            argImVec2("size", true),
            argBool("border", true),
            argInt("flags", true)
        )}
        method("EndChild")

        // Windows Utilities
        method("IsWindowAppearing", ApiResult.Bool())
        method("IsWindowCollapsed", ApiResult.Bool())
        method("IsWindowFocused", ApiResult.Bool()) { args(argInt("flags", true)) }
        method("IsWindowHovered", ApiResult.Bool()) { args(argInt("flags", true)) }
        method("GetWindowDrawList", ApiResult.Struct("imgui.ImDrawList"))
        method("GetWindowDpiScale", ApiResult.Float())
        method("GetWindowPos", ApiResult.ImVec2())
        method("GetWindowSize", ApiResult.ImVec2())
        method("GetWindowWidth", ApiResult.Float())
        method("GetWindowHeight", ApiResult.Float())
        method("GetWindowViewport", ApiResult.Struct("imgui.ImGuiViewport"))

        // Window manipulation
        method("SetNextWindowPos") { args(
            argImVec2("pos"),
            argInt("cond", true),
            argImVec2("pivot", true)
        )}

        method("GetStyleColorVec4", ApiResult.ImVec4()) { args(argInt("idx")) }
    }
}
