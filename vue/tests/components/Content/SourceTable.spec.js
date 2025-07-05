import { mount } from "@vue/test-utils";
import SourceTable from "@/components/Content/SourceTable.vue";
import { createStore } from "vuex";
import { expect, vi } from "vitest";

describe("SourceTable.vue", () => {
  let store;
  let mutations;

  beforeEach(() => {
    mutations = {
      SET_URL_EXISTS(state, value) {
        state.urlExists = value;
      },
      SET_TABLE_CONTENT(state, value) {
        state.tableData = value;
      },
      SET_SOURCE_TAB(state, value) {
        state.sourceTab = value;
      },
      SET_SELECTED_ARTIST: vi.fn(),
      SET_ALLOW_BUTTONS: vi.fn(),
      SET_PREVIEW_VIS: vi.fn(),
    };

    store = createStore({
      state() {
        return {
          tableData: [{ song: "something" }],
          selectedArtist: "artist",
          previewVis: false,
          isoDates: false,
          sourceTab: "beatport",
          urlExists: false,
        };
      },
      mutations,
    });
  });

  async function setup(store) {
    const wrapper = mount(SourceTable, {
      global: {
        plugins: [store],
      },
    });
    return { wrapper };
  }

  it("Table container is visible only when table data is not empty.", async () => {
    const { wrapper } = await setup(store);

    expect(wrapper.find(".table-container").exists()).toBe(true);

    store.commit("SET_TABLE_CONTENT", []);
    await wrapper.vm.$nextTick();

    expect(wrapper.find(".table-container").exists()).toBe(false);
  });

  it("Quick guide is visible when nothing else is and sourceTab = combview.", async () => {
    const { wrapper } = await setup(store);

    expect(wrapper.find(".quickstart").exists()).toBe(false);

    store.commit("SET_TABLE_CONTENT", []);
    store.commit("SET_SOURCE_TAB", "combview");
    await wrapper.vm.$nextTick();

    expect(wrapper.find(".quickstart").exists()).toBe(true);
  });

  it("Text: empty table is visible when nothing else is, URL exists and sourceTab != combview.", async () => {
    const { wrapper } = await setup(store);

    expect(wrapper.find(".emptynotice").exists()).toBe(false);

    store.commit("SET_TABLE_CONTENT", []);
    store.commit("SET_SOURCE_TAB", "beatport");
    store.commit("SET_URL_EXISTS", true);
    await wrapper.vm.$nextTick();

    expect(wrapper.find(".emptynotice").exists()).toBe(true);

    store.commit("SET_SOURCE_TAB", "combview");
    await wrapper.vm.$nextTick();

    expect(wrapper.find(".emptynotice").exists()).toBe(false);
  });
});
