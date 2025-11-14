using System;
using System.Diagnostics;
using System.IO;
using System.Windows.Forms;
using L2Guard.Client.Core;

namespace L2Guard.Launcher
{
    static class Program
    {
        [STAThread]
        static void Main(string[] args)
        {
            Application.SetHighDpiMode(HighDpiMode.SystemAware);
            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);

            // Show launcher form
            Application.Run(new LauncherForm());
        }
    }

    public class LauncherForm : Form
    {
        private readonly GuardEngine _guardEngine;
        private Button _launchButton;
        private TextBox _logTextBox;
        private Label _statusLabel;
        private ProgressBar _progressBar;
        private bool _guardReady = false;

        public LauncherForm()
        {
            InitializeComponents();

            _guardEngine = new GuardEngine();
            _guardEngine.BotDetected += OnBotDetected;
            _guardEngine.DebuggerDetected += OnDebuggerDetected;
            _guardEngine.StatusChanged += OnStatusChanged;

            PerformInitialCheck();
        }

        private void InitializeComponents()
        {
            // Setup form
            Text = "L2Guard Launcher";
            Size = new System.Drawing.Size(600, 500);
            StartPosition = FormStartPosition.CenterScreen;
            FormBorderStyle = FormBorderStyle.FixedDialog;
            MaximizeBox = false;

            // Status label
            _statusLabel = new Label
            {
                Text = "Initializing L2Guard...",
                Location = new System.Drawing.Point(20, 20),
                Size = new System.Drawing.Size(550, 30),
                Font = new System.Drawing.Font("Segoe UI", 12, System.Drawing.FontStyle.Bold)
            };
            Controls.Add(_statusLabel);

            // Progress bar
            _progressBar = new ProgressBar
            {
                Location = new System.Drawing.Point(20, 60),
                Size = new System.Drawing.Size(550, 30),
                Style = ProgressBarStyle.Marquee
            };
            Controls.Add(_progressBar);

            // Log textbox
            _logTextBox = new TextBox
            {
                Location = new System.Drawing.Point(20, 100),
                Size = new System.Drawing.Size(550, 300),
                Multiline = true,
                ScrollBars = ScrollBars.Vertical,
                ReadOnly = true,
                Font = new System.Drawing.Font("Consolas", 9)
            };
            Controls.Add(_logTextBox);

            // Launch button
            _launchButton = new Button
            {
                Text = "Launch Lineage 2",
                Location = new System.Drawing.Point(200, 415),
                Size = new System.Drawing.Size(200, 40),
                Font = new System.Drawing.Font("Segoe UI", 10, System.Drawing.FontStyle.Bold),
                Enabled = false
            };
            _launchButton.Click += OnLaunchButtonClick;
            Controls.Add(_launchButton);
        }

        private void PerformInitialCheck()
        {
            AddLog("L2Guard Anti-Bot Protection System");
            AddLog("====================================");
            AddLog("");

            AddLog("[1/3] Scanning for bot processes...");

            // Start guard engine
            bool startSuccess = _guardEngine.Start(5000);

            if (startSuccess)
            {
                AddLog("[OK] No bot processes detected");
                AddLog("[2/3] Checking for debuggers...");
                AddLog("[OK] No debuggers detected");
                AddLog("[3/3] Verifying game integrity...");
                AddLog("[OK] Game files verified");
                AddLog("");
                AddLog("✓ All checks passed! You may launch the game.");

                _statusLabel.Text = "✓ Protection Active - Ready to Launch";
                _statusLabel.ForeColor = System.Drawing.Color.Green;
                _progressBar.Style = ProgressBarStyle.Continuous;
                _progressBar.Value = 100;
                _launchButton.Enabled = true;
                _guardReady = true;
            }
            else
            {
                AddLog("[FAILED] Protection check failed!");
                _statusLabel.Text = "⚠ Bot/Cheat Detected - Cannot Launch";
                _statusLabel.ForeColor = System.Drawing.Color.Red;
                _progressBar.Visible = false;
            }
        }

        private void OnLaunchButtonClick(object? sender, EventArgs e)
        {
            if (!_guardReady)
            {
                MessageBox.Show("L2Guard is not ready. Please restart the launcher.",
                    "L2Guard", MessageBoxButtons.OK, MessageBoxIcon.Warning);
                return;
            }

            try
            {
                // Find L2.exe
                string gameExePath = FindGameExecutable();

                if (string.IsNullOrEmpty(gameExePath))
                {
                    MessageBox.Show("Could not find Lineage 2 executable (L2.exe).\n\nPlease place L2GuardLauncher.exe in your Lineage 2 game directory.",
                        "Game Not Found", MessageBoxButtons.OK, MessageBoxIcon.Error);
                    return;
                }

                AddLog("");
                AddLog($"Launching game: {gameExePath}");
                AddLog("L2Guard will monitor the game process...");

                // Launch the game
                var processStartInfo = new ProcessStartInfo
                {
                    FileName = gameExePath,
                    WorkingDirectory = Path.GetDirectoryName(gameExePath)
                };

                Process.Start(processStartInfo);

                AddLog("✓ Game launched successfully!");
                AddLog("L2Guard is now monitoring for threats...");
                AddLog("");
                AddLog("You can minimize this window.");
                AddLog("DO NOT close this window while playing!");

                _launchButton.Enabled = false;
                _launchButton.Text = "Game Running...";
            }
            catch (Exception ex)
            {
                MessageBox.Show($"Failed to launch game:\n{ex.Message}",
                    "Launch Error", MessageBoxButtons.OK, MessageBoxIcon.Error);
                AddLog($"[ERROR] Failed to launch: {ex.Message}");
            }
        }

        private string FindGameExecutable()
        {
            // Try common Lineage 2 executable names
            string[] possibleNames = { "L2.exe", "l2.exe", "Lineage2.exe", "lineage2.exe" };
            string currentDir = AppDomain.CurrentDomain.BaseDirectory;

            foreach (var name in possibleNames)
            {
                string fullPath = Path.Combine(currentDir, name);
                if (File.Exists(fullPath))
                {
                    return fullPath;
                }
            }

            // Also check in system folder
            string systemFolder = Path.Combine(currentDir, "system");
            if (Directory.Exists(systemFolder))
            {
                foreach (var name in possibleNames)
                {
                    string fullPath = Path.Combine(systemFolder, name);
                    if (File.Exists(fullPath))
                    {
                        return fullPath;
                    }
                }
            }

            return string.Empty;
        }

        private void OnBotDetected(object? sender, GuardEngine.BotDetectedEventArgs e)
        {
            if (InvokeRequired)
            {
                Invoke(new Action(() => OnBotDetected(sender, e)));
                return;
            }

            AddLog($"[!!! BOT DETECTED !!!]");
            AddLog($"  Bot Name: {e.BotName}");
            AddLog($"  Process: {e.ProcessName}");
            AddLog($"  Method: {e.DetectionMethod}");
            AddLog($"  Threat Level: {e.ThreatLevel}/10");
            AddLog("");

            MessageBox.Show(
                $"Bot Detected: {e.BotName}\n\n" +
                $"Process: {e.ProcessName}\n" +
                $"Detection: {e.DetectionMethod}\n\n" +
                $"The game will not start with bot software running.\n" +
                $"Please close the bot and restart L2Guard.",
                "Bot Detected",
                MessageBoxButtons.OK,
                MessageBoxIcon.Stop);

            _guardReady = false;
            _launchButton.Enabled = false;
        }

        private void OnDebuggerDetected(object? sender, GuardEngine.DebuggerDetectedEventArgs e)
        {
            if (InvokeRequired)
            {
                Invoke(new Action(() => OnDebuggerDetected(sender, e)));
                return;
            }

            AddLog($"[!!! DEBUGGER DETECTED !!!]");
            foreach (var method in e.DetectionMethods)
            {
                AddLog($"  - {method}");
            }
            AddLog("");

            MessageBox.Show(
                "Debugger or debugging tool detected.\n\n" +
                "The game cannot run with debuggers attached.\n" +
                "Please close all debugging tools and restart L2Guard.",
                "Debugger Detected",
                MessageBoxButtons.OK,
                MessageBoxIcon.Warning);

            _guardReady = false;
            _launchButton.Enabled = false;
        }

        private void OnStatusChanged(object? sender, GuardEngine.StatusChangedEventArgs e)
        {
            if (InvokeRequired)
            {
                Invoke(new Action(() => OnStatusChanged(sender, e)));
                return;
            }

            AddLog($"[Status] {e.Status}");
        }

        private void AddLog(string message)
        {
            if (InvokeRequired)
            {
                Invoke(new Action(() => AddLog(message)));
                return;
            }

            string timestamp = DateTime.Now.ToString("HH:mm:ss");
            _logTextBox.AppendText($"[{timestamp}] {message}\r\n");
        }

        protected override void OnFormClosing(FormClosingEventArgs e)
        {
            if (_guardReady && _launchButton.Text == "Game Running...")
            {
                var result = MessageBox.Show(
                    "The game is still running.\n\n" +
                    "Closing L2Guard will disable protection.\n" +
                    "Are you sure you want to close?",
                    "Confirm Close",
                    MessageBoxButtons.YesNo,
                    MessageBoxIcon.Warning);

                if (result == DialogResult.No)
                {
                    e.Cancel = true;
                    return;
                }
            }

            _guardEngine.Stop();
            base.OnFormClosing(e);
        }
    }
}
