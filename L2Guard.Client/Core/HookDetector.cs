using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Runtime.InteropServices;

namespace L2Guard.Client.Core
{
    /// <summary>
    /// Detects API hooks used by bots to intercept game functions
    /// </summary>
    public class HookDetector
    {
        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern IntPtr GetModuleHandle(string lpModuleName);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern IntPtr GetProcAddress(IntPtr hModule, string lpProcName);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern bool ReadProcessMemory(IntPtr hProcess, IntPtr lpBaseAddress, byte[] lpBuffer,
            int dwSize, out int lpNumberOfBytesRead);

        public class HookDetectionResult
        {
            public bool HooksDetected { get; set; }
            public List<DetectedHook> DetectedHooks { get; set; } = new();
            public DateTime ScanTime { get; set; }
        }

        public class DetectedHook
        {
            public string FunctionName { get; set; } = string.Empty;
            public string ModuleName { get; set; } = string.Empty;
            public string HookType { get; set; } = string.Empty;
            public string Evidence { get; set; } = string.Empty;
        }

        /// <summary>
        /// Critical Windows APIs that bots commonly hook
        /// </summary>
        private readonly Dictionary<string, List<string>> _criticalApis = new()
        {
            ["kernel32.dll"] = new List<string>
            {
                "ReadProcessMemory",
                "WriteProcessMemory",
                "VirtualProtect",
                "VirtualAlloc",
                "CreateRemoteThread"
            },
            ["user32.dll"] = new List<string>
            {
                "SendMessage",
                "PostMessage",
                "GetAsyncKeyState",
                "GetCursorPos",
                "SetCursorPos",
                "mouse_event"
            },
            ["ws2_32.dll"] = new List<string>
            {
                "send",
                "recv",
                "sendto",
                "recvfrom",
                "WSASend",
                "WSARecv"
            }
        };

        /// <summary>
        /// Scan for hooked APIs
        /// </summary>
        public HookDetectionResult ScanForHooks()
        {
            var result = new HookDetectionResult
            {
                ScanTime = DateTime.UtcNow
            };

            try
            {
                foreach (var module in _criticalApis)
                {
                    var hModule = GetModuleHandle(module.Key);
                    if (hModule == IntPtr.Zero)
                        continue;

                    foreach (var functionName in module.Value)
                    {
                        try
                        {
                            var hook = DetectInlineHook(hModule, module.Key, functionName);
                            if (hook != null)
                            {
                                result.DetectedHooks.Add(hook);
                            }
                        }
                        catch (Exception ex)
                        {
                            Debug.WriteLine($"Error checking {module.Key}!{functionName}: {ex.Message}");
                        }
                    }
                }

                result.HooksDetected = result.DetectedHooks.Count > 0;
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Error in ScanForHooks: {ex.Message}");
            }

            return result;
        }

        /// <summary>
        /// Detect inline hooks (JMP instructions at function start)
        /// </summary>
        private DetectedHook? DetectInlineHook(IntPtr hModule, string moduleName, string functionName)
        {
            try
            {
                var functionAddress = GetProcAddress(hModule, functionName);
                if (functionAddress == IntPtr.Zero)
                    return null;

                // Read first bytes of the function
                byte[] buffer = new byte[16];
                if (!ReadProcessMemory(Process.GetCurrentProcess().Handle, functionAddress,
                    buffer, buffer.Length, out int bytesRead))
                {
                    return null;
                }

                // Check for common hook patterns
                // 0xE9 = JMP (relative)
                // 0xEB = JMP (short)
                // 0xFF 0x25 = JMP (absolute, x64)
                // 0xE8 = CALL

                if (buffer[0] == 0xE9) // JMP relative
                {
                    return new DetectedHook
                    {
                        FunctionName = functionName,
                        ModuleName = moduleName,
                        HookType = "Inline Hook (JMP relative)",
                        Evidence = $"First byte: 0xE9 at {functionAddress:X}"
                    };
                }

                if (buffer[0] == 0xEB) // JMP short
                {
                    return new DetectedHook
                    {
                        FunctionName = functionName,
                        ModuleName = moduleName,
                        HookType = "Inline Hook (JMP short)",
                        Evidence = $"First byte: 0xEB at {functionAddress:X}"
                    };
                }

                if (buffer[0] == 0xFF && buffer[1] == 0x25) // JMP absolute (x64)
                {
                    return new DetectedHook
                    {
                        FunctionName = functionName,
                        ModuleName = moduleName,
                        HookType = "Inline Hook (JMP absolute)",
                        Evidence = $"First bytes: 0xFF 0x25 at {functionAddress:X}"
                    };
                }

                // Check for hotpatching pattern (int3 breakpoint)
                if (buffer[0] == 0xCC || buffer[0] == 0xCD)
                {
                    return new DetectedHook
                    {
                        FunctionName = functionName,
                        ModuleName = moduleName,
                        HookType = "Breakpoint Hook",
                        Evidence = $"Breakpoint instruction at {functionAddress:X}"
                    };
                }

                // Check for suspicious patterns (NOP sled before hook)
                if (buffer[0] == 0x90 && buffer[1] == 0x90 && buffer[2] == 0x90)
                {
                    return new DetectedHook
                    {
                        FunctionName = functionName,
                        ModuleName = moduleName,
                        HookType = "Suspicious NOP Pattern",
                        Evidence = $"NOP sled detected at {functionAddress:X}"
                    };
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Error detecting inline hook for {functionName}: {ex.Message}");
            }

            return null;
        }

        /// <summary>
        /// Monitor for new hooks being installed
        /// </summary>
        public void StartContinuousMonitoring(Action<DetectedHook> onHookDetected, int intervalMs = 5000)
        {
            var knownHooks = new HashSet<string>();
            var timer = new System.Timers.Timer(intervalMs);

            timer.Elapsed += (sender, e) =>
            {
                try
                {
                    var result = ScanForHooks();
                    foreach (var hook in result.DetectedHooks)
                    {
                        var key = $"{hook.ModuleName}!{hook.FunctionName}";
                        if (!knownHooks.Contains(key))
                        {
                            knownHooks.Add(key);
                            onHookDetected(hook);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Debug.WriteLine($"Error in hook monitoring: {ex.Message}");
                }
            };

            timer.Start();
        }

        /// <summary>
        /// Check if Import Address Table (IAT) has been hooked
        /// </summary>
        public List<string> DetectIATHooks()
        {
            var suspiciousEntries = new List<string>();

            try
            {
                // This is a simplified IAT check
                // In production, you would parse PE headers and verify IAT entries
                var currentProcess = Process.GetCurrentProcess();
                foreach (ProcessModule module in currentProcess.Modules)
                {
                    try
                    {
                        // Check if module is in an unexpected location
                        if (!module.FileName.StartsWith(Environment.GetFolderPath(Environment.SpecialFolder.Windows),
                            StringComparison.OrdinalIgnoreCase))
                        {
                            if (!module.FileName.StartsWith(AppDomain.CurrentDomain.BaseDirectory,
                                StringComparison.OrdinalIgnoreCase))
                            {
                                suspiciousEntries.Add($"Module from unexpected location: {module.FileName}");
                            }
                        }
                    }
                    catch
                    {
                        // Skip modules we can't check
                    }
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Error in IAT hook detection: {ex.Message}");
            }

            return suspiciousEntries;
        }
    }
}
