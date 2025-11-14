using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.InteropServices;
using L2Guard.Client.Signatures;

namespace L2Guard.Client.Core
{
    /// <summary>
    /// Scans for injected DLLs in the game process
    /// </summary>
    public class ModuleScanner
    {
        [DllImport("kernel32.dll")]
        private static extern IntPtr OpenProcess(int dwDesiredAccess, bool bInheritHandle, int dwProcessId);

        [DllImport("kernel32.dll")]
        private static extern bool CloseHandle(IntPtr hObject);

        [DllImport("psapi.dll", SetLastError = true)]
        private static extern bool EnumProcessModules(IntPtr hProcess, [Out] IntPtr[] lphModule, uint cb, out uint lpcbNeeded);

        [DllImport("psapi.dll")]
        private static extern uint GetModuleFileNameEx(IntPtr hProcess, IntPtr hModule, [Out] System.Text.StringBuilder lpBaseName, uint nSize);

        private const int PROCESS_QUERY_INFORMATION = 0x0400;
        private const int PROCESS_VM_READ = 0x0010;

        public class ModuleScanResult
        {
            public bool SuspiciousModulesFound { get; set; }
            public List<SuspiciousModule> SuspiciousModules { get; set; } = new();
            public List<string> AllModules { get; set; } = new();
            public DateTime ScanTime { get; set; }
        }

        public class SuspiciousModule
        {
            public string ModuleName { get; set; } = string.Empty;
            public string ModulePath { get; set; } = string.Empty;
            public string Reason { get; set; } = string.Empty;
            public string? MatchedBotName { get; set; }
            public int ThreatLevel { get; set; }
        }

        /// <summary>
        /// Scan modules loaded in a specific process
        /// </summary>
        public ModuleScanResult ScanProcessModules(int processId)
        {
            var result = new ModuleScanResult
            {
                ScanTime = DateTime.UtcNow
            };

            IntPtr hProcess = IntPtr.Zero;

            try
            {
                hProcess = OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, false, processId);
                if (hProcess == IntPtr.Zero)
                {
                    throw new Exception("Cannot open process for module scanning");
                }

                // Get module handles
                uint cbNeeded;
                if (!EnumProcessModules(hProcess, null, 0, out cbNeeded))
                {
                    return result;
                }

                uint moduleCount = cbNeeded / (uint)IntPtr.Size;
                IntPtr[] moduleHandles = new IntPtr[moduleCount];

                if (!EnumProcessModules(hProcess, moduleHandles, cbNeeded, out cbNeeded))
                {
                    return result;
                }

                // Enumerate each module
                foreach (var moduleHandle in moduleHandles)
                {
                    var modulePath = new System.Text.StringBuilder(1024);
                    GetModuleFileNameEx(hProcess, moduleHandle, modulePath, (uint)modulePath.Capacity);

                    if (modulePath.Length > 0)
                    {
                        var path = modulePath.ToString();
                        var fileName = System.IO.Path.GetFileName(path);

                        result.AllModules.Add(path);

                        // Check against known bot DLLs
                        var botSignature = KnownBots.FindByDLL(fileName);
                        if (botSignature != null)
                        {
                            result.SuspiciousModules.Add(new SuspiciousModule
                            {
                                ModuleName = fileName,
                                ModulePath = path,
                                Reason = "Known bot DLL",
                                MatchedBotName = botSignature.Name,
                                ThreatLevel = botSignature.ThreatLevel
                            });
                            continue;
                        }

                        // Check against suspicious patterns
                        if (KnownBots.DLLMatchesSuspiciousPattern(fileName))
                        {
                            result.SuspiciousModules.Add(new SuspiciousModule
                            {
                                ModuleName = fileName,
                                ModulePath = path,
                                Reason = "Suspicious DLL pattern",
                                ThreatLevel = 5
                            });
                            continue;
                        }

                        // Check for unsigned or suspicious DLLs in game directory
                        if (IsInGameDirectory(path) && !IsWhitelistedDLL(fileName))
                        {
                            result.SuspiciousModules.Add(new SuspiciousModule
                            {
                                ModuleName = fileName,
                                ModulePath = path,
                                Reason = "Unknown DLL in game directory",
                                ThreatLevel = 6
                            });
                        }
                    }
                }

                result.SuspiciousModulesFound = result.SuspiciousModules.Count > 0;
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Error scanning modules: {ex.Message}");
            }
            finally
            {
                if (hProcess != IntPtr.Zero)
                {
                    CloseHandle(hProcess);
                }
            }

            return result;
        }

        /// <summary>
        /// Scan modules in the current process
        /// </summary>
        public ModuleScanResult ScanCurrentProcess()
        {
            return ScanProcessModules(Process.GetCurrentProcess().Id);
        }

        /// <summary>
        /// Check if a DLL is in the game directory (potential injection)
        /// </summary>
        private bool IsInGameDirectory(string path)
        {
            try
            {
                var gameDir = AppDomain.CurrentDomain.BaseDirectory;
                return path.StartsWith(gameDir, StringComparison.OrdinalIgnoreCase);
            }
            catch
            {
                return false;
            }
        }

        /// <summary>
        /// Whitelist of legitimate DLLs that can be in the game directory
        /// </summary>
        private readonly HashSet<string> _whitelistedDLLs = new(StringComparer.OrdinalIgnoreCase)
        {
            "l2guard.client.dll",
            "l2.exe",
            "l2.bin",
            "engine.dll",
            "core.dll",
            "d3d9.dll",
            "d3d11.dll",
            "opengl32.dll",
            "xinput.dll",
            // Add legitimate game DLLs here
        };

        private bool IsWhitelistedDLL(string fileName)
        {
            return _whitelistedDLLs.Contains(fileName) ||
                   fileName.StartsWith("microsoft.", StringComparison.OrdinalIgnoreCase) ||
                   fileName.StartsWith("system.", StringComparison.OrdinalIgnoreCase);
        }

        /// <summary>
        /// Monitor for new module loads
        /// </summary>
        public void StartModuleMonitoring(int processId, Action<SuspiciousModule> onSuspiciousModule, int intervalMs = 3000)
        {
            var knownModules = new HashSet<string>();
            var timer = new System.Timers.Timer(intervalMs);

            timer.Elapsed += (sender, e) =>
            {
                try
                {
                    var result = ScanProcessModules(processId);
                    foreach (var module in result.SuspiciousModules)
                    {
                        if (!knownModules.Contains(module.ModulePath))
                        {
                            knownModules.Add(module.ModulePath);
                            onSuspiciousModule(module);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Debug.WriteLine($"Error in module monitoring: {ex.Message}");
                }
            };

            timer.Start();
        }

        /// <summary>
        /// Compare current modules with baseline (detect new injections)
        /// </summary>
        public List<string> DetectNewModules(List<string> baselineModules, List<string> currentModules)
        {
            return currentModules.Except(baselineModules).ToList();
        }
    }
}
