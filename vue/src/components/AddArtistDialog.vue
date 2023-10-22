<template>
    <div v-if="addDialogVis" class="wrapper">

        <div class="card-text">
            <p class="text--primary">Type artist's name and confirm.</p>
        </div>
    
        <div class="diag-actions">
            <input v-model="input" :class="{ 'invalid': !isValid }"/>
            <button @click="clickClose" class="imgbutton">
                <img v-if="primaryColor !== 'Light'" class="image" src="./icons/crossdark.png" alt="X"/>
                <img v-if="primaryColor === 'Light'" class="image" src="./icons/crosslight.png" alt="X"/>
            </button>
            <button @click="clickAdd" class="imgbutton" :disabled="!isValid">
                <img v-if="primaryColor !== 'Light'" class="image" src="./icons/confirmdark.png" alt="OK"/>
                <img v-if="primaryColor === 'Light'" class="image" src="./icons/confirmlight.png" alt="OK"/>
            </button>
        </div>

    </div>
</template>
  
<script>
import axios from 'axios';
import { mapState } from 'vuex';

export default {
    data: () => ({
        input: "",
        rules: [
        value => !!value.trim(),
        value => (value || '').length <= 25,
        ],
    }),
    methods: {
        //add artist to db
        clickAdd() {
            const artistname = encodeURIComponent(this.input);
            axios.post('http://localhost:8080/api/clickArtistAdd', artistname)
                .then(() => {
                    this.input = "";
                    this.$store.commit('SET_SELECTED_ARTIST', "");
                    this.$store.commit('SET_ADD_VIS', false);
                    this.$store.commit('SET_LOAD_REQUEST', true);
                })
                .catch(error => {
                    console.error(error);
                });
        },
        //close dialog
        clickClose() {
            this.$store.commit('SET_ADD_VIS', false);
        },
    },
    computed: {
        ...mapState([
            'addDialogVis',
            "primaryColor",
        ]),
        //if no rules in data broken, enable add button
        isValid() {
            return this.rules.every(rule => rule(this.input) === true);
        },
    },
}
</script>

<style scoped>
    .wrapper {
        top: 25%;
        left: 35%;
        width: 325px;
        height: 240px;
        position: absolute;
        z-index: 3;
        background-color: var(--primary-color);
        border: 1px solid var(--contrast-color);
        padding: 8px;
    }
    .diag-actions {
        position: absolute;
        bottom: 3px;
        left: 5px;
    }
    button {
        border: none;
    }
    input {
        background-color: var(--duller-color);
        color: var(--contrast-color);
        width: 200px;
        margin-right: 58px;
        margin-left: 10px;
        border: none;
    }
    input:focus {
        outline: none;
    }
    .imgbutton, .image {
        height: 23px;
        width: 23px;
        padding: 0;
        float: right;
        margin-left: 5px;
        margin-right: 2px;
        background-color: transparent;
    }
    .imgbutton {
        position: relative;
    }
    .imgbutton:hover {
        opacity: 50%;
    }
    :disabled {
        opacity: 0.5;
        pointer-events: none;
    }
</style>