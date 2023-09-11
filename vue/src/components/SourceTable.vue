<template>
  <div v-if="hideTable" class="table-container">

    <table>
      <thead>
        <tr>
          <th class="song">song</th>
          <th v-if="!hideArtistColumn" class="artist">artist</th>
          <th class="date">date</th>
        </tr>
      </thead>
    </table>

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
.table-container{
  height: 90vh;
  padding-left: 5px;
}
.table-body {
  height: 100%;
  overflow-y: auto;
  user-select: text;
}

table {
  width: 100%;
  min-width: 500px;
}
th {
  padding-bottom: 6px;
  border-bottom: solid 2px #CCCCFF;
}
tr {
  height: 14px;
}

.song, .tdsong{
  width: 45%;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.artist, .tdartist{
  width: 37%;
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.date, .tddate{
  width: 18%;
}

</style>