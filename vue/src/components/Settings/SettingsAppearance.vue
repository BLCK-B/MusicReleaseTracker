<template>
  <p class="title">Appearance</p>
  <div class="appearancecont">
    <div class="theme-buttons">
      <input type="radio" :checked="primaryColor === 'black'" @change="$emit('set-setting', 'theme', 'black')" :disabled="autoTheme" />
      <label :class="{ disabled: autoTheme }">Black</label>
      <input type="radio" :checked="primaryColor === 'dark'" @change="$emit('set-setting', 'theme', 'dark')" :disabled="autoTheme" />
      <label :class="{ disabled: autoTheme }">Dark</label>
      <input type="radio" :checked="primaryColor === 'light'" @change="$emit('set-setting', 'theme', 'light')" :disabled="autoTheme" />
      <label :class="{ disabled: autoTheme }">Light</label>

      <div class="colorindicator"></div>
    </div>

    <div class="accent-buttons">
      <input type="radio" :checked="accentColor === 'cactus'" @change="$emit('set-setting', 'accent', 'cactus')" />
      <label>Cactus</label>
      <input type="radio" :checked="accentColor === 'cloud'" @change="$emit('set-setting', 'accent', 'cloud')" />
      <label>Cloud</label>
      <input type="radio" :checked="accentColor === 'lavender'" @change="$emit('set-setting', 'accent', 'lavender')" />
      <label>Lavender</label>
      <input type="radio" :checked="accentColor === 'ocean'" @change="$emit('set-setting', 'accent', 'ocean')" />
      <label>Ocean</label>
      <input type="radio" :checked="accentColor === 'rose'" @change="$emit('set-setting', 'accent', 'rose')" />
      <label>Rose</label>
      <input type="radio" :checked="accentColor === 'warm'" @change="$emit('set-setting', 'accent', 'warm')" />
      <label>Warm</label>
    </div>
  </div>

  <div class="belowAppearance">
    <input type="checkbox" :checked="autoTheme" @change="$emit('set-setting', 'autoTheme', $event.target.checked)" />
    <label>Match system theme</label>

    <div class="scaling">
      Scale:
      <select v-model.number="zoomFactor" @change="updateZoomFactor" class="scale-control">
        <option v-for="level in zoomLevels" :key="level" :value="level">
          {{ level.toFixed(1) }}
        </option>
      </select>
    </div>
  </div>
</template>

<script>
export default {
  emits: ["set-setting"],
  props: {
    primaryColor: String,
    accentColor: String,
    autoTheme: Boolean,
    initialZoomFactor: {
      type: Number,
      default: 1,
    },
  },
  data() {
    return {
      zoomFactor: this.initialZoomFactor,
      zoomLevels: [0.9, 1, 1.1, 1.2],
    };
  },
  methods: {
    updateZoomFactor() {
      window.ipcRenderer.send("set-zoom-factor", this.zoomFactor);
    },
  },
};
</script>

<style scoped>
.title {
  font-weight: bold;
}
.appearancecont {
  display: flex;
  accent-color: var(--dull-color);
}
.theme-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  width: 30%;
  line-height: 18px;
  padding-right: 5px;
  border-right: 2px solid var(--dull-color);
}
.accent-buttons {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
}
.belowAppearance {
  margin-left: 15px;
  margin-top: 8px;
}
input[type="checkbox"] {
  margin-right: 6px;
}
.colorindicator {
  transition: 0.3s;
  position: absolute;
  right: 0;
  top: 0;
  height: 100%;
  width: 8px;
  background-color: var(--accent-color);
  border-top-right-radius: 5px;
  border-bottom-right-radius: 5px;
}
.disabled {
  opacity: 0.3;
}
.scaling {
  padding: 4px;
}
.scale-control {
  background-color: white;
  border: none;
  border-radius: 4px;
}
</style>
