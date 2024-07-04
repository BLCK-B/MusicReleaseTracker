<template>
    <div v-if="addDialogVis" class="wrapper">
    
        <input v-model="input" :class="{ 'invalid': !isValid }" placeholder="Artist's name"/>

        <div class="buttons">
            <button @click="clickClose" class="imgbutton">
                <img v-if="primaryColor !== 'Light'" class="image" src="../icons/crossdark.png" alt="X"/>
                <img v-if="primaryColor === 'Light'" class="image" src="../icons/crosslight.png" alt="X"/>
            </button>
            <button @click="clickAdd" class="imgbutton" :disabled="!isValid">
                <img v-if="primaryColor !== 'Light'" class="image" src="../icons/confirmdark.png" alt="OK"/>
                <img v-if="primaryColor === 'Light'" class="image" src="../icons/confirmlight.png" alt="OK"/>
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
        //if isn't empty
        value => !!value.trim(),
        value => (value || '').length <= 25,
        ],
    }),
    computed: {
        ...mapState([
            'addDialogVis',
            "primaryColor",
        ]),
        // if no rules in data broken, enable add button
        isValid() {
            return this.rules.every(rule => rule(this.input) === true);
        },
    },
    methods: {
        // add artist to db
        clickAdd() {
            // it needs be encoded decoded trimmed ... because axios is changing symbols
            const artistname = encodeURIComponent(this.input);
            axios.post('/api/clickArtistAdd', artistname)
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
        // close dialog
        clickClose() {
            this.$store.commit('SET_ADD_VIS', false);
        },
    },
}
</script>

<style scoped>
    .wrapper {
        position: absolute;
        display: flex;
        gap: 4px;
        top: 3px;
        left: 2px;
        width: 290px;
        z-index: 4;
        height: 38px;
    }
    button {
        border: none;
    }
    input {
        position: relative;
        height: 26px;
        background-color: var(--duller-color);
        color: var(--contrast-color);
        width: 155px;
        border: 2px solid var(--contrast-color);
        border-radius: 4px;
        padding-left: 6px;
    }
    input:focus {
        outline: none;
    }
    .buttons {
        position: relative;
        height: 28px;
        background-color: var(--primary-color);
        border: 2px solid var(--contrast-color);
        border-radius: 4px;
        align-content: center;
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
        opacity: 0.3;
        pointer-events: none;
    }
</style>