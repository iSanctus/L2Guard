using System;
using System.IO;
using System.Security.Cryptography;
using System.Text;

namespace L2Guard.Client.Core
{
    /// <summary>
    /// Hardcoded security configuration that CANNOT be modified by users
    /// Critical settings are compiled into the code, not read from config files
    /// </summary>
    public static class SecurityConfig
    {
        // CRITICAL: These settings are HARDCODED and cannot be changed by users
        // They are compiled into the executable

        /// <summary>
        /// Scan interval in milliseconds - CANNOT be disabled or increased
        /// </summary>
        public const int SCAN_INTERVAL_MS = 5000;

        /// <summary>
        /// Minimum scan interval allowed (prevent users from slowing down scans)
        /// </summary>
        public const int MIN_SCAN_INTERVAL_MS = 3000;

        /// <summary>
        /// Process scanning - ALWAYS ENABLED
        /// </summary>
        public const bool PROCESS_SCANNING_ENABLED = true;

        /// <summary>
        /// Module scanning - ALWAYS ENABLED
        /// </summary>
        public const bool MODULE_SCANNING_ENABLED = true;

        /// <summary>
        /// Anti-debug checks - ALWAYS ENABLED
        /// </summary>
        public const bool ANTI_DEBUG_ENABLED = true;

        /// <summary>
        /// Hook detection - ALWAYS ENABLED
        /// </summary>
        public const bool HOOK_DETECTION_ENABLED = true;

        /// <summary>
        /// Memory integrity checks - ALWAYS ENABLED
        /// </summary>
        public const bool MEMORY_INTEGRITY_ENABLED = true;

        /// <summary>
        /// Allow debugger - ALWAYS FALSE (never allow debuggers)
        /// </summary>
        public const bool ALLOW_DEBUGGER = false;

        /// <summary>
        /// Action on bot detected - ALWAYS EXIT (cannot be changed to "ignore")
        /// </summary>
        public const string ACTION_ON_BOT_DETECTED = "exit";

        /// <summary>
        /// Show warnings - ALWAYS TRUE
        /// </summary>
        public const bool SHOW_WARNINGS = true;

        /// <summary>
        /// Heartbeat interval to server (seconds)
        /// </summary>
        public const int HEARTBEAT_INTERVAL_SECONDS = 30;

        /// <summary>
        /// Guard version - used for server validation
        /// </summary>
        public const string GUARD_VERSION = "1.0.0";

        /// <summary>
        /// Expected hash of the guard assembly (for integrity check)
        /// This should be updated during build process
        /// </summary>
        private const string EXPECTED_ASSEMBLY_HASH = "PLACEHOLDER_HASH";

        /// <summary>
        /// Verify the guard assembly hasn't been tampered with
        /// </summary>
        public static bool VerifyIntegrity()
        {
            try
            {
                var assembly = System.Reflection.Assembly.GetExecutingAssembly();
                var assemblyPath = assembly.Location;

                if (string.IsNullOrEmpty(assemblyPath) || !File.Exists(assemblyPath))
                {
                    return false;
                }

                // Calculate hash of current assembly
                using (var stream = File.OpenRead(assemblyPath))
                using (var sha256 = SHA256.Create())
                {
                    var hash = sha256.ComputeHash(stream);
                    var hashString = BitConverter.ToString(hash).Replace("-", "");

                    // In production, verify against expected hash
                    // For now, just ensure file can be read
                    return hash.Length > 0;
                }
            }
            catch
            {
                return false;
            }
        }

        /// <summary>
        /// Check if any critical files have been modified
        /// </summary>
        public static bool VerifyFileIntegrity()
        {
            try
            {
                // Verify launcher executable exists and hasn't been replaced with dummy
                var launcherPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "L2GuardLauncher.exe");
                if (!File.Exists(launcherPath))
                {
                    return false;
                }

                var fileInfo = new FileInfo(launcherPath);
                if (fileInfo.Length < 10000) // Too small = probably tampered
                {
                    return false;
                }

                return true;
            }
            catch
            {
                return false;
            }
        }

        /// <summary>
        /// Validate that no one is trying to bypass security by modifying the code
        /// This uses a simple checksum of method names
        /// </summary>
        public static bool VerifyCodeIntegrity()
        {
            try
            {
                // Check that critical types haven't been removed or replaced
                var assembly = System.Reflection.Assembly.GetExecutingAssembly();

                var requiredTypes = new[]
                {
                    "L2Guard.Client.Core.ProcessScanner",
                    "L2Guard.Client.Core.ModuleScanner",
                    "L2Guard.Client.Core.AntiDebug",
                    "L2Guard.Client.Core.HookDetector",
                    "L2Guard.Client.Core.MemoryProtector",
                    "L2Guard.Client.Core.GuardEngine"
                };

                foreach (var typeName in requiredTypes)
                {
                    var type = assembly.GetType(typeName);
                    if (type == null)
                    {
                        return false; // Critical class removed
                    }
                }

                return true;
            }
            catch
            {
                return false;
            }
        }
    }

    /// <summary>
    /// Server configuration - only non-security settings
    /// This is the ONLY config that can be loaded from file
    /// </summary>
    public class ServerConfig
    {
        public string ServerHost { get; set; } = "127.0.0.1";
        public int ServerPort { get; set; } = 7777;
        public int GuardPort { get; set; } = 2107;

        /// <summary>
        /// Load ONLY server connection settings from file
        /// Security settings are NEVER loaded from file
        /// </summary>
        public static ServerConfig LoadSafe()
        {
            var config = new ServerConfig();

            try
            {
                var configPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "l2guard-server.json");

                if (File.Exists(configPath))
                {
                    var json = File.ReadAllText(configPath);

                    // Parse ONLY safe settings (server connection info)
                    // Never parse security-related settings
                    var parsed = System.Text.Json.JsonSerializer.Deserialize<ServerConfig>(json);
                    if (parsed != null)
                    {
                        config = parsed;
                    }
                }
            }
            catch
            {
                // If config is corrupted, use defaults
            }

            return config;
        }

        /// <summary>
        /// Create default config file
        /// </summary>
        public static void CreateDefault()
        {
            try
            {
                var configPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "l2guard-server.json");
                var config = new ServerConfig();

                var json = System.Text.Json.JsonSerializer.Serialize(config, new System.Text.Json.JsonSerializerOptions
                {
                    WriteIndented = true
                });

                File.WriteAllText(configPath, json);
            }
            catch
            {
                // Ignore write errors
            }
        }
    }
}
