import {mount} from "@vue/test-utils";
import SourceTable from "@/components/Content/SourceTable.vue";
import {useMainStore} from "@/store/mainStore.ts";
import {expect, vi, describe, beforeEach, it} from "vitest";
import {nextTick} from "vue";
import {createTestingPinia} from "@pinia/testing";

describe("SourceTable.vue", () => {
    let store;

    async function setup() {
        const wrapper = mount(SourceTable, {
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    stubActions: false,
                    initialState: {
                        mainStore: {
                            tableData: [{song: "something"}],
                            selectedArtist: "artist",
                            previewVis: false,
                            isoDates: false,
                            sourceTab: "beatport",
                            urlExists: false,
                        }
                    }
                })],
            },
        });
        store = useMainStore();
        return {wrapper};
    }

    it("Table container is visible only when table data is not empty.", async () => {
        const {wrapper} = await setup(store);

        expect(wrapper.find(".table-container").exists()).toBe(true);

        store.setTableContent([]);
        await nextTick();

        expect(wrapper.find(".table-container").exists()).toBe(false);
    });

    it("Quick guide is visible when nothing else is and sourceTab = combview.", async () => {
        const {wrapper} = await setup(store);

        expect(wrapper.find(".quickstart").exists()).toBe(false);

        store.setTableContent([]);
        store.setSourceTab("combview");
        await nextTick();

        expect(wrapper.find(".quickstart").exists()).toBe(true);
    });

    it("Text: empty table is visible when nothing else is, URL exists and sourceTab != combview.", async () => {
        const {wrapper} = await setup(store);

        expect(wrapper.find(".emptynotice").exists()).toBe(false);

        store.setTableContent([]);
        store.setSourceTab("beatport");
        store.setUrlExists(true);
        await nextTick();

        expect(wrapper.find(".emptynotice").exists()).toBe(true);

        store.setSourceTab("combview");
        await nextTick();

        expect(wrapper.find(".emptynotice").exists()).toBe(false);
    });
});
