<template>
    
    <p class="title">Other</p>
       <div class="flex-items">
         <div class="flex-padding">
           <input type="checkbox" :checked="isoDates" @change="setSetting('isoDates', $event.target.checked)">
               <label>Dates in yyyy-MM-dd (ISO 8601)</label>
         </div>
       </div>

</template>

<script>
 import axios from 'axios';
 import { mapState, mapMutations } from 'vuex';
 
 export default {
  props: {
    isoDates: Boolean,
  },
 };
 </script>

<style scoped>
* {
  transition: 0.1s;
}
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
