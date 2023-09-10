<template>
    <div v-if="addDialogVis" class="wrapper">

      <div class="card-text">
        <p class="text--primary">Type artist's name and confirm.</p>
      </div>
  
        <div class="diag-actions">
            <input v-model="input" :class="{ 'invalid': !isValid }" />
            <button @click="clickAdd" :disabled="!isValid">insert</button>
            <button @click="clickClose">cancel</button>
        </div>

    </div>
  </template>
  
<script>
import axios from 'axios';
import { mapMutations, mapActions, mapState } from 'vuex';

export default {
    data: () => ({
        input: '',
        rules: [
        value => !!value.trim(),
        value => (value || '').length <= 30,
        ],
    }),
    methods: {
        clickAdd() {
        axios.post('http://localhost:8080/api/clickArtistAdd', this.input)
            .then(() => {
                this.$store.commit('SET_SELECTED_ARTIST', "");
                this.$store.commit('SET_ADD_VIS', false);
                this.$store.commit('SET_LOAD_REQUEST', true);
            })
            .catch(error => {
                console.error(error);
            });
        },
        clickClose() {
            this.$store.commit('SET_ADD_VIS', false);
        },
    },
    computed: {
        ...mapState(['addDialogVis']),
        isValid() {
            return this.rules.every(rule => rule(this.input) === true);
        },
    },
}
</script>

<style scoped>

.wrapper {
    top: 180px;
    left: 260px;
    width: 300px;
    height: 230px;
    position: absolute;
    z-index: 3;
    background-color: grey;
}
.diag-actions {
    position: relative;
    top: 155px;
    left: 5px;
}

</style>