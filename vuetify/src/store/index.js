//vuex store
import { createStore } from 'vuex'

const state = {
  // Your initial state variables go here
  sourceTab: 'combview',
  tableData: null,
};

const mutations = {
  // Mutations to update the state go here
  SET_SOURCE_TAB(state, newTab) {
    state.sourceTab = newTab;
    console.log(state.sourceTab);
  },
  SET_TABLE_CONTENT(state, tableData) {
    state.tableData = tableData;
  },
};

const actions = {
  // Actions to perform async operations go here
};

const getters = {
  // Getters to retrieve computed state values go here
};

export default createStore({
  state,
  mutations,
  actions,
  getters
})