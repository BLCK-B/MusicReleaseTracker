/* eslint-disable no-undef */
import { app, BrowserWindow } from "electron";
import path from "path";
import { fileURLToPath } from "url";
import { spawn } from "child_process";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

let externalEXE;

function createWindow() {
  const win = new BrowserWindow({
    width: 800,
    height: 600,
    webPreferences: {
      preload: path.join(__dirname, "electron-preload.js"),
    },
  });

  win.loadURL("http://localhost:57782");

  win.on("closed", () => {
    if (externalEXE) {
      externalEXE.kill();
    }
  });
}

app.whenReady().then(() => {
  externalEXE = spawn("../build/native/nativeCompile/MusicReleaseTracker.exe", {
    detached: true,
    stdio: "ignore", // ignore stdio to prevent blocking
  });

  externalEXE.unref(); // allow the parent process to exit independently

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
