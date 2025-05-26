import { mount } from "@vue/test-utils";
import ArtistList from "@/components/Artists/ArtistList.vue";
import { createStore } from "vuex";
import axios from "axios";
import { expect, vi } from "vitest";

vi.mock("axios");

describe("ArtistList.vue", () => {
  let store;
  let mutations;

  beforeEach(() => {
    mutations = {
      SET_LOAD_REQUEST(state, value) {
        state.loadListRequest = value;
      },
      SET_SELECTED_ARTIST: vi.fn(),
      SET_TABLE_CONTENT: vi.fn(),
    };

    store = createStore({
      state() {
        return {
          allowButtons: true,
          sourceTab: "beatport",
          selectedArtist: "Artist 1",
          previewVis: false,
          loadListRequest: false,
        };
      },
      mutations,
    });

    axios.get.mockResolvedValue({
      data: [],
    });
    axios.post.mockResolvedValue({
      data: [],
    });
    axios.get.mockClear();
    axios.post.mockClear();
  });

  async function setup(store) {
    const wrapper = mount(ArtistList, {
      global: {
        plugins: [store],
      },
    });
    wrapper.vm.showDropdown = true;
    await wrapper.vm.$nextTick();
    wrapper.vm.artistsArrayList = ["Artist 1", "Artist 2"];
    const deleteButton = wrapper.find('[data-testid="delete-button"]');
    const deleteUrlButton = wrapper.find('[data-testid="delete-url-button"]');
    return { wrapper, deleteButton, deleteUrlButton };
  }

  it("Loads data on mounted.", async () => {
    await setup(store);

    const calls = axios.get.mock.calls.filter(([url]) => url === "/api/loadList");
    expect(calls.length).toEqual(1);
  });

  it("Loads data and resets flag on SET_LOAD_REQUEST.", async () => {
    const { wrapper } = await setup(store);

    store.commit("SET_LOAD_REQUEST", true);
    await wrapper.vm.$nextTick();

    const calls = axios.get.mock.calls.filter(([url]) => url === "/api/loadList");
    expect(calls.length).toEqual(2);
    expect(store.state.loadListRequest).toEqual(false);
  });

  it("Loads table data on artist click.", async () => {
    const { wrapper } = await setup(store);
    const items = wrapper.findAll("li.listbtn");

    await items[1].trigger("mousedown");

    expect(axios.post).toHaveBeenCalledWith("/api/getTableData", { artist: "Artist 2", source: "beatport" });
  });

  it("Calls delete artist on delete button click.", async () => {
    const { deleteButton } = await setup(store);

    await deleteButton.trigger("click");

    expect(axios.post).toHaveBeenCalledWith("/api/deleteArtist", "Artist 1");
  });

  it("Deletes URL and loads table when button clicked.", async () => {
    const { deleteUrlButton } = await setup(store);

    await deleteUrlButton.trigger("click");

    expect(axios.post).toHaveBeenCalledWith("/api/deleteUrl", { artist: "Artist 1", source: "beatport" });
    expect(axios.post).toHaveBeenCalledWith("/api/getTableData", { artist: "Artist 1", source: "beatport" });
  });
});
