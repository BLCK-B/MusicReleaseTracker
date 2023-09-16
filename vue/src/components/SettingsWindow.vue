<template>
 
 <div class="settings">

    <section class="typefilters">
      <p>Exclusion filters<br>Select types of songs to be hidden in Combined view.</p>

      <div class="toggle-buttons">
        <div class="grid-item">
          <label>Remix</label>
          <input type="checkbox" @change="updateFilter('Remix', $event.target.checked)">
        </div>
        <div class="grid-item">
          <label>VIP</label>
          <input type="checkbox" @change="updateFilter('VIP', $event.target.checked)">
        </div>
        <div class="grid-item">
          <label>Instrumental</label>
          <input type="checkbox" @change="updateFilter('Instrumental', $event.target.checked)">
        </div>
        <div class="grid-item">
          <label>Acoustic</label>
          <input type="checkbox" @change="updateFilter('Acoustic', $event.target.checked)">
        </div>
        <div class="grid-item">
          <label>Extended</label>
          <input type="checkbox" @change="updateFilter('Extended', $event.target.checked)">
        </div>
        <div class="grid-item">
          <label>Remaster</label>
          <input type="checkbox" @change="updateFilter('Remaster', $event.target.checked)">
        </div>
      </div>

    </section>

    <section class="cvlength">
      <p>Combined view table length</p>
      <div class="radio-buttons">
        <label>Short</label>
        <input type="radio" value="option1" v-model="radioValue">
        <label>Medium</label>
        <input type="radio" value="option2" v-model="radioValue">
        <label>Long</label>
        <input type="radio" value="option3" v-model="radioValue">
      </div>
    </section>

 </div>

</template>
  
<script>
import axios from 'axios';
import { mapState, mapActions } from 'vuex';

export default {
  data() {
    return {
       
    }
  },
  computed: {
    
  },
  methods: {
    updateFilter(filter, value) {
      console.log(filter, value);
      axios.post(`http://localhost:8080/api/toggleFilter?filter=${filter}&value=${value}`)
        .then({
          
        })
        .catch(error => {
          console.error(error);
        });
    },
  },
};
</script>


<style scoped>
* {
  font-family: 'arial', sans-serif;
  font-size: 14px;
  user-select: none;
}
.toggle-buttons {
  margin-top: 10px;
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-gap: 10px;
}

.radio-buttons {
  display: flex;
  flex-direction: column;
}
.radio-option {
  display: flex;
  align-items: center;
}

</style>