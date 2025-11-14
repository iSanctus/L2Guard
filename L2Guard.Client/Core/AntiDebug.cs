using System;
using System.Diagnostics;
using System.Runtime.InteropServices;

namespace L2Guard.Client.Core
{
    /// <summary>
    /// Detects debuggers and debugging attempts
    /// </summary>
    public class AntiDebug
    {
        [DllImport("kernel32.dll", SetLastError = true, ExactSpelling = true)]
        private static extern bool CheckRemoteDebuggerPresent(IntPtr hProcess, ref bool isDebuggerPresent);

        [DllImport("kernel32.dll", SetLastError = true, ExactSpelling = true)]
        private static extern bool IsDebuggerPresent();

        [DllImport("ntdll.dll", SetLastError = true)]
        private static extern int NtQueryInformationProcess(IntPtr processHandle, int processInformationClass,
            ref ProcessDebugPort processInformation, uint processInformationLength, out uint returnLength);

        [StructLayout(LayoutKind.Sequential)]
        private struct ProcessDebugPort
        {
            public IntPtr DebugPort;
        }

        public class DebugDetectionResult
        {
            public bool DebuggerDetected { get; set; }
            public List<string> DetectionMethods { get; set; } = new();
            public DateTime CheckTime { get; set; }
        }

        /// <summary>
        /// Perform comprehensive debugger detection
        /// </summary>
        public DebugDetectionResult DetectDebugger()
        {
            var result = new DebugDetectionResult
            {
                CheckTime = DateTime.UtcNow
            };

            // Method 1: Check IsDebuggerPresent API
            if (IsDebuggerPresent())
            {
                result.DebuggerDetected = true;
                result.DetectionMethods.Add("IsDebuggerPresent() returned true");
            }

            // Method 2: Check via CheckRemoteDebuggerPresent
            bool isDebuggerPresent = false;
            CheckRemoteDebuggerPresent(Process.GetCurrentProcess().Handle, ref isDebuggerPresent);
            if (isDebuggerPresent)
            {
                result.DebuggerDetected = true;
                result.DetectionMethods.Add("CheckRemoteDebuggerPresent() detected debugger");
            }

            // Method 3: Check via NtQueryInformationProcess
            try
            {
                var debugPort = new ProcessDebugPort();
                uint returnLength;
                int status = NtQueryInformationProcess(
                    Process.GetCurrentProcess().Handle,
                    7, // ProcessDebugPort
                    ref debugPort,
                    (uint)Marshal.SizeOf(debugPort),
                    out returnLength
                );

                if (status == 0 && debugPort.DebugPort != IntPtr.Zero)
                {
                    result.DebuggerDetected = true;
                    result.DetectionMethods.Add("NtQueryInformationProcess detected debug port");
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Error in NtQueryInformationProcess check: {ex.Message}");
            }

            // Method 4: Check for debugger processes
            var debuggerProcesses = new[]
            {
                "ollydbg", "x64dbg", "x32dbg", "windbg", "ida", "ida64",
                "idaq", "idag", "idaw", "idag64", "idaw64", "idaq64",
                "scylla", "protection_id", "reshacker", "importrec",
                "immunitydebugger", "cheatengine"
            };

            foreach (var debuggerName in debuggerProcesses)
            {
                var processes = Process.GetProcessesByName(debuggerName);
                if (processes.Length > 0)
                {
                    result.DebuggerDetected = true;
                    result.DetectionMethods.Add($"Debugger process detected: {debuggerName}");
                }
            }

            // Method 5: Timing check (debuggers slow down execution)
            if (DetectTimingAnomaly())
            {
                result.DebuggerDetected = true;
                result.DetectionMethods.Add("Timing anomaly detected (possible stepping)");
            }

            return result;
        }

        /// <summary>
        /// Detect timing anomalies that suggest debugging/stepping
        /// </summary>
        private bool DetectTimingAnomaly()
        {
            try
            {
                var sw = Stopwatch.StartNew();

                // Simple operation that should be very fast
                int dummy = 0;
                for (int i = 0; i < 100; i++)
                {
                    dummy += i;
                }

                sw.Stop();

                // If this simple operation takes more than 10ms, something is wrong
                // (normal execution: < 1ms, with debugger stepping: much longer)
                return sw.ElapsedMilliseconds > 10;
            }
            catch
            {
                return false;
            }
        }

        /// <summary>
        /// Check if process has elevated privileges (sometimes used by debuggers)
        /// </summary>
        public bool HasElevatedPrivileges()
        {
            try
            {
                var identity = System.Security.Principal.WindowsIdentity.GetCurrent();
                var principal = new System.Security.Principal.WindowsPrincipal(identity);
                return principal.IsInRole(System.Security.Principal.WindowsBuiltInRole.Administrator);
            }
            catch
            {
                return false;
            }
        }

        /// <summary>
        /// Continuous debugger monitoring
        /// </summary>
        public void StartContinuousMonitoring(Action<DebugDetectionResult> onDebuggerDetected, int intervalMs = 2000)
        {
            var timer = new System.Timers.Timer(intervalMs);

            timer.Elapsed += (sender, e) =>
            {
                try
                {
                    var result = DetectDebugger();
                    if (result.DebuggerDetected)
                    {
                        onDebuggerDetected(result);
                    }
                }
                catch (Exception ex)
                {
                    Debug.WriteLine($"Error in continuous debugger monitoring: {ex.Message}");
                }
            };

            timer.Start();
        }

        /// <summary>
        /// Self-integrity check (detect if guard code is being modified)
        /// </summary>
        public bool VerifySelfIntegrity()
        {
            try
            {
                // Check if our own assembly has been modified
                var assembly = System.Reflection.Assembly.GetExecutingAssembly();
                var assemblyPath = assembly.Location;

                if (string.IsNullOrEmpty(assemblyPath))
                    return false;

                // Check file exists
                if (!System.IO.File.Exists(assemblyPath))
                    return false;

                // In production, you would verify a cryptographic signature here
                // For now, just verify the file can be read
                var fileInfo = new System.IO.FileInfo(assemblyPath);
                return fileInfo.Exists && fileInfo.Length > 0;
            }
            catch
            {
                return false;
            }
        }
    }
}
