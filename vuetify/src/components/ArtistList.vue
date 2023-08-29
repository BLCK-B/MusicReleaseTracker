<template>
  <v-card class="mx-auto" max-width="300">
    <v-list>
      <!-- Loop through the array and display each artist as a clickable list item -->
      <v-btn v-for="item in artistsArrayList" :key="item" @mousedown="handleItemClick(item)" :class="{'highlighted': item === lastClickedItem}" text>
        <v-list-item>
          <v-list-item-title>{{ item }}</v-list-item-title>
        </v-list-item>
      </v-btn>
    </v-list>
  </v-card>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      artistsArrayList: [],
      lastClickedItem: null,
    };
  },
  created() {
    this.loadList();
  },
  methods: {
    loadList() {
      axios.get('http://localhost:8080/api/loadList')
        .then(response => {
          this.artistsArrayList = response.data;
        })
        .catch(error => {
          console.error(error);
        });
    },
    handleItemClick(artist) {
      this.lastClickedItem = artist;
      axios.post('http://localhost:8080/api/artistListClick', { artist })
        .then(response => {
          this.$emit('update-table', response.data);
        })
        .catch(error => {
          console.error(error);
        });
    },
  },
};
</script>

<style scoped>
.v-btn {
  display: block;
  width: 100%;
  margin-top: 3px;
}
.v-card {
  margin-top: 4px;
}
.v-row {
  margin-top: 1px;
  margin-bottom: 0px;
}

.highlighted {
  background-color: #CCCCFF;
}

.v-list-item-title {
  font-size: 14px;
}

</style>