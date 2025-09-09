import ArtistsPreviewDialog from "@/components/Artists/ArtistsPreviewDialog.vue";
import { createStore } from "vuex";
import { mount } from "@vue/test-utils";
import axios from "axios";
import { expect, vi, describe, beforeEach, it } from "vitest";

vi.mock("axios");

describe("ArtistsPreviewDialog.vue", () => {
  let store;
  let mutations;

  beforeEach(() => {
    mutations = {
      SET_LOAD_REQUEST(state, value) {
        state.loadListRequest = value;
      },
      SET_PREVIEW_VIS: vi.fn(),
    };

    store = createStore({
      state() {
        return {
          previewVis: true,
          sourceTab: "beatport",
          selectedArtist: "joe",
          tableData: "abcd",
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
    const wrapper = mount(ArtistsPreviewDialog, {
      global: {
        plugins: [store],
      },
    });
    wrapper.vm.showDropdown = true;
    await wrapper.vm.$nextTick();
    wrapper.vm.artistsArrayList = ["Artist 1", "Artist 2"];
    const cancelButton = wrapper.find('[data-testid="cancel-button"]');
    const confirmButton = wrapper.find('[data-testid="confirm-button"]');
    return { wrapper, cancelButton, confirmButton };
  }

  it("Makes a request to clear artist source on cancel clicked.", async () => {
    const { cancelButton } = await setup(store);

    await cancelButton.trigger("click");

    expect(axios.post).toHaveBeenCalledWith("/api/cleanArtistSource", null, {
      params: {
        artist: "joe",
        source: "beatport",
      },
    });
  });

  it("Makes a request to save url on confirm clicked.", async () => {
    const { confirmButton } = await setup(store);

    await confirmButton.trigger("click");

    expect(axios.post).toHaveBeenCalledWith("/api/confirmSaveUrl", null, {
      params: {
        artist: "joe",
        source: "beatport",
      },
    });
  });

  it("Closes preview on any confirm click.", async () => {
    const { confirmButton } = await setup(store);

    await confirmButton.trigger("click");

    expect(mutations.SET_PREVIEW_VIS).toHaveBeenCalledWith(expect.anything(), false);
  });

  it("Closes preview on any cancel click.", async () => {
    const { cancelButton } = await setup(store);

    await cancelButton.trigger("click");

    expect(mutations.SET_PREVIEW_VIS).toHaveBeenCalledWith(expect.anything(), false);
  });
});
