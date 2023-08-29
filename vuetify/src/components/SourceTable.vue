<template>
  <div>

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
        <tr v-for="(item, index) in TableData" :key="index">
          <td style="height:25px;">{{ item.song }}</td>
          <td v-if="!hideArtistColumn" style="height:25px;">{{ item.artist }}</td>
          <td style="height:25px;">{{ item.date }}</td>
        </tr>
      </tbody>
    </v-table>

  </div>
</template>

<script>
export default {
  props: ['TableData'],
  computed: {
    hideArtistColumn() {
        //hide artists if column is null
        return this.TableData.some(item => item.artist === null);
    },
    hideTable() {
        //hide table if null
        const conditionMet = this.TableData.some(item => item.song !== null);
        if (!conditionMet) {
          this.emitTableNullEvent(); // Trigger the method when condition is met
        }
        return conditionMet;
    },
  },
  methods: {
    emitTableNullEvent() {
      this.$emit('table-null');
    },
  },
};
</script>

<style scoped>
.v-table {
  width: 90%;
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