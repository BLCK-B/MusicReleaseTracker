<template>

    <v-table
      fixed-header
      theme="light"
      v-if="hideTable"
    >
      <thead>
        <tr>
          <th class="song">song</th>
          <th v-if="!hideArtistColumn" class="artist">artist</th>
          <th class="date">date</th>
        </tr>
      </thead>

      <tbody>
        <tr v-for="(item, index) in tableData" :key="index">
          <td style="height:25px;">{{ item.song }}</td>
          <td v-if="!hideArtistColumn" style="height:25px;">{{ item.artist }}</td>
          <td style="height:25px;">{{ item.date }}</td>
        </tr>
      </tbody>
    </v-table>

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

.v-table {
  width: 75%;
  font-size: 14px;
}
.song{
  width: 50%;
}
.artist{
  width: 40%;
}
.date{
  width: 10%;
}

</style>