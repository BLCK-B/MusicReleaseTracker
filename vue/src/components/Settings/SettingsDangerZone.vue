<template>
    
    <p class="title">Danger zone</p>
       <div class="dangercont">
 
         <button v-if="settingsProtection" @click="resetSettings()">Reset settings</button>
         <button v-if="!settingsProtection" @click="resetSettings()" @mouseleave="resetProtection()">confirm</button>
        
         <button v-if="dbProtection" @click="resetDB()">Reset database</button>
         <button v-if="!dbProtection" @click="resetDB()" @mouseleave="resetProtection()">confirm</button>
          
       </div>

</template>

<script>
 import axios from 'axios';
 import { mapState, mapMutations } from 'vuex';
 
 export default {
  data() {
    return {
      settingsProtection: true,
      dbProtection: true,
    }
  },
  methods: {
    // default the settings
    resetSettings() {
      console.log("refistedterd");
      if (this.settingsProtection == true) {
        this.settingsProtection = false;
      }
      else {
        axios.post('/api/resetSettings')
        .then(() => {
          this.clickClose();
        })
        .catch(error => {
            console.error(error);
        });
      }
    },
    // default database
    resetDB() {
      if (this.dbProtection == true) {
        this.dbProtection = false;
      }
      else {
        axios.post('/api/resetDB')
        .then(() => {
          this.clickClose();
        })
        .catch(error => {
            console.error(error);
        });
      }
    },
    resetProtection() {
      this.settingsProtection = true;
      this.dbProtection = true;
    }
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
.dangercont {
  display: flex;
  justify-content: space-evenly;
}
.danger button {
  border-radius: 5px;
  background-color: transparent;
  color: var(--contrast-color);
  border: 2px solid red;
  padding: 4px;
  width: 120px;
}
.danger button:hover {
  background-color: red;
}

.disabled {
  opacity: 0.3;
}

</style>
