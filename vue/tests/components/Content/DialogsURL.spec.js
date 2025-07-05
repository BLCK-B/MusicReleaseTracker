import { mount } from "@vue/test-utils";
import DialogsURL from "@/components/Content/DialogsURL.vue";
import { createStore } from "vuex";
import axios from "axios";
import { expect, vi } from "vitest";

vi.mock("axios");

describe("DialogsURL.vue", () => {
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
      SET_SELECTED_ARTIST: vi.fn(),
      SET_SOURCE_TAB: vi.fn(),
      SET_ALLOW_BUTTONS: vi.fn(),
      SET_PREVIEW_VIS: vi.fn(),
    };

    store = createStore({
      state() {
        return {
          tableData: [],
          sourceTab: "beatport",
          selectedArtist: "artist",
          allowButtons: true,
          primaryColor: "black",
          urlExists: false,
        };
      },
      mutations,
    });
    axios.get.mockClear();
    axios.post.mockClear();
    axios.get.mockResolvedValue({
      data: [],
    });
    axios.post.mockResolvedValue({
      data: [],
    });
  });

  async function setup(store) {
    const wrapper = mount(DialogsURL, {
      global: {
        plugins: [store],
      },
    });
    const dialog = wrapper.find(".dialog");
    const confirmButton = wrapper.find('[data-testid="confirm-button"]');
    return { wrapper, dialog, confirmButton };
  }

  it("Is visible when artist and source are selected and ID does not exist.", async () => {
    store.commit("SET_URL_EXISTS", false);
    const { wrapper, dialog } = await setup(store);

    expect(dialog.exists()).toBe(true);
  });

  it("Is not visible when ID exists.", async () => {
    store.commit("SET_URL_EXISTS", true);
    const { wrapper, dialog } = await setup(store);

    expect(dialog.exists()).toBe(false);
  });

  it("Does not send request when user input is empty.", async () => {
    store.commit("SET_URL_EXISTS", false);
    const { wrapper, dialog, confirmButton } = await setup(store);

    await confirmButton.trigger("click");

    expect(axios.post).not.toHaveBeenCalledWith("/api/clickAddURL", expect.anything());
  });

  it("Sends request and opens preview when confirm clicked.", async () => {
    store.commit("SET_URL_EXISTS", false);
    const { wrapper, dialog, confirmButton } = await setup(store);

    const input = wrapper.find("input");
    input.element.value = "some url";
    await input.trigger("input");
    await confirmButton.trigger("click");

    expect(axios.post).toHaveBeenCalledWith("/api/clickAddURL", expect.anything());
    expect(mutations.SET_PREVIEW_VIS).toHaveBeenCalledWith(expect.anything(), true);
  });
});
