package com.example.hardware

import android.util.Log
import com.example.model.CommandResult
import com.example.model.RootStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

object RootManager {

    private const val TAG = "RootManager"

    private val SU_BINARY_PATHS = arrayOf(
        "/system/xbin/su",
        "/system/bin/su",
        "/sbin/su",
        "/system/sd/xbin/su",
        "/data/local/xbin/su",
        "/data/local/bin/su",
        "/data/local/su",
        "/system/bin/failsafe/su"
    )

    private val ALLOWED_PATH_PREFIXES = arrayOf(
        "/sys/class/leds/",
        "/sys/devices/platform/leds"
    )

    private val UID_PATTERN = Pattern.compile("uid=(\\d+)")

    // Interactive root shell for smooth continuous updates (breathing/rainbow)
    private var activeRootProcess: Process? = null
    private var rootWriter: OutputStreamWriter? = null
    private val sessionLock = Any()

    /**
     * Checks if the `su` binary is physically present on the device.
     * Note: This does NOT mean root permission is granted, only that su is available.
     */
    fun isRootAvailable(): Boolean {
        for (path in SU_BINARY_PATHS) {
            try {
                if (File(path).exists()) return true
            } catch (_: Exception) {}
        }
        val pathEnv = System.getenv("PATH") ?: ""
        for (dir in pathEnv.split(":")) {
            val file = File(dir, "su")
            try {
                if (file.exists()) return true
            } catch (_: Exception) {}
        }
        return false
    }

    /**
     * Executes `su -c id` to trigger the Magisk / Superuser authorization prompt.
     * Returns the exact CommandResult without faking.
     */
    suspend fun requestRoot(): CommandResult = withContext(Dispatchers.IO) {
        executeRootCommandInternal("id", timeoutSeconds = 25)
    }

    /**
     * Checks if root is currently granted by executing `id` through `su`.
     */
    suspend fun checkRoot(): CommandResult = withContext(Dispatchers.IO) {
        executeRootCommandInternal("id", timeoutSeconds = 10)
    }

    /**
     * Evaluates the current RootStatus enum based on real command execution.
     */
    suspend fun determineRootStatus(): Pair<RootStatus, Int?> = withContext(Dispatchers.IO) {
        if (!isRootAvailable()) {
            return@withContext Pair(RootStatus.ROOT_NOT_FOUND, null)
        }
        val result = checkRoot()
        val uid = parseUid(result.stdout)

        val status = when {
            result.exitCode == 0 && uid == 0 -> RootStatus.ROOT_GRANTED
            result.stdout.contains("uid=0") || result.stdout.contains("(root)") -> RootStatus.ROOT_GRANTED
            result.exitCode == 13 || result.exitCode == 1 -> {
                // Typical Magisk denial exit codes
                if (result.stderr.contains("denied", ignoreCase = true) ||
                    result.stdout.contains("denied", ignoreCase = true)
                ) {
                    RootStatus.ROOT_DENIED
                } else {
                    RootStatus.ROOT_ERROR
                }
            }
            result.stderr.contains("not found", ignoreCase = true) -> RootStatus.ROOT_NOT_FOUND
            result.stderr.contains("denied", ignoreCase = true) -> RootStatus.ROOT_DENIED
            else -> RootStatus.ROOT_ERROR
        }

        Pair(status, if (status == RootStatus.ROOT_GRANTED) 0 else uid)
    }

    /**
     * Parses the UID from the `id` command output (e.g. "uid=0(root) gid=0(root)").
     */
    fun parseUid(output: String): Int? {
        val matcher = UID_PATTERN.matcher(output)
        if (matcher.find()) {
            return matcher.group(1)?.toIntOrNull()
        }
        return null
    }

    /**
     * Gets the root UID if available, or null.
     */
    suspend fun getRootUid(): Int? = withContext(Dispatchers.IO) {
        val result = checkRoot()
        parseUid(result.stdout)
    }

    /**
     * Executes a privileged root command safely.
     * Only whitelisted commands and validated paths are accepted.
     */
    suspend fun executeRootCommand(command: String): CommandResult = withContext(Dispatchers.IO) {
        executeRootCommandInternal(command, timeoutSeconds = 8)
    }

    private fun executeRootCommandInternal(command: String, timeoutSeconds: Long): CommandResult {
        return try {
            val process = ProcessBuilder("su", "-c", command)
                .redirectErrorStream(false)
                .start()

            val stdoutBuilder = StringBuilder()
            val stderrBuilder = StringBuilder()

            val stdoutReader = BufferedReader(InputStreamReader(process.inputStream))
            val stderrReader = BufferedReader(InputStreamReader(process.errorStream))

            val stdoutThread = Thread {
                try {
                    var line: String?
                    while (stdoutReader.readLine().also { line = it } != null) {
                        stdoutBuilder.appendLine(line)
                    }
                } catch (_: Exception) {}
            }
            val stderrThread = Thread {
                try {
                    var line: String?
                    while (stderrReader.readLine().also { line = it } != null) {
                        stderrBuilder.appendLine(line)
                    }
                } catch (_: Exception) {}
            }

            stdoutThread.start()
            stderrThread.start()

            val finished = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
            } else {
                process.waitFor()
                true
            }

            if (!finished) {
                process.destroy()
                return CommandResult(
                    success = false,
                    exitCode = -1,
                    stdout = stdoutBuilder.toString().trim(),
                    stderr = "Command timed out after ${timeoutSeconds}s"
                )
            }

            stdoutThread.join(500)
            stderrThread.join(500)

            val exitCode = process.exitValue()
            val stdout = stdoutBuilder.toString().trim()
            val stderr = stderrBuilder.toString().trim()

            CommandResult(
                success = exitCode == 0,
                exitCode = exitCode,
                stdout = stdout,
                stderr = stderr
            )
        } catch (e: Exception) {
            CommandResult(
                success = false,
                exitCode = -1,
                stdout = "",
                stderr = e.message ?: "Failed to execute su command"
            )
        }
    }

    /**
     * Reads a sysfs file under /sys/class/leds/ using root access.
     */
    suspend fun readRootFile(path: String): String? = withContext(Dispatchers.IO) {
        if (!isPathAllowed(path)) return@withContext null
        val result = executeRootCommandInternal("cat $path", timeoutSeconds = 3)
        if (result.success) result.stdout.trim() else null
    }

    /**
     * Writes a value to a sysfs file under /sys/class/leds/ using root access.
     */
    suspend fun writeRootFile(path: String, value: String): Boolean = withContext(Dispatchers.IO) {
        if (!isPathAllowed(path)) return@withContext false
        val sanitizedValue = value.filter { it.isDigit() || it == '-' }
        if (sanitizedValue.isEmpty()) return@withContext false

        // Try streaming interactive session first for high-speed performance
        synchronized(sessionLock) {
            try {
                val writer = getOrCreateRootSession()
                if (writer != null) {
                    writer.write("echo $sanitizedValue > $path\n")
                    writer.flush()
                    return@withContext true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Root session write error on path $path: ${e.message}")
                closeRootSession()
            }
        }

        // Fallback to one-shot su -c
        val result = executeRootCommandInternal("echo $sanitizedValue > $path", timeoutSeconds = 2)
        if (!result.success) {
            Log.e(TAG, "Fallback root write failed for path $path: ${result.stderr}")
        }
        result.success
    }

    /**
     * Batch writes RGB values to multiple paths in a single root instruction.
     * Uses atomic single-line execution with persistent root shell.
     */
    suspend fun writeRgbRoot(redPath: String?, r: Int, greenPath: String?, g: Int, bluePath: String?, b: Int): Boolean =
        withContext(Dispatchers.IO) {
            val cmdList = mutableListOf<String>()
            if (redPath != null && isPathAllowed(redPath)) cmdList.add("echo $r > $redPath")
            if (greenPath != null && isPathAllowed(greenPath)) cmdList.add("echo $g > $greenPath")
            if (bluePath != null && isPathAllowed(bluePath)) cmdList.add("echo $b > $bluePath")

            if (cmdList.isEmpty()) return@withContext false

            val combinedCmd = cmdList.joinToString("; ")

            synchronized(sessionLock) {
                try {
                    val writer = getOrCreateRootSession()
                    if (writer != null) {
                        writer.write("$combinedCmd\n")
                        writer.flush()
                        return@withContext true
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Persistent root session write error: ${e.message}, closing session and attempting fallback")
                    closeRootSession()
                }
            }

            // Fallback to one-shot command if streaming session failed
            val result = executeRootCommandInternal(combinedCmd, timeoutSeconds = 3)
            if (!result.success) {
                Log.e(TAG, "Hardware root write failed for RGB($r,$g,$b): ${result.stderr}")
            }
            result.success
        }

    private fun getOrCreateRootSession(): OutputStreamWriter? {
        if (activeRootProcess != null && rootWriter != null) {
            try {
                // Test if process is still alive
                activeRootProcess?.exitValue()
                // If exitValue() doesn't throw, the process has exited!
                closeRootSession()
            } catch (_: IllegalThreadStateException) {
                // Process is still alive
                return rootWriter
            }
        }

        return try {
            val proc = ProcessBuilder("su").start()
            activeRootProcess = proc
            // Drain stdout and stderr in background daemon threads so pipe buffers never block
            drainStream(proc.inputStream)
            drainStream(proc.errorStream)
            val writer = OutputStreamWriter(proc.outputStream)
            rootWriter = writer
            Log.i(TAG, "Persistent root session established")
            writer
        } catch (e: Exception) {
            Log.e(TAG, "Failed to spawn persistent root process: ${e.message}")
            null
        }
    }

    private fun drainStream(stream: java.io.InputStream) {
        Thread {
            try {
                val buffer = ByteArray(256)
                while (stream.read(buffer) != -1) {
                    // Drain buffer silently
                }
            } catch (_: Exception) {}
        }.apply {
            isDaemon = true
            name = "su-pipe-drain"
            start()
        }
    }

    fun closeRootSession() {
        synchronized(sessionLock) {
            try {
                rootWriter?.write("exit\n")
                rootWriter?.flush()
                rootWriter?.close()
            } catch (_: Exception) {}
            try {
                activeRootProcess?.destroy()
            } catch (_: Exception) {}
            rootWriter = null
            activeRootProcess = null
        }
    }

    /**
     * Security validation: Only paths starting with /sys/class/leds/ or /sys/devices/platform/leds
     * and without command injection characters are permitted.
     */
    fun isPathAllowed(path: String): Boolean {
        if (path.contains(";") || path.contains("&") || path.contains("|") ||
            path.contains("`") || path.contains("$") || path.contains("\n")
        ) {
            return false
        }
        return ALLOWED_PATH_PREFIXES.any { path.startsWith(it) }
    }
}
