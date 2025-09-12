import {mount} from "@vue/test-utils";
import DialogsURL from "@/components/Content/DialogsURL.vue";
import {useMainStore} from "@/store/mainStore.ts";
import axios from "axios";
import {expect, vi, describe, beforeEach, it} from "vitest";
import {createTestingPinia} from "@pinia/testing";

vi.mock("axios");

describe("DialogsURL.vue", () => {
    let store;

    beforeEach(() => {
        axios.get.mockClear();
        axios.post.mockClear();
        axios.get.mockResolvedValue({
            data: [],
        });
        axios.post.mockResolvedValue({
            data: [],
        });
    });

    async function setup() {
        const wrapper = mount(DialogsURL, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    stubActions: false,
                    initialState: {
                        mainStore: {
                            tableData: [],
                            sourceTab: "beatport",
                            selectedArtist: "artist",
                            allowButtons: true,
                            primaryColor: "black",
                            urlExists: false,
                        }
                    }
                })],
            },
        });
        store = useMainStore();
        const dialog = wrapper.find(".dialog");
        const confirmButton = wrapper.find('[data-testid="confirm-button"]');
        return {wrapper, dialog, confirmButton};
    }

    it("Is visible when artist and source are selected and ID does not exist.", async () => {
        store.setUrlExists(false);
        const {dialog} = await setup(store);

        expect(dialog.exists()).toBe(true);
    });

    it("Is not visible when ID exists.", async () => {
        store.setUrlExists(true);
        const {dialog} = await setup(store);

        expect(dialog.exists()).toBe(false);
    });

    it("Does not send request when user input is empty.", async () => {
        store.setUrlExists(false);
        const {confirmButton} = await setup(store);

        await confirmButton.trigger("click");

        expect(axios.post).not.toHaveBeenCalledWith("/api/clickAddURL", expect.anything());
    });

    it("Sends request and opens preview when confirm clicked.", async () => {
        store.setUrlExists(false);
        const {wrapper, confirmButton} = await setup(store);

        const input = wrapper.find("input");
        input.element.value = "some url";
        await input.trigger("input");
        await confirmButton.trigger("click");

        expect(axios.post).toHaveBeenCalledWith("/api/scrapePreview", null, expect.anything());
        expect(mutations.SET_PREVIEW_VIS).toHaveBeenCalledWith(expect.anything(), true);
    });
});
