using System;
using System.Collections.Generic;
using System.Linq;

namespace L2Guard.Client.Signatures
{
    /// <summary>
    /// Database of known bot programs and their signatures
    /// </summary>
    public static class KnownBots
    {
        public class BotSignature
        {
            public string Name { get; set; } = string.Empty;
            public List<string> ProcessNames { get; set; } = new();
            public List<string> WindowTitles { get; set; } = new();
            public List<string> SuspiciousDLLs { get; set; } = new();
            public string Description { get; set; } = string.Empty;
            public int ThreatLevel { get; set; } // 1-10
        }

        public static readonly List<BotSignature> Signatures = new()
        {
            new BotSignature
            {
                Name = "Adrenaline",
                ProcessNames = new List<string>
                {
                    "adrenaline.exe",
                    "adrenaline bot.exe",
                    "l2adrenaline.exe",
                    "adren.exe"
                },
                WindowTitles = new List<string>
                {
                    "adrenaline",
                    "l2 adrenaline",
                    "adrenaline bot"
                },
                SuspiciousDLLs = new List<string>
                {
                    "adrenaline.dll",
                    "adren.dll"
                },
                Description = "Popular L2 bot with advanced features",
                ThreatLevel = 10
            },
            new BotSignature
            {
                Name = "L2Walker",
                ProcessNames = new List<string>
                {
                    "l2walker.exe",
                    "walker.exe",
                    "l2w.exe"
                },
                WindowTitles = new List<string>
                {
                    "l2walker",
                    "lineage 2 walker",
                    "walker"
                },
                SuspiciousDLLs = new List<string>
                {
                    "l2walker.dll",
                    "walker.dll"
                },
                Description = "Classic L2 bot program",
                ThreatLevel = 9
            },
            new BotSignature
            {
                Name = "L2Tower",
                ProcessNames = new List<string>
                {
                    "l2tower.exe",
                    "tower.exe",
                    "l2t.exe"
                },
                WindowTitles = new List<string>
                {
                    "l2tower",
                    "l2 tower",
                    "tower bot"
                },
                SuspiciousDLLs = new List<string>
                {
                    "l2tower.dll",
                    "tower.dll"
                },
                Description = "Advanced L2 bot with scripting",
                ThreatLevel = 10
            },
            new BotSignature
            {
                Name = "L2.Net",
                ProcessNames = new List<string>
                {
                    "l2.net.exe",
                    "l2net.exe",
                    "l2dotnet.exe"
                },
                WindowTitles = new List<string>
                {
                    "l2.net",
                    "l2 .net",
                    "l2net"
                },
                SuspiciousDLLs = new List<string>
                {
                    "l2net.dll"
                },
                Description = "L2 bot framework",
                ThreatLevel = 9
            },
            new BotSignature
            {
                Name = "L2PHX",
                ProcessNames = new List<string>
                {
                    "l2phx.exe",
                    "phx.exe",
                    "phoenix.exe"
                },
                WindowTitles = new List<string>
                {
                    "l2phx",
                    "l2 phoenix",
                    "phoenix"
                },
                SuspiciousDLLs = new List<string>
                {
                    "l2phx.dll",
                    "phx.dll"
                },
                Description = "L2 packet editor and bot",
                ThreatLevel = 8
            },
            new BotSignature
            {
                Name = "Cheat Engine",
                ProcessNames = new List<string>
                {
                    "cheatengine-x86_64.exe",
                    "cheatengine-i386.exe",
                    "cheatengine.exe",
                    "cheat engine.exe"
                },
                WindowTitles = new List<string>
                {
                    "cheat engine",
                    "ce"
                },
                SuspiciousDLLs = new List<string>
                {
                    "cheatengine.dll",
                    "ce.dll"
                },
                Description = "Memory editing tool",
                ThreatLevel = 7
            },
            new BotSignature
            {
                Name = "OllyDbg",
                ProcessNames = new List<string>
                {
                    "ollydbg.exe",
                    "olly.exe",
                    "ollyice.exe"
                },
                WindowTitles = new List<string>
                {
                    "ollydbg",
                    "olly"
                },
                Description = "Debugger tool",
                ThreatLevel = 6
            },
            new BotSignature
            {
                Name = "x64dbg",
                ProcessNames = new List<string>
                {
                    "x64dbg.exe",
                    "x32dbg.exe",
                    "x96dbg.exe"
                },
                WindowTitles = new List<string>
                {
                    "x64dbg",
                    "x32dbg"
                },
                Description = "Advanced debugger",
                ThreatLevel = 6
            },
            new BotSignature
            {
                Name = "Process Hacker",
                ProcessNames = new List<string>
                {
                    "processhacker.exe",
                    "processhacker-2.exe"
                },
                WindowTitles = new List<string>
                {
                    "process hacker"
                },
                Description = "Process monitoring tool",
                ThreatLevel = 5
            }
        };

        /// <summary>
        /// Suspicious process name patterns that might indicate a bot
        /// </summary>
        public static readonly List<string> SuspiciousPatterns = new()
        {
            "l2bot",
            "lineagebot",
            "l2hack",
            "l2cheat",
            "botl2",
            "autopot",
            "autofish",
            "autofarm",
            "autohunt",
            "l2auto",
            "l2script",
            "injector",
            "hook"
        };

        /// <summary>
        /// Known DLL signatures for injected modules
        /// </summary>
        public static readonly List<string> SuspiciousDLLPatterns = new()
        {
            "hook",
            "inject",
            "bot",
            "cheat",
            "hack",
            "l2mod",
            "l2dll"
        };

        /// <summary>
        /// Check if a process name matches a known bot signature
        /// </summary>
        public static BotSignature? FindByProcessName(string processName)
        {
            var lowerName = processName.ToLowerInvariant();
            return Signatures.FirstOrDefault(sig =>
                sig.ProcessNames.Any(name => lowerName.Contains(name.ToLowerInvariant())));
        }

        /// <summary>
        /// Check if a window title matches a known bot signature
        /// </summary>
        public static BotSignature? FindByWindowTitle(string windowTitle)
        {
            var lowerTitle = windowTitle.ToLowerInvariant();
            return Signatures.FirstOrDefault(sig =>
                sig.WindowTitles.Any(title => lowerTitle.Contains(title.ToLowerInvariant())));
        }

        /// <summary>
        /// Check if a DLL name matches a known bot signature
        /// </summary>
        public static BotSignature? FindByDLL(string dllName)
        {
            var lowerDLL = dllName.ToLowerInvariant();
            return Signatures.FirstOrDefault(sig =>
                sig.SuspiciousDLLs.Any(dll => lowerDLL.Contains(dll.ToLowerInvariant())));
        }

        /// <summary>
        /// Check if a process name matches suspicious patterns
        /// </summary>
        public static bool MatchesSuspiciousPattern(string processName)
        {
            var lowerName = processName.ToLowerInvariant();
            return SuspiciousPatterns.Any(pattern => lowerName.Contains(pattern));
        }

        /// <summary>
        /// Check if a DLL name matches suspicious patterns
        /// </summary>
        public static bool DLLMatchesSuspiciousPattern(string dllName)
        {
            var lowerDLL = dllName.ToLowerInvariant();
            return SuspiciousDLLPatterns.Any(pattern => lowerDLL.Contains(pattern));
        }
    }
}
