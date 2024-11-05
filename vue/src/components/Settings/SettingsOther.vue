<template>
  <p class="title">Other</p>
  <div class="flex-items">
    <div class="flex-padding">
      <input type="checkbox" :checked="isoDates" @change="$emit('set-setting', 'isoDates', $event.target.checked)" />
      <label>Dates as yyyy-MM-dd</label>
    </div>
  </div>
</template>

<script>
export default {
  emits: ["set-setting"],
  props: {
    isoDates: Boolean,
  },
};
</script>

<style scoped>
.title {
  font-weight: bold;
}
.flex-padding {
  padding: 5px;
}
input {
  margin-right: 5px;
}
</style>
