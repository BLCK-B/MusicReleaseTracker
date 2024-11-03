/* eslint-disable no-undef */
import { app, BrowserWindow, Menu, shell } from "electron";
import path from "path";
import { fileURLToPath } from "url";
import { spawn } from "child_process";
import axios from "axios";
import windowStateKeeper from "electron-window-state";

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
    minHeight: 450,
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
    externalEXE = spawn("buildResources/MusicReleaseTracker", { detached: true, stdio: "ignore" });
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
