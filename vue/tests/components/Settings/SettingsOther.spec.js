import SettingsOther from "@/components/Settings/SettingsOther.vue";
import { expect, it } from "vitest";
import { mount } from "@vue/test-utils";

describe("SettingsOther.vue", () => {
  async function setup() {
    const wrapper = mount(SettingsOther, {
      props: {
        isoDates: false,
      },
    });
    return { wrapper };
  }

  it("Every input emits set-setting.", async () => {
    const { wrapper } = await setup();
    const inputs = wrapper.findAll('input[type="checkbox"]');

    let i = 0;
    for (let input of inputs) {
      await input.setChecked();

      const emittedEvent = wrapper.emitted("set-setting")[i];
      expect(typeof emittedEvent[0]).toBe("string");
      ++i;
    }
  });
});
