import {mount} from "@vue/test-utils";
import ArtistList from "@/components/Artists/ArtistList.vue";
import {useMainStore} from "@/store/mainStore.ts";
import {createTestingPinia} from '@pinia/testing';
import axios from "axios";
import {expect, vi, describe, beforeEach, it} from "vitest";
import {nextTick} from "vue";

vi.mock("axios");

describe("ArtistList.vue", () => {
    let store;

    beforeEach(() => {
        axios.get.mockResolvedValue({
            data: [],
        });
        axios.post.mockResolvedValue({
            data: [],
        });
        axios.delete.mockResolvedValue({
            data: [],
        });
        axios.get.mockClear();
        axios.post.mockClear();
        axios.delete.mockClear();
    });

    async function setup() {
        const wrapper = mount(ArtistList, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    stubActions: false,
                    initialState: {
                        mainStore: {
                            allowButtons: true,
                            sourceTab: "beatport",
                            selectedArtist: "Artist 1",
                            previewVis: false,
                            loadListRequest: false,
                        },
                    },
                })],
            },
        });
        store = useMainStore();
        wrapper.vm.showDropdown = true;
        await nextTick();
        wrapper.vm.artistsArrayList = ["Artist 1", "Artist 2"];
        const deleteButton = wrapper.find('[testid="delete-button"]');
        const deleteUrlButton = wrapper.find('[testid="delete-url-button"]');
        return {wrapper, deleteButton, deleteUrlButton};
    }

    it("Loads data on mounted.", async () => {
        await setup();

        const calls = axios.get.mock.calls.filter(([url]) => url === "/api/loadList");
        expect(calls.length).toEqual(1);
    });

    it("Loads data and resets flag on SET_LOAD_REQUEST.", async () => {
        await setup();

        store.setLoadRequest(true);
        await nextTick();

        const calls = axios.get.mock.calls.filter(([url]) => url === "/api/loadList");
        expect(calls.length).toEqual(2);
        expect(store.loadListRequest).toEqual(false);
    });
});
