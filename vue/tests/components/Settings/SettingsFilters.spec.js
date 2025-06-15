import SettingsFilters from "@/components/Settings/SettingsFilters.vue";
import { expect, it } from "vitest";
import { mount } from "@vue/test-utils";

describe("SettingsFilters.vue", () => {
  async function setup() {
    const wrapper = mount(SettingsFilters, {
      props: {
        filterRemix: false,
        filterVIP: false,
        filterInstrumental: false,
        filterAcoustic: false,
        filterExtended: false,
        filterRemaster: false,
      },
    });
    return { wrapper };
  }

  it("Every input emits set-setting that contains 'filter'.", async () => {
    const { wrapper } = await setup();
    const inputs = wrapper.findAll('input[type="checkbox"]');

    let i = 0;
    for (let input of inputs) {
      await input.setChecked();

      const emittedEvent = wrapper.emitted("set-setting")[i];
      expect(emittedEvent[0]).toContain("filter");
      ++i;
    }
  });
});
