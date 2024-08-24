package com.youaji.libs.debug.crash.util

const val logPrefixDebugCrash = "debug-crash:"

inline fun logI_LDC(message: Any?) =
    com.youaji.libs.util.logger.logInfo("$logPrefixDebugCrash$message")

inline fun logD_LDC(message: Any?) =
    com.youaji.libs.util.logger.logDebug("$logPrefixDebugCrash$message")

inline fun logW_LDC(message: Any?) =
    com.youaji.libs.util.logger.logWarn("$logPrefixDebugCrash$message")

inline fun logE_LDC(message: Any?) =
    com.youaji.libs.util.logger.logError("$logPrefixDebugCrash$message")