<template>
    <div class="progress-container" v-if="!allowButtons">
      <div class="progressbar" :style="{ width: `${progress}%` }"></div>
    </div>
  </template>
  
<script>
import { mapState, mapMutations } from 'vuex';

export default {
    computed: {
        ...mapState([
            'progress',
            'allowButtons',
        ]),
    },
    methods: {
    ...mapMutations(['setProgress']),
  },
}
</script>

<style scoped>
.progress-container {
    background-color: grey;
}
.progressbar {
    height: 5px;
    line-height: 5px;
    text-align: center;
    background-color: red;
}
</style>