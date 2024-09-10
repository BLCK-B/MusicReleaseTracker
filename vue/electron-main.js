/* eslint-disable no-undef */
import { app, BrowserWindow, Menu } from "electron";
import path from "path";
import { fileURLToPath } from "url";
import { spawn } from "child_process";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

app.disableHardwareAcceleration();

let externalEXE;

function createWindow() {
  if (process.env.NODE_ENV !== "development") Menu.setApplicationMenu(null);
  const win = new BrowserWindow({
    icon: path.join(__dirname, "buildResources/MRTicon.ico"),
    show: false,
    backgroundColor: "#000000",
    width: 900,
    height: 650,
    minHeight: 450,
    minWidth: 700,
    webPreferences: {
      preload: path.join(__dirname, "electron-preload.js"),
    },
  });

  win.loadURL("http://localhost:57782");

  win.once("ready-to-show", () => {
    win.show();
  });

  win.on("closed", () => {
    if (externalEXE) {
      externalEXE.kill();
    }
  });
}

app.whenReady().then(() => {
  if (process.env.NODE_ENV !== "development") {
    externalEXE = spawn("buildResources/MusicReleaseTracker", {
      detached: true,
      stdio: "ignore", // ignore stdio to prevent blocking
    });

    externalEXE.unref(); // allow the parent process to exit independently
  }

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
