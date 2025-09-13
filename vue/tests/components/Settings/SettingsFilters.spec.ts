import SettingsAppearance from "@/components/Settings/SettingsFilters.vue";
import {expect, describe, it} from "vitest";
import {mount} from "@vue/test-utils";

describe("SettingsFilters.vue", () => {
    async function setup() {
        const wrapper = mount(SettingsAppearance, {
            props: {
                filterRemix: false,
                filterVIP: false,
                filterInstrumental: false,
                filterAcoustic: false,
                filterExtended: false,
                filterRemaster: false,
            },
        });
        return {wrapper};
    }

    it("Every input emits set-setting.", async () => {
        const {wrapper} = await setup();
        const inputs = wrapper.findAll('input[type="checkbox"], input[type="radio"]');

        let i = 0;
        for (let input of inputs) {
            await input.setChecked();

            const emittedEvent = wrapper.emitted("set-setting")[i];
            expect(typeof emittedEvent[0]).toBe("string");
            ++i;
        }
    });
});
