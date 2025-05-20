import { mount } from "@vue/test-utils";
import ArtistsAddNew from "@/components/Artists/ArtistsAddNew.vue";
import { createStore } from "vuex";
import axios from "axios";
import { vi } from "vitest";

vi.mock("axios");

describe("ArtistsAddNew.vue", () => {
  let store;
  let mutations;

  beforeEach(() => {
    mutations = {
      SET_SELECTED_ARTIST: vi.fn(),
      SET_LOAD_REQUEST: vi.fn(),
    };

    store = createStore({
      state() {
        return { primaryColor: "dark" };
      },
      mutations,
    });
  });

  async function mountAndAddArtist(store) {
    const wrapper = mount(ArtistsAddNew, {
      props: { addVisibility: true },
      global: { plugins: [store] },
    });

    const input = wrapper.find("input");

    const addButton = wrapper.find('[data-testid="add-button"]');
    const closeButton = wrapper.find('[data-testid="close-button"]');

    axios.post.mockResolvedValue({ data: {} });
    return { wrapper, input, addButton, closeButton };
  }

  it("Validates user input and if invalid, disables confirm button.", async () => {
    const { wrapper, input, addButton } = await mountAndAddArtist(store);

    // empty input forbidden
    await input.setValue("");
    expect(wrapper.vm.isValid).toBe(false);
    expect(addButton.attributes("disabled")).toBeDefined();

    // limit input length
    await input.setValue("A".repeat(30));
    expect(wrapper.vm.isValid).toBe(false);
    expect(addButton.attributes("disabled")).toBeDefined();

    // valid input
    await input.setValue("Joe");
    expect(wrapper.vm.isValid).toBe(true);
    expect(addButton.attributes("disabled")).toBeUndefined();
  });

  it("Emits close event when close button is clicked.", async () => {
    const { wrapper, closeButton } = await mountAndAddArtist(store);

    await closeButton.trigger("click");

    expect(wrapper.emitted("close-add-new")).toHaveLength(1);
  });

  it("Sets new artist as selected artist.", async () => {
    const { input, addButton } = await mountAndAddArtist(store);

    await input.setValue("Joe");
    await addButton.trigger("click");

    expect(mutations.SET_SELECTED_ARTIST).toHaveBeenCalledWith(expect.anything(), encodeURIComponent("Joe"));
  });

  it("Requests artistList reload when artist added.", async () => {
    const { input, addButton } = await mountAndAddArtist(store);

    await input.setValue("Joe");
    await addButton.trigger("click");

    expect(mutations.SET_LOAD_REQUEST).toHaveBeenCalledWith(expect.anything(), true);
  });

  it("Emits close event when artist added.", async () => {
    const { wrapper, input, addButton } = await mountAndAddArtist(store);

    await input.setValue("Joe");
    await addButton.trigger("click");

    expect(wrapper.emitted("close-add-new")).toHaveLength(1);
  });
});
