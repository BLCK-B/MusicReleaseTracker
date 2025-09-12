import ArtistsPreviewDialog from "@/components/Artists/ArtistsPreviewDialog.vue";
import {useMainStore} from "@/store/mainStore.ts";
import {mount} from "@vue/test-utils";
import axios from "axios";
import {expect, vi, describe, beforeEach, it} from "vitest";
import {createTestingPinia} from "@pinia/testing";
import {nextTick} from "vue";

vi.mock("axios");

describe("ArtistsPreviewDialog.vue", () => {
    let store;

    beforeEach(() => {
        axios.get.mockResolvedValue({
            data: [],
        });
        axios.post.mockResolvedValue({
            data: [],
        });
        axios.get.mockClear();
        axios.post.mockClear();
    });

    async function setup() {
        const wrapper = mount(ArtistsPreviewDialog, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    stubActions: false,
                    initialState: {
                        mainStore: {
                            previewVis: true,
                            sourceTab: "beatport",
                            selectedArtist: "joe",
                            tableData: "abcd",
                        }
                    }
                })],
            },
        });
        store = useMainStore();
        wrapper.vm.showDropdown = true;
        await nextTick();
        wrapper.vm.artistsArrayList = ["Artist 1", "Artist 2"];
        const cancelButton = wrapper.find('[data-testid="cancel-button"]');
        const confirmButton = wrapper.find('[data-testid="confirm-button"]');
        return {wrapper, cancelButton, confirmButton};
    }

    it("Makes a request to clear artist source on cancel clicked.", async () => {
        const {cancelButton} = await setup(store);

        await cancelButton.trigger("click");

        expect(axios.post).toHaveBeenCalledWith("/api/cleanArtistSource", null, {
            params: {
                artist: "joe",
                source: "beatport",
            },
        });
    });

    it("Makes a request to save url on confirm clicked.", async () => {
        const {confirmButton} = await setup(store);

        await confirmButton.trigger("click");

        expect(axios.post).toHaveBeenCalledWith("/api/confirmSaveUrl", null, {
            params: {
                artist: "joe",
                source: "beatport",
            },
        });
    });

    it("Closes preview on any confirm click.", async () => {
        const {confirmButton} = await setup(store);

        await confirmButton.trigger("click");

        expect(store.previewVis).toBe(false);
    });

    it("Closes preview on any cancel click.", async () => {
        const {cancelButton} = await setup(store);

        await cancelButton.trigger("click");

        expect(store.previewVis).toBe(false);
    });
});
