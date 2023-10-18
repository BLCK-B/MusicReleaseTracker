<template>
  <div class="table-container" v-if="hideTable">

    <div class="table-header">
      <table>
          <tr>
            <th class="song">song</th>
            <th v-if="!hideArtistColumn" class="artist">artist</th>
            <th class="date">date</th>
          </tr>
      </table>
    </div>

    <div class="table-body">
      <table>
        <tbody>
          <td><br></td>
          <tr v-for="(item, index) in tableData" :key="index" :class="{'future-date': isDateInFuture(item.date)}">
            <td class="tdsong">{{ item.song }}</td>
            <td class="tdartist" v-if="!hideArtistColumn">{{ item.artist }}</td>
            <td class="tddate">{{ item.date }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    
  </div>

  <div class="emptynotice" v-if="!hideTable && !previewVis && artist">
    <p>table empty, but URL exists</p>
  </div>
  
</template>

<script>
import { mapState } from 'vuex';

export default {
  mounted() {
    this.isDateInFuture;
  },
  computed: {
    ...mapState([
      'tableData',
      "previewVis",
      "artist",
    ]),
    hideArtistColumn() {
      return this.tableData.some(item => item.artist === null);
    },
    hideTable() {
      return this.tableData.some(item => item.song !== null);
    },
  },
  methods: {
    //get current date
    isDateInFuture(dateString) {
      const date = new Date(dateString);
      return date > new Date();
    },
  },
};
</script>

<style scoped>
.table-header {
  flex-shrink: 0;
  overflow: hidden;
  z-index: 3;
  position: fixed;
  width: calc(100% - 170px);
}
.table-body {
  flex-grow: 1;
  overflow-y: auto;
  user-select: text;
  margin-bottom: 10vh;
}
table {
  width: 100%;
  min-width: 500px;
  border-collapse: collapse;
}
th, td {
  padding: 4px;
}
th {
  background-color: var(--primary-color);
  border: none;
  position: sticky;
  top: 0;
}
.song, .tdsong {
  width: 50%;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.artist, .tdartist {
  width: 50%;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.date, .tddate {
  width: 100px;
  min-width: 100px;
}
.future-date {
  opacity: 40%;
}
.emptynotice {
  position: absolute;
  left: 45%;
  top: 50%;
  color: var(--dull-color);
}
</style>