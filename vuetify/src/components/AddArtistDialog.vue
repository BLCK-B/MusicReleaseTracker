<template>
    <v-card v-if="addDialogVis"
        class="mx-auto"
    >
    
    <v-card-text>
      <p class="text--primary">Type artist's name and confirm.</p>
    </v-card-text>

    <v-text-field v-model="input" :rules="rules"></v-text-field>

    <v-card-actions>
      <v-btn @click="clickAdd()" :disabled="!isValid" variant="text">insert</v-btn>
    </v-card-actions>

  </v-card>
</template>

<script>
import axios from 'axios';
import { mapMutations, mapActions, mapState } from 'vuex';

export default {
    data: () => ({
        input: '',
        rules: [
        value => !!value.trim(),
        value => (value || '').length <= 30 || 'Too long.',
        ],
    }),
    methods: {
        ...mapMutations(['SET_SELECTED_ARTIST', 'SET_ADD_VIS']),
        clickAdd() {
        axios.post('http://localhost:8080/api/clickArtistAdd', this.input)
            .then(() => {
            this.$store.commit('SET_SELECTED_ARTIST', "");
            this.$store.commit('SET_ADD_VIS', false);
            })
            .catch(error => {
            console.error(error);
            });
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

.v-card {
    top: 130px;
    left: 280px;
    width: 300px;
    height: 230px;
    position: absolute;
    z-index: 3;
}
.v-card-text {
    height: 100px;
}

</style>