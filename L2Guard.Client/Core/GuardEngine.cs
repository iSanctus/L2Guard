using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Timers;

namespace L2Guard.Client.Core
{
    /// <summary>
    /// Main L2Guard engine that orchestrates all protection mechanisms
    /// </summary>
    public class GuardEngine
    {
        private readonly ProcessScanner _processScanner;
        private readonly ModuleScanner _moduleScanner;
        private readonly AntiDebug _antiDebug;
        private readonly HookDetector _hookDetector;
        private readonly MemoryProtector _memoryProtector;

        private Timer? _scanTimer;
        private bool _isRunning;
        private readonly object _lockObject = new();

        public event EventHandler<BotDetectedEventArgs>? BotDetected;
        public event EventHandler<DebuggerDetectedEventArgs>? DebuggerDetected;
        public event EventHandler<HookDetectedEventArgs>? HookDetected;
        public event EventHandler<MemoryViolationEventArgs>? MemoryViolation;
        public event EventHandler<StatusChangedEventArgs>? StatusChanged;

        public class BotDetectedEventArgs : EventArgs
        {
            public string BotName { get; set; } = string.Empty;
            public string ProcessName { get; set; } = string.Empty;
            public string DetectionMethod { get; set; } = string.Empty;
            public int ThreatLevel { get; set; }
            public DateTime Timestamp { get; set; }
        }

        public class DebuggerDetectedEventArgs : EventArgs
        {
            public List<string> DetectionMethods { get; set; } = new();
            public DateTime Timestamp { get; set; }
        }

        public class HookDetectedEventArgs : EventArgs
        {
            public string FunctionName { get; set; } = string.Empty;
            public string ModuleName { get; set; } = string.Empty;
            public string HookType { get; set; } = string.Empty;
            public DateTime Timestamp { get; set; }
        }

        public class MemoryViolationEventArgs : EventArgs
        {
            public List<string> Violations { get; set; } = new();
            public DateTime Timestamp { get; set; }
        }

        public class StatusChangedEventArgs : EventArgs
        {
            public string Status { get; set; } = string.Empty;
            public DateTime Timestamp { get; set; }
        }

        public GuardEngine()
        {
            _processScanner = new ProcessScanner();
            _moduleScanner = new ModuleScanner();
            _antiDebug = new AntiDebug();
            _hookDetector = new HookDetector();
            _memoryProtector = new MemoryProtector();
        }

        /// <summary>
        /// Start the guard engine
        /// </summary>
        public bool Start(int scanIntervalMs = 5000)
        {
            lock (_lockObject)
            {
                if (_isRunning)
                {
                    return false;
                }

                try
                {
                    RaiseStatusChanged("L2Guard initializing...");

                    // CRITICAL: Verify guard integrity before starting
                    RaiseStatusChanged("Verifying guard integrity...");
                    if (!VerifyGuardIntegrity())
                    {
                        RaiseStatusChanged("TAMPER DETECTED - Guard files have been modified!");
                        return false;
                    }

                    // Enforce hardcoded scan interval (prevent tampering via config)
                    scanIntervalMs = SecurityConfig.SCAN_INTERVAL_MS;

                    // Perform initial scan before allowing game to start
                    var initialScan = PerformInitialScan();
                    if (!initialScan.Success)
                    {
                        RaiseStatusChanged($"Initial scan failed: {initialScan.Message}");
                        return false;
                    }

                    // Start continuous monitoring using HARDCODED interval
                    _scanTimer = new Timer(SecurityConfig.SCAN_INTERVAL_MS);
                    _scanTimer.Elapsed += OnScanTimerElapsed;
                    _scanTimer.Start();

                    _isRunning = true;

                    RaiseStatusChanged("L2Guard active - Protection enabled");
                    return true;
                }
                catch (Exception ex)
                {
                    RaiseStatusChanged($"Failed to start L2Guard: {ex.Message}");
                    return false;
                }
            }
        }

        /// <summary>
        /// Verify guard files haven't been tampered with
        /// </summary>
        private bool VerifyGuardIntegrity()
        {
            // Check assembly integrity
            if (!SecurityConfig.VerifyIntegrity())
            {
                Debug.WriteLine("Assembly integrity check failed");
                return false;
            }

            // Check file integrity
            if (!SecurityConfig.VerifyFileIntegrity())
            {
                Debug.WriteLine("File integrity check failed");
                return false;
            }

            // Check code integrity
            if (!SecurityConfig.VerifyCodeIntegrity())
            {
                Debug.WriteLine("Code integrity check failed");
                return false;
            }

            return true;
        }

        /// <summary>
        /// Stop the guard engine
        /// </summary>
        public void Stop()
        {
            lock (_lockObject)
            {
                if (!_isRunning)
                    return;

                _scanTimer?.Stop();
                _scanTimer?.Dispose();
                _scanTimer = null;

                _isRunning = false;

                RaiseStatusChanged("L2Guard stopped");
            }
        }

        /// <summary>
        /// Perform initial scan before game starts
        /// </summary>
        private ScanResult PerformInitialScan()
        {
            var result = new ScanResult { Success = true };

            try
            {
                // 1. Scan for bot processes
                var processScanResult = _processScanner.ScanProcesses();
                if (processScanResult.BotDetected)
                {
                    foreach (var bot in processScanResult.DetectedBots)
                    {
                        RaiseBotDetected(bot.BotName, bot.ProcessName, bot.DetectionMethod, bot.ThreatLevel);
                    }
                    result.Success = false;
                    result.Message = $"Bot detected: {processScanResult.DetectedBots[0].BotName}";
                    return result;
                }

                // 2. Check for debuggers
                var debugResult = _antiDebug.DetectDebugger();
                if (debugResult.DebuggerDetected)
                {
                    RaiseDebuggerDetected(debugResult.DetectionMethods);
                    result.Success = false;
                    result.Message = "Debugger detected";
                    return result;
                }

                // 3. Scan for API hooks
                var hookResult = _hookDetector.ScanForHooks();
                if (hookResult.HooksDetected)
                {
                    foreach (var hook in hookResult.DetectedHooks)
                    {
                        RaiseHookDetected(hook.FunctionName, hook.ModuleName, hook.HookType);
                    }
                    // Hooks might be legitimate (e.g., Discord overlay), so we don't block
                    // but we log them for monitoring
                }

                result.Message = "Initial scan complete - No threats detected";
                return result;
            }
            catch (Exception ex)
            {
                result.Success = false;
                result.Message = $"Scan error: {ex.Message}";
                return result;
            }
        }

        /// <summary>
        /// Periodic scan timer event
        /// </summary>
        private void OnScanTimerElapsed(object? sender, ElapsedEventArgs e)
        {
            try
            {
                PerformContinuousScan();
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Error in continuous scan: {ex.Message}");
            }
        }

        /// <summary>
        /// Perform continuous monitoring scans
        /// </summary>
        private void PerformContinuousScan()
        {
            // 1. Check for new bot processes
            var processScanResult = _processScanner.ScanProcesses();
            if (processScanResult.BotDetected)
            {
                foreach (var bot in processScanResult.DetectedBots)
                {
                    RaiseBotDetected(bot.BotName, bot.ProcessName, bot.DetectionMethod, bot.ThreatLevel);
                }
            }

            // 2. Check for debuggers
            var debugResult = _antiDebug.DetectDebugger();
            if (debugResult.DebuggerDetected)
            {
                RaiseDebuggerDetected(debugResult.DetectionMethods);
            }

            // 3. Check memory integrity
            var memoryResult = _memoryProtector.CheckIntegrity();
            if (memoryResult.IntegrityCompromised)
            {
                RaiseMemoryViolation(memoryResult.Violations);
            }

            // 4. Scan for new hooks
            var hookResult = _hookDetector.ScanForHooks();
            if (hookResult.HooksDetected)
            {
                foreach (var hook in hookResult.DetectedHooks)
                {
                    RaiseHookDetected(hook.FunctionName, hook.ModuleName, hook.HookType);
                }
            }
        }

        /// <summary>
        /// Scan modules in current process
        /// </summary>
        public ModuleScanner.ModuleScanResult ScanCurrentProcessModules()
        {
            return _moduleScanner.ScanCurrentProcess();
        }

        /// <summary>
        /// Register a memory region to protect
        /// </summary>
        public void RegisterProtectedMemory(IntPtr address, int size, string description)
        {
            _memoryProtector.RegisterProtectedRegion(address, size, description);
        }

        /// <summary>
        /// Get current guard status
        /// </summary>
        public string GetStatus()
        {
            return _isRunning ? "Running" : "Stopped";
        }

        /// <summary>
        /// Check if guard is running
        /// </summary>
        public bool IsRunning => _isRunning;

        // Event raising helpers
        private void RaiseBotDetected(string botName, string processName, string method, int threatLevel)
        {
            BotDetected?.Invoke(this, new BotDetectedEventArgs
            {
                BotName = botName,
                ProcessName = processName,
                DetectionMethod = method,
                ThreatLevel = threatLevel,
                Timestamp = DateTime.UtcNow
            });
        }

        private void RaiseDebuggerDetected(List<string> methods)
        {
            DebuggerDetected?.Invoke(this, new DebuggerDetectedEventArgs
            {
                DetectionMethods = methods,
                Timestamp = DateTime.UtcNow
            });
        }

        private void RaiseHookDetected(string functionName, string moduleName, string hookType)
        {
            HookDetected?.Invoke(this, new HookDetectedEventArgs
            {
                FunctionName = functionName,
                ModuleName = moduleName,
                HookType = hookType,
                Timestamp = DateTime.UtcNow
            });
        }

        private void RaiseMemoryViolation(List<string> violations)
        {
            MemoryViolation?.Invoke(this, new MemoryViolationEventArgs
            {
                Violations = violations,
                Timestamp = DateTime.UtcNow
            });
        }

        private void RaiseStatusChanged(string status)
        {
            StatusChanged?.Invoke(this, new StatusChangedEventArgs
            {
                Status = status,
                Timestamp = DateTime.UtcNow
            });
        }

        private class ScanResult
        {
            public bool Success { get; set; }
            public string Message { get; set; } = string.Empty;
        }
    }
}
