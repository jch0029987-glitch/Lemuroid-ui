package com.swordfish.lemuroid.common.system

import android.content.Context
import android.content.pm.PackageManager
import android.opengl.GLES20
import android.os.Build
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLContext

object GpuInfo {

    enum class MaliArchitecture(val generation: String) {
        UTGARD("Utgard (Old: Mali-400/450)"),
        MIDGARD("Midgard (T-Series: Mali-T6xx/7xx/8xx)"),
        BIFROST("Bifrost (G-Series: Mali-G31/51/71/72/76)"),
        VALHALL("Valhall (New: Mali-G77/78/710/715/G610)"),
        UNKNOWN("Unknown Mali Architecture")
    }

    private var cachedRenderer: String? = null
    private var cachedVendor: String? = null

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
     * Specifically detects the Mali Architecture generation.
     * Useful for applying specific shader hacks or performance profiles.
     */
    fun getMaliArchitecture(context: Context): MaliArchitecture {
        val renderer = getRenderer(context).uppercase()
        if (!renderer.contains("MALI")) return MaliArchitecture.UNKNOWN

        return when {
            // Valhall: G77, G78, G710, G715, G610, G615, G310
            renderer.matches(Regex(".*MALI-G[367][17][05].*")) -> MaliArchitecture.VALHALL
            // Bifrost: G31, G51, G52, G71, G72, G76
            renderer.matches(Regex(".*MALI-G[357][126].*")) -> MaliArchitecture.BIFROST
            // Midgard: T604, T628, T720, T760, T820, T830, T860, T880
            renderer.contains("MALI-T") -> MaliArchitecture.MIDGARD
            // Utgard: Mali-300, 400, 450, 470
            renderer.matches(Regex(".*MALI-[34][057]0.*")) -> MaliArchitecture.UTGARD
            else -> MaliArchitecture.UNKNOWN
        }
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

            egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
            egl.eglDestroySurface(display, surface)
            egl.eglDestroyContext(display, context)
            egl.eglTerminate(display)
        } catch (e: Exception) {
            cachedRenderer = "Detection Failed"
            cachedVendor = "Unknown"
        }
    }
}
