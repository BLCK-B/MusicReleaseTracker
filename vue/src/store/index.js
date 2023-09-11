//vuex store
import { createStore } from 'vuex'

const state = {
  // Your initial state variables
  artist: [],
  sourceTab: "combview",
  tableData: [],
  addDialogVis: false,
  loadListRequest: false,
  allowButtons: true,
};

const mutations = {
  // Synchronous updates of states
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
};

const actions = {
  // Actions to perform async operations
  
};

const getters = {
  // Getters to retrieve computed state values
};

export default createStore({
  state,
  mutations,
  actions,
  getters
})