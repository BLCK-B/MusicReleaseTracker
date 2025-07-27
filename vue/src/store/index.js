// vuex store
import { createStore } from "vuex";

const state = {
  selectedArtist: "",
  sourceTab: "",
  tableData: [],
  loadListRequest: false,
  allowButtons: true,
  progress: 0,
  settingsOpen: false,
  primaryColor: "",
  accentColor: "",
  previewVis: false,
  isoDates: false,
  urlExists: false,
};

const mutations = {
  SET_SOURCE_TAB(state, sourceTab) {
    state.sourceTab = sourceTab;
  },
  SET_SELECTED_ARTIST(state, selectedArtist) {
    state.selectedArtist = selectedArtist;
  },
  SET_TABLE_CONTENT(state, tableData) {
    state.tableData = tableData;
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
  SET_ISODATES(state, isoDates) {
    state.isoDates = isoDates;
  },
  SET_URL_EXISTS(state, urlExists) {
    state.urlExists = urlExists;
  },
};

const actions = {};

const getters = {
  getPrimaryColor(state) {
    return state.primaryColor;
  },
  getAccentColor(state) {
    return state.accentColor;
  },
};

export default createStore({
  state,
  mutations,
  actions,
  getters,
});
