<template>
    <div class="progress-container">
      <div class="progressbar" :style="{ width: `${progress}%` }"></div>
    </div>
</template>
  
<script>
import { mapState, mapMutations } from 'vuex';

export default {
    computed: {
        ...mapState([
            'progress',
        ]),
    },
    methods: {
        ...mapMutations(['setProgress']),
    },
}
</script>

<style scoped>
* {
  transition: 0.2s;
}
.progress-container {
  background-color: transparent;
}
.progressbar {
  height: 5px;
  line-height: 5px;
  text-align: center;
  background-color: var(--accent-color);
}
</style>