/* eslint-disable no-undef */
import { app, BrowserWindow, Menu, shell } from "electron";
import path from "path";
import { fileURLToPath } from "url";
import { spawn } from "child_process";
import axios from "axios";
import windowStateKeeper from "electron-window-state";
import fs from "fs";

axios.defaults.baseURL = "http://localhost:57782";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

app.disableHardwareAcceleration();

let externalEXE;

function createWindow() {
  if (process.env.NODE_ENV !== "development") Menu.setApplicationMenu(null);

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
  while (true) {
    try {
      const response = await axios.get("/api/isBackendReady");
      if (response.data === true) break;
    } catch (error) {
      console.error();
    }
    await new Promise((resolve) => setTimeout(resolve, 5));
  }
}

app.whenReady().then(async () => {
  // needs open backend in dev to run
  if (process.env.NODE_ENV !== "development") {
    let currentDir = __dirname;
    // climb up until buildResources is present - necessary for portables
    for (let i = 0; i <= 2; ++i) {
      currentDir = path.dirname(currentDir);
      try {
        const contents = fs.readdirSync(currentDir);
        console.log(`Contents at Level ${i}: ${currentDir} - ${contents}`);
        if (contents.includes("buildResources")) {
          console.log("buildResources is present");
        }
        if (i == 1) {
          const truePath = path.join(currentDir, "buildResources", "MusicReleaseTracker");
          externalEXE = spawn(truePath, { detached: true, stdio: "ignore" });
          break;
        }
      } catch (e) {
        console.error(`Error reading directory ${e}`);
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
