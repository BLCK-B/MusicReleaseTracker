//vuex store
import { createStore } from 'vuex'

const state = {
  artist: "",
  sourceTab: "",
  tableData: [],
  addDialogVis: false,
  loadListRequest: false,
  allowButtons: true,
  progress: 0,
  settingsOpen: false,
  primaryColor: "",
  accentColor: "",
  previewVis: false,
};

const mutations = {
  //synchronous updates of states
  SET_SOURCE_TAB(state, sourceTab) {
    state.sourceTab = sourceTab;
  },
  SET_SELECTED_ARTIST(state, artist) {
    state.artist = artist;
  },
  SET_TABLE_CONTENT(state, tableData) {
    state.tableData = tableData;
  },
  SET_ADD_VIS(state, addDialogVis) {
    state.addDialogVis = addDialogVis;
  },
  SET_LOAD_REQUEST(state, loadListRequest) {
    state.loadListRequest = loadListRequest;
  },
  SET_ALLOW_BUTTONS(state, allowButtons) {
    state.allowButtons = allowButtons;
  },
  SET_PROGRESS(state, progress) {
    state.progress = progress * 100;
  },
  SET_SETTINGS_OPEN(state, settingsOpen) {
    state.settingsOpen = settingsOpen;
  },
  SET_PRIMARY_COLOR(state, color) {
    state.primaryColor = color;
  },
  SET_ACCENT_COLOR(state, color) {
    state.accentColor = color;
  },
  SET_PREVIEW_VIS(state, previewVis) {
    state.previewVis = previewVis;
  },
};

const actions = {
  //actions to perform async operations
  
};

const getters = {
  //getters to retrieve computed state values
};

export default createStore({
  state,
  mutations,
  actions,
  getters
})