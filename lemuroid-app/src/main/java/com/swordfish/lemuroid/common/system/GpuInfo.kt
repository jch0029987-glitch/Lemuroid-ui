package com.swordfish.lemuroid.common.system

import android.content.Context
import android.content.pm.PackageManager
import android.opengl.GLES20
import android.os.Build
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext

object GpuInfo {

    enum class MaliArchitecture(val generation: String, val supportsTE: Boolean, val supportsAFBC: Boolean) {
        UTGARD("Utgard (Mali-400)", supportsTE = false, supportsAFBC = false),
        MIDGARD("Midgard (T-Series)", supportsTE = true, supportsAFBC = false),
        BIFROST("Bifrost (G-Series)", supportsTE = true, supportsAFBC = true),
        VALHALL("Valhall (G-7xx)", supportsTE = true, supportsAFBC = true),
        UNKNOWN("Unknown", supportsTE = false, supportsAFBC = false)
    }

    private var cachedRenderer: String? = null
    private var cachedVendor: String? = null
    private var cachedExtensions: String? = null

    fun getVendor(context: Context): String {
        if (cachedVendor == null) detectGpuDetails()
        val renderer = cachedRenderer?.lowercase() ?: ""
        val vendor = cachedVendor?.lowercase() ?: ""
        
        return when {
            vendor.contains("qualcomm") || renderer.contains("adreno") -> "Qualcomm"
            vendor.contains("arm") || renderer.contains("mali") -> "ARM"
            else -> cachedVendor ?: "Unknown"
        }
    }

    fun getRenderer(context: Context): String {
        if (cachedRenderer == null) detectGpuDetails()
        return cachedRenderer ?: "Unknown"
    }

    /**
     * Detects specific Mali features like Transaction Elimination (TE) 
     * which helps reduce redundant tile writes to memory.
     */
    fun getMaliArchitecture(context: Context): MaliArchitecture {
        val renderer = getRenderer(context).uppercase()
        if (!renderer.contains("MALI")) return MaliArchitecture.UNKNOWN

        return when {
            renderer.matches(Regex(".*MALI-G[367][17][05].*")) -> MaliArchitecture.VALHALL
            renderer.matches(Regex(".*MALI-G[357][126].*")) -> MaliArchitecture.BIFROST
            renderer.contains("MALI-T") -> MaliArchitecture.MIDGARD
            renderer.matches(Regex(".*MALI-[34][057]0.*")) -> MaliArchitecture.UTGARD
            else -> MaliArchitecture.UNKNOWN
        }
    }

    /**
     * Checks if the current Mali GPU supports Transaction Elimination.
     * This allows the emulator to skip rendering tiles that haven't changed.
     */
    fun supportsTransactionElimination(context: Context): Boolean {
        return getMaliArchitecture(context).supportsTE
    }

    /**
     * Checks for AFBC (Arm Frame Buffer Compression) support.
     * AFBC reduces bandwidth for textures and framebuffers.
     */
    fun supportsAFBC(context: Context): Boolean {
        if (cachedExtensions == null) detectGpuDetails()
        val architecture = getMaliArchitecture(context)
        
        // Check both architecture default and extension string for safety
        return architecture.supportsAFBC || 
               cachedExtensions?.contains("GL_ARM_shader_framebuffer_fetch") == true
    }

    fun isVulkanSupported(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_VERSION)
    }

    private fun detectGpuDetails() {
        try {
            val egl = EGLContext.getEGL() as EGL10
            val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            val version = IntArray(2)
            egl.eglInitialize(display, version)

            val confAttr = intArrayOf(EGL10.EGL_RENDERABLE_TYPE, 4, EGL10.EGL_NONE)
            val configs = arrayOfNulls<javax.microedition.khronos.egl.EGLConfig>(1)
            val numConfigs = IntArray(1)
            egl.eglChooseConfig(display, confAttr, configs, 1, numConfigs)
            val config = configs[0] ?: return

            val ctxAttr = intArrayOf(0x3098, 2, EGL10.EGL_NONE) 
            val context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, ctxAttr)
            val surfAttr = intArrayOf(EGL10.EGL_WIDTH, 1, EGL10.EGL_HEIGHT, 1, EGL10.EGL_NONE)
            val surface = egl.eglCreatePbufferSurface(display, config, surfAttr)

            egl.eglMakeCurrent(display, surface, surface, context)
            
            cachedRenderer = GLES20.glGetString(GLES20.GL_RENDERER)
            cachedVendor = GLES20.glGetString(GLES20.GL_VENDOR)
            cachedExtensions = GLES20.glGetString(GLES20.GL_EXTENSIONS)

            egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
            egl.eglDestroySurface(display, surface)
            egl.eglDestroyContext(display, context)
            egl.eglTerminate(display)
        } catch (e: Exception) {
            cachedRenderer = "Detection Failed"
            cachedVendor = "Unknown"
            cachedExtensions = ""
        }
    }
}
