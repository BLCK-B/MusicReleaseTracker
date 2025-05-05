<!--
  -         MusicReleaseTracker
  -         Copyright (C) 2023 - 2025 BLCK
  -         This program is free software: you can redistribute it and/or modify
  -         it under the terms of the GNU General Public License as published by
  -         the Free Software Foundation, either version 3 of the License, or
  -         (at your option) any later version.
  -         This program is distributed in the hope that it will be useful,
  -         but WITHOUT ANY WARRANTY; without even the implied warranty of
  -         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  -         GNU General Public License for more details.
  -         You should have received a copy of the GNU General Public License
  -         along with this program.  If not, see <https://www.gnu.org/licenses/>.
  -->

<template>
  <p class="title">Appearance</p>
  <div class="appearancecont">
    <div class="theme-buttons">
      <input
        :checked="primaryColor === 'black'"
        :disabled="autoTheme"
        type="radio"
        @change="$emit('set-setting', 'theme', 'black')" />
      <label :class="{ disabled: autoTheme }">Black</label>
      <input
        :checked="primaryColor === 'dark'"
        :disabled="autoTheme"
        type="radio"
        @change="$emit('set-setting', 'theme', 'dark')" />
      <label :class="{ disabled: autoTheme }">Dark</label>
      <input
        :checked="primaryColor === 'light'"
        :disabled="autoTheme"
        type="radio"
        @change="$emit('set-setting', 'theme', 'light')" />
      <label :class="{ disabled: autoTheme }">Light</label>

      <div class="colorindicator"></div>
    </div>

    <div class="accent-buttons">
      <input :checked="accentColor === 'cactus'" type="radio" @change="$emit('set-setting', 'accent', 'cactus')" />
      <label>Cactus</label>
      <input :checked="accentColor === 'cloud'" type="radio" @change="$emit('set-setting', 'accent', 'cloud')" />
      <label>Cloud</label>
      <input :checked="accentColor === 'lavender'" type="radio" @change="$emit('set-setting', 'accent', 'lavender')" />
      <label>Lavender</label>
      <input :checked="accentColor === 'ocean'" type="radio" @change="$emit('set-setting', 'accent', 'ocean')" />
      <label>Ocean</label>
      <input :checked="accentColor === 'rose'" type="radio" @change="$emit('set-setting', 'accent', 'rose')" />
      <label>Rose</label>
      <input :checked="accentColor === 'warm'" type="radio" @change="$emit('set-setting', 'accent', 'warm')" />
      <label>Warm</label>
    </div>
  </div>

  <div class="belowAppearance">
    <input :checked="autoTheme" type="checkbox" @change="$emit('set-setting', 'autoTheme', $event.target.checked)" />
    <label>Match system theme</label>
  </div>
</template>

<script setup>
defineProps({
  primaryColor: String,
  accentColor: String,
  autoTheme: Boolean,
});
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
</style>
