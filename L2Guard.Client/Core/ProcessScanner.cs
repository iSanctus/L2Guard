using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using L2Guard.Client.Signatures;

namespace L2Guard.Client.Core
{
    /// <summary>
    /// Scans for running bot processes
    /// </summary>
    public class ProcessScanner
    {
        [DllImport("user32.dll")]
        private static extern int GetWindowText(IntPtr hWnd, StringBuilder text, int count);

        [DllImport("user32.dll")]
        private static extern bool EnumWindows(EnumWindowsProc enumProc, IntPtr lParam);

        private delegate bool EnumWindowsProc(IntPtr hWnd, IntPtr lParam);

        public class DetectionResult
        {
            public bool BotDetected { get; set; }
            public List<DetectedBot> DetectedBots { get; set; } = new();
            public DateTime ScanTime { get; set; }
        }

        public class DetectedBot
        {
            public string BotName { get; set; } = string.Empty;
            public string ProcessName { get; set; } = string.Empty;
            public int ProcessId { get; set; }
            public string DetectionMethod { get; set; } = string.Empty;
            public int ThreatLevel { get; set; }
            public string Description { get; set; } = string.Empty;
        }

        /// <summary>
        /// Scan all running processes for known bots
        /// </summary>
        public DetectionResult ScanProcesses()
        {
            var result = new DetectionResult
            {
                ScanTime = DateTime.UtcNow
            };

            try
            {
                var processes = Process.GetProcesses();

                foreach (var process in processes)
                {
                    try
                    {
                        // Check process name against known bot signatures
                        var botSignature = KnownBots.FindByProcessName(process.ProcessName);
                        if (botSignature != null)
                        {
                            result.DetectedBots.Add(new DetectedBot
                            {
                                BotName = botSignature.Name,
                                ProcessName = process.ProcessName,
                                ProcessId = process.Id,
                                DetectionMethod = "Process Name Match",
                                ThreatLevel = botSignature.ThreatLevel,
                                Description = botSignature.Description
                            });
                            continue;
                        }

                        // Check against suspicious patterns
                        if (KnownBots.MatchesSuspiciousPattern(process.ProcessName))
                        {
                            result.DetectedBots.Add(new DetectedBot
                            {
                                BotName = "Unknown Bot",
                                ProcessName = process.ProcessName,
                                ProcessId = process.Id,
                                DetectionMethod = "Suspicious Process Pattern",
                                ThreatLevel = 5,
                                Description = "Process name matches suspicious patterns"
                            });
                        }
                    }
                    catch (Exception ex)
                    {
                        // Some system processes can't be accessed
                        Debug.WriteLine($"Error scanning process {process.ProcessName}: {ex.Message}");
                    }
                    finally
                    {
                        process.Dispose();
                    }
                }

                // Scan window titles
                ScanWindowTitles(result);

                result.BotDetected = result.DetectedBots.Count > 0;
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Error in ScanProcesses: {ex.Message}");
            }

            return result;
        }

        /// <summary>
        /// Scan window titles for known bot programs
        /// </summary>
        private void ScanWindowTitles(DetectionResult result)
        {
            var windowTitles = new List<string>();

            EnumWindows((hWnd, lParam) =>
            {
                var length = 256;
                var stringBuilder = new StringBuilder(length);
                if (GetWindowText(hWnd, stringBuilder, length) > 0)
                {
                    var title = stringBuilder.ToString();
                    if (!string.IsNullOrWhiteSpace(title))
                    {
                        windowTitles.Add(title);

                        // Check against known bot signatures
                        var botSignature = KnownBots.FindByWindowTitle(title);
                        if (botSignature != null)
                        {
                            // Check if we haven't already detected this bot by process name
                            if (!result.DetectedBots.Any(b => b.BotName == botSignature.Name))
                            {
                                result.DetectedBots.Add(new DetectedBot
                                {
                                    BotName = botSignature.Name,
                                    ProcessName = "Unknown (detected by window title)",
                                    ProcessId = 0,
                                    DetectionMethod = "Window Title Match",
                                    ThreatLevel = botSignature.ThreatLevel,
                                    Description = botSignature.Description
                                });
                            }
                        }
                    }
                }
                return true;
            }, IntPtr.Zero);
        }

        /// <summary>
        /// Check if a specific process is running
        /// </summary>
        public bool IsProcessRunning(string processName)
        {
            try
            {
                var processes = Process.GetProcessesByName(processName);
                return processes.Length > 0;
            }
            catch
            {
                return false;
            }
        }

        /// <summary>
        /// Get detailed information about a process
        /// </summary>
        public string GetProcessInfo(int processId)
        {
            try
            {
                var process = Process.GetProcessById(processId);
                return $"Name: {process.ProcessName}, Path: {process.MainModule?.FileName ?? "Unknown"}, Threads: {process.Threads.Count}";
            }
            catch (Exception ex)
            {
                return $"Error getting process info: {ex.Message}";
            }
        }

        /// <summary>
        /// Monitor for new processes (continuous scanning)
        /// </summary>
        public void StartContinuousMonitoring(Action<DetectedBot> onBotDetected, int intervalMs = 5000)
        {
            var previousProcesses = new HashSet<string>();
            var timer = new System.Timers.Timer(intervalMs);

            timer.Elapsed += (sender, e) =>
            {
                try
                {
                    var result = ScanProcesses();
                    foreach (var bot in result.DetectedBots)
                    {
                        var key = $"{bot.ProcessName}_{bot.ProcessId}";
                        if (!previousProcesses.Contains(key))
                        {
                            previousProcesses.Add(key);
                            onBotDetected(bot);
                        }
                    }
                }
                catch (Exception ex)
                {
                    Debug.WriteLine($"Error in continuous monitoring: {ex.Message}");
                }
            };

            timer.Start();
        }
    }
}
