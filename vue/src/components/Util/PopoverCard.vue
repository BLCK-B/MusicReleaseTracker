<template>
  <div :class="['card', cardSize]">
    <slot></slot>
  </div>
</template>

<script setup lang="ts">
defineProps({
  cardSize: String,
});
</script>

<style scoped>
.card {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: var(--duller-color);
  border-radius: 5px;
  z-index: 1;
  padding: 16px;
  border: 2px solid var(--dull-color);
}

.s {
  width: 200px;
  height: 150px;
}

.m {
  width: 300px;
  height: 200px;
}

.l {
  width: 400px;
  height: 300px;
}
</style>
