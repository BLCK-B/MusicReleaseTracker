import SettingsDangerZone from "@/components/Settings/SettingsDangerZone.vue";
import { expect, it } from "vitest";
import { mount } from "@vue/test-utils";
import axios from "axios";

vi.mock("axios");

describe("SettingsDangerZone.vue", () => {
  async function setup() {
    const wrapper = mount(SettingsDangerZone);
    const resetSettingsBtn = wrapper.find('[data-testid="reset-settings-btn"]');
    const resetDbBtn = wrapper.find('[data-testid="reset-db-btn"]');
    return { wrapper, resetSettingsBtn, resetDbBtn };
  }

  beforeEach(() => {
    axios.post.mockResolvedValue({
      data: [],
    });
    axios.post.mockClear();
  });

  it("Requires two clicks on reset buttons to trigger call.", async () => {
    const { resetSettingsBtn, resetDbBtn } = await setup();

    await resetSettingsBtn.trigger("click");
    expect(axios.post).not.toHaveBeenCalled();

    await resetSettingsBtn.trigger("click");
    expect(axios.post).toHaveBeenCalledWith("/api/resetSettings");

    axios.post.mockClear();

    await resetDbBtn.trigger("click");
    expect(axios.post).not.toHaveBeenCalled();

    await resetDbBtn.trigger("click");
    expect(axios.post).toHaveBeenCalledWith("/api/resetDB");
  });

  it("Resets reset buttons' protection on mouse off.", async () => {
    const { resetSettingsBtn, resetDbBtn } = await setup();

    expect(resetSettingsBtn.text()).toContain("Reset");
    await resetSettingsBtn.trigger("click");
    expect(resetSettingsBtn.text()).toContain("Confirm");
    await resetSettingsBtn.trigger("mouseleave");
    expect(resetSettingsBtn.text()).toContain("Reset");

    expect(resetDbBtn.text()).toContain("Reset");
    await resetDbBtn.trigger("click");
    expect(resetDbBtn.text()).toContain("Confirm");
    await resetDbBtn.trigger("mouseleave");
    expect(resetDbBtn.text()).toContain("Reset");
  });
});
