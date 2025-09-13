import {mount} from "@vue/test-utils";
import ArtistsAddNew from "@/components/Artists/ArtistsAddNew.vue";
import {useMainStore} from "@/store/mainStore.ts";
import axios from "axios";
import {expect, vi, describe, beforeEach, it} from "vitest";
import {createTestingPinia} from "@pinia/testing";

vi.mock("axios");

describe("ArtistsAddNew.vue", () => {
    let store;

    beforeEach(() => {
        axios.get.mockClear();
        axios.post.mockClear();
    });

    async function setup() {
        const wrapper = mount(ArtistsAddNew, {
            props: {addVisibility: true},
            global: {
                plugins: [createTestingPinia({
                    createSpy: vi.fn,
                    stubActions: false,
                    initialState: {
                        mainStore: {
                            primaryColor: "dark"
                        }
                    }
                })],
            },
        });
        store = useMainStore();

        const input = wrapper.find("input");

        const addButton = wrapper.find('[testid="add-button"]');
        const closeButton = wrapper.find('[testid="close-button"]');

        return {wrapper, input, addButton, closeButton};
    }

    it("Validates user input and if invalid, disables confirm button.", async () => {
        const {wrapper, input, addButton} = await setup();

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
        const {wrapper, closeButton} = await setup();

        await closeButton.trigger("click");

        expect(wrapper.emitted("close-add-new")).toHaveLength(1);
    });

    it("Sets new artist as selected artist.", async () => {
        const {input, addButton} = await setup();

        await input.setValue("Joe");
        await addButton.trigger("click");

        expect(store.selectedArtist).toBe("Joe");
    });

    it("Requests artistList reload when artist added.", async () => {
        const {input, addButton} = await setup();

        await input.setValue("Joe");
        await addButton.trigger("click");

        expect(store.loadListRequest).toBe(true);
    });

    it("Emits close event when artist added.", async () => {
        const {wrapper, input, addButton} = await setup();

        await input.setValue("Joe");
        await addButton.trigger("click");

        expect(wrapper.emitted("close-add-new")).toHaveLength(1);
    });
});
