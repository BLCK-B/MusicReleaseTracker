/* eslint-disable no-undef */
import { app, BrowserWindow, Menu, shell, dialog } from "electron";
import path from "path";
import { fileURLToPath } from "url";
import { spawn } from "child_process";
import axios from "axios";
import windowStateKeeper from "electron-window-state";
import fs from "fs";
import os from "os";

axios.defaults.baseURL = "http://localhost:57782";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const logFilePath = getLogPath();

function getLogPath() {
  let appDataPath;
  switch (process.platform) {
    case "win32":
      appDataPath = app.getPath("appData");
      break;
    case "darwin":
    case "linux":
      appDataPath = os.homedir();
      break;
    default:
      throw new Error("Unsupported OS");
  }
  return path.join(appDataPath, "MusicReleaseTracker", "errorlogs.txt");
}

app.disableHardwareAcceleration();

let externalEXE;

function createWindow() {
  if (process.env.NODE_ENV !== "development") Menu.setApplicationMenu(null);

  // single instance allowed
  if (!app.requestSingleInstanceLock()) app.quit();

  const mainWindowState = windowStateKeeper({
    defaultWidth: 900,
    defaultHeight: 650,
  });

  const win = new BrowserWindow({
    icon: path.join(__dirname, "buildResources/MRTicon.ico"),
    show: false,
    backgroundColor: "#000000",
    width: mainWindowState.width,
    height: mainWindowState.height,
    minHeight: 500,
    minWidth: 720,
    x: mainWindowState.x,
    y: mainWindowState.y,
    webPreferences: {
      preload: path.join(__dirname, "electron-preload.js"),
    },
  });

  mainWindowState.manage(win);

  win.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: "deny" };
  });

  if (process.env.NODE_ENV !== "development") win.loadURL("http://localhost:57782");
  else win.loadURL("http://localhost:57782", { extraHeaders: "Cache-Control: no-cache" });

  win.once("ready-to-show", () => {
    win.show();
  });

  win.on("closed", () => {
    if (externalEXE) {
      externalEXE.kill();
    }
  });
}

// could be better than this polling
async function checkBackendReady() {
  let attempts = 0;
  while (true) {
    try {
      const response = await axios.get("/api/isBackendReady");
      if (response.data === true) break;
    } catch (error) {
      console.log("Waiting for backend ready.");
    }
    await new Promise((resolve) => setTimeout(resolve, 5));
    attempts++;
    if (attempts === 150) {
      writeLog(`Backend connection timeout.`);
      electronStartErrorDialog({ message: "Backend connection timeout." });
    }
  }
}

function writeLog(message) {
  const timestamp = new Date().toISOString();
  const logMessage = `${timestamp} - Electron log: ${message}\n`;
  console.log(logMessage);
  fs.appendFile(logFilePath, logMessage, (err) => {
    if (err) console.error("Error writing to log", err);
  });
}

function electronStartErrorDialog(error) {
  dialog.showErrorBox(
    `Error: ${error.message}`,
    `Please report this at the issue tracker.\n\nThe error logs file is located in: ${logFilePath}`
  );
  app.quit();
}

app.whenReady().then(async () => {
  // needs an open backend in dev to connect to
  if (process.env.NODE_ENV !== "development") {
    let currentDir = __dirname;
    // climb up until buildResources is present - necessary for portables
    for (let i = 0; i <= 2; ++i) {
      currentDir = path.dirname(currentDir);
      try {
        const contents = fs.readdirSync(currentDir);
        writeLog(`Contents at Level ${i}: ${currentDir} - ${contents}`);
        if (contents.includes("buildResources")) {
          writeLog("buildResources is present");
          const truePath = path.join(currentDir, "buildResources", "MusicReleaseTracker");
          externalEXE = spawn(truePath, { detached: false, stdio: "ignore" });
          break;
        }
      } catch (e) {
        writeLog(`Error reading directory ${e}`);
        electronStartErrorDialog(e);
      }
      if (i == 2) {
        writeLog("Backend executable not found.");
        electronStartErrorDialog({ message: "Backend executable not found." });
      }
    }
  }

  await checkBackendReady();

  createWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
