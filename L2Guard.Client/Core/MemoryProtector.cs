using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Runtime.InteropServices;
using System.Security.Cryptography;
using System.Text;

namespace L2Guard.Client.Core
{
    /// <summary>
    /// Monitors memory integrity and detects modifications
    /// </summary>
    public class MemoryProtector
    {
        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern bool ReadProcessMemory(IntPtr hProcess, IntPtr lpBaseAddress, byte[] lpBuffer,
            int dwSize, out int lpNumberOfBytesRead);

        [DllImport("kernel32.dll", SetLastError = true)]
        private static extern bool VirtualProtect(IntPtr lpAddress, UIntPtr dwSize, uint flNewProtect, out uint lpflOldProtect);

        private const uint PAGE_READONLY = 0x02;
        private const uint PAGE_EXECUTE_READ = 0x20;

        public class MemoryIntegrityResult
        {
            public bool IntegrityCompromised { get; set; }
            public List<string> Violations { get; set; } = new();
            public DateTime CheckTime { get; set; }
        }

        private class MemoryRegion
        {
            public IntPtr Address { get; set; }
            public int Size { get; set; }
            public string Hash { get; set; } = string.Empty;
            public string Description { get; set; } = string.Empty;
        }

        private readonly List<MemoryRegion> _protectedRegions = new();

        /// <summary>
        /// Register a memory region to protect
        /// </summary>
        public void RegisterProtectedRegion(IntPtr address, int size, string description)
        {
            try
            {
                var hash = CalculateMemoryHash(address, size);
                if (!string.IsNullOrEmpty(hash))
                {
                    _protectedRegions.Add(new MemoryRegion
                    {
                        Address = address,
                        Size = size,
                        Hash = hash,
                        Description = description
                    });

                    Debug.WriteLine($"Protected region registered: {description} at {address:X}");
                }
            }
            catch (Exception ex)
            {
                Debug.WriteLine($"Error registering protected region: {ex.Message}");
            }
        }

        /// <summary>
        /// Check integrity of all protected memory regions
        /// </summary>
        public MemoryIntegrityResult CheckIntegrity()
        {
            var result = new MemoryIntegrityResult
            {
                CheckTime = DateTime.UtcNow
            };

            foreach (var region in _protectedRegions)
            {
                try
                {
                    var currentHash = CalculateMemoryHash(region.Address, region.Size);
                    if (currentHash != region.Hash)
                    {
                        result.IntegrityCompromised = true;
                        result.Violations.Add($"Memory modification detected in {region.Description} at {region.Address:X}");
                    }
                }
                catch (Exception ex)
                {
                    result.Violations.Add($"Error checking {region.Description}: {ex.Message}");
                }
            }

            return result;
        }

        /// <summary>
        /// Calculate hash of memory region
        /// </summary>
        private string CalculateMemoryHash(IntPtr address, int size)
        {
            try
            {
                byte[] buffer = new byte[size];
                if (!ReadProcessMemory(Process.GetCurrentProcess().Handle, address, buffer, size, out int bytesRead))
                {
                    return string.Empty;
                }

                using (var sha256 = SHA256.Create())
                {
                    var hash = sha256.ComputeHash(buffer, 0, bytesRead);
                    return BitConverter.ToString(hash).Replace("-", "");
                }
            }
            catch
            {
                return string.Empty;
            }
        }

        /// <summary>
        /// Protect critical code sections from modification
        /// </summary>
        public bool ProtectCodeSection(IntPtr address, int size)
        {
            try
            {
                uint oldProtect;
                return VirtualProtect(address, (UIntPtr)size, PAGE_EXECUTE_READ, out oldProtect);
            }
            catch
            {
                return false;
            }
        }

        /// <summary>
        /// Detect memory scanning tools
        /// </summary>
        public List<string> DetectMemoryScanners()
        {
            var suspiciousTools = new List<string>();

            // Check for processes that commonly scan memory
            var scannerProcesses = new[]
            {
                "cheatengine", "artmoney", "gameguardian",
                "scanmem", "memoryviewer", "processhacker"
            };

            foreach (var processName in scannerProcesses)
            {
                var processes = Process.GetProcessesByName(processName);
                if (processes.Length > 0)
                {
                    suspiciousTools.Add($"Memory scanner detected: {processName}");
                }
            }

            return suspiciousTools;
        }

        /// <summary>
        /// Monitor for suspicious memory access patterns
        /// </summary>
        public void StartContinuousMonitoring(Action<MemoryIntegrityResult> onIntegrityViolation, int intervalMs = 5000)
        {
            var timer = new System.Timers.Timer(intervalMs);

            timer.Elapsed += (sender, e) =>
            {
                try
                {
                    var result = CheckIntegrity();
                    if (result.IntegrityCompromised)
                    {
                        onIntegrityViolation(result);
                    }

                    // Also check for memory scanners
                    var scanners = DetectMemoryScanners();
                    if (scanners.Count > 0)
                    {
                        result.Violations.AddRange(scanners);
                        onIntegrityViolation(result);
                    }
                }
                catch (Exception ex)
                {
                    Debug.WriteLine($"Error in memory integrity monitoring: {ex.Message}");
                }
            };

            timer.Start();
        }

        /// <summary>
        /// Clear all protected regions (for cleanup)
        /// </summary>
        public void ClearProtectedRegions()
        {
            _protectedRegions.Clear();
        }

        /// <summary>
        /// Get count of protected regions
        /// </summary>
        public int GetProtectedRegionCount()
        {
            return _protectedRegions.Count;
        }
    }
}
