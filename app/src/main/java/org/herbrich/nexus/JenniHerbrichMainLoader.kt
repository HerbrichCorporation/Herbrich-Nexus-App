package org.herbrich.nexus

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Razor-safe: Gleiche Namen wie window.JenniLoader in deiner VBHTML
object JenniHerbrichMainLoader {

    sealed class LoaderType {
        data class Fullscreen(
            val text: String = "RED QUEEN // LOADING",
            val sub: String = "SYSTEM ACTIVE // MATRIX.HERBRICH.ORG",
            val colorHex: String = "#FF0000",
            val progress: Int? = null // null = animiert, 0-100 = setProgress
        ) : LoaderType()

        data class Inline(
            val slotId: String, // = containerId aus showInline()
            val text: String = "LOADING...",
            val sub: String = "",
            val colorHex: String = "#FF0000"
        ) : LoaderType()

        data class Mini(
            val slotId: String,
            val colorClass: String = "red" // red / blue / green oder #hex
        ) : LoaderType()
    }

    data class LoaderState(
        val isVisible: Boolean = false,
        val type: LoaderType = LoaderType.Fullscreen(),
        val loaderColor: Color = Color(0xFFFF0000)
    )

    private val _state = MutableStateFlow(LoaderState())
    val state: StateFlow<LoaderState> = _state.asStateFlow()

    // === FULLSCREEN - wie showFullscreen / show / showLoader ===
    fun showFullscreen(text: String? = null, sub: String? = null, color: String? = null, progress: Int? = null) {
        val current = _state.value.type as? LoaderType.Fullscreen ?: LoaderType.Fullscreen()
        _state.value = LoaderState(
            isVisible = true,
            type = current.copy(
                text = text ?: current.text,
                sub = sub ?: current.sub,
                colorHex = color ?: current.colorHex,
                progress = progress ?: current.progress
            ),
            loaderColor = parseColor(color ?: current.colorHex)
        )
    }
    fun hideFullscreen() { _state.value = _state.value.copy(isVisible = false) }

    // === INLINE GRID - wie showInline ===
    fun showInline(containerId: String, text: String? = null, sub: String? = null, color: String? = null) {
        _state.value = LoaderState(
            isVisible = true,
            type = LoaderType.Inline(containerId, text ?: "LOADING...", sub ?: "", color ?: "#FF0000"),
            loaderColor = parseColor(color ?: "#FF0000")
        )
    }
    fun hideInline(slotId: String? = null) {
        if (slotId == null || (_state.value.type as? LoaderType.Inline)?.slotId == slotId) {
            _state.value = _state.value.copy(isVisible = false)
        }
    }

    // === MINI - wie showMini ===
    fun showMini(targetIdOrEl: String, colorClass: String? = null) {
        _state.value = LoaderState(
            isVisible = true,
            type = LoaderType.Mini(targetIdOrEl, colorClass ?: "red"),
            loaderColor = parseColor(colorClass)
        )
    }
    fun hideMini(targetIdOrEl: String? = null) { _state.value = _state.value.copy(isVisible = false) }

    // === HELPERS - wie setColor / setProgress aus deinem JS ===
    fun setColor(c: String) { _state.value = _state.value.copy(loaderColor = parseColor(c)) }
    fun setProgress(pct: Int, text: String? = null) {
        val cur = _state.value.type as? LoaderType.Fullscreen ?: LoaderType.Fullscreen()
        _state.value = _state.value.copy(type = cur.copy(progress = pct, sub = text ?: cur.sub))
    }

    // Aliases für Backward Compat wie in deinem JS
    fun show(text: String? = null, sub: String? = null, color: String? = null, progress: Int? = null) = showFullscreen(text, sub, color, progress)
    fun hide() = hideFullscreen()

    private fun parseColor(input: String?): Color {
        if (input == null) return Color(0xFFFF0000)
        return try {
            if (input.startsWith("#")) Color(android.graphics.Color.parseColor(input))
            else when(input.lowercase()) {
                "red" -> Color(0xFFFF0000)
                "blue" -> Color(0xFF00B7FF)
                "green" -> Color(0xFF00FF00)
                else -> Color(0xFFFF0000)
            }
        } catch (e: Exception) { Color(0xFFFF0000) }
    }
}