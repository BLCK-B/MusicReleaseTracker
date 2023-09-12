<template>
  <div class="table-container">

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
          <tr v-for="(item, index) in tableData" :key="index">
            <td class="tdsong">{{ item.song }}</td>
            <td class="tdartist" v-if="!hideArtistColumn">{{ item.artist }}</td>
            <td class="tddate">{{ item.date }}</td>
          </tr>
        </tbody>
      </table>
    </div>
    
  </div>
</template>

<script>
import { mapState } from 'vuex';

export default {
  computed: {
    ...mapState([
      'tableData'
    ]),
    hideArtistColumn() {
      return this.tableData.some(item => item.artist === null);
    },
    hideTable() {
      const conditionMet = this.tableData.some(item => item.song !== null);
      if (!conditionMet) {
        this.emitNullEvent();
      }
      return conditionMet;
    },
  },
  methods: {
    emitNullEvent() {
      this.$emit('table-null');
    },
  },
};
</script>

<style scoped>

.table-container {
  height: 92%;
  overflow-y: scroll;
}
.table-header {
  flex-shrink: 0;
  overflow: hidden;
  background-color: white;
  z-index: 3;
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
  background-color: #f2f2f2;
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
</style>