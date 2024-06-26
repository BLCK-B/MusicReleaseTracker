<template>
    <div class="preview">
        <h1>preview</h1>
        <p v-if="!hideTable">Confirm only if the table has usable information.</p>
        <p v-if="hideTable">Incorrect link.</p>
        <button @click="clickConfirm" class="imgbutton" :disabled="hideTable">
            <img v-if="primaryColor !== 'Light'" class="image" src="../icons/confirmdark.png" alt="OK"/>
            <img v-if="primaryColor === 'Light'" class="image" src="../icons/confirmlight.png" alt="OK"/>
        </button>
        <button @click="clickCancel" class="imgbutton">
            <img v-if="primaryColor !== 'Light'" class="image" src="../icons/crossdark.png" alt="X"/>
            <img v-if="primaryColor === 'Light'" class="image" src="../icons/crosslight.png" alt="X"/>
        </button>
    </div>
</template>

<script>
import axios from 'axios';
import { mapState } from 'vuex';

export default {
    computed: {
        ...mapState([
        'tableData',
        "primaryColor",
        ]),
        hideTable() {
            return this.tableData.length == 0;
        },
    },
    methods: {
        // close dialog, delete scraped preview from db
        clickCancel() {
            axios.request('/api/cleanArtistSource')
            .catch(error => {
                console.error(error);
            });
            this.$store.commit('SET_PREVIEW_VIS', false);
        },
        // close dialog, save url used for preview
        clickConfirm() {
            axios.request('/api/saveUrl')
            .catch(error => {
                console.error(error);
            });
            this.$store.commit('SET_PREVIEW_VIS', false);
        },
    }

};
</script>

<style scoped>
    .preview {
        width: 100%;
        height: 100%;
        background-color: var(--subtle-color);
        font-size: 15px;
    }
    h1 {
        font-size: 18px;
        color: var(--accent-color);
    }
    p, h1 {
        position: relative;
        left: 6px;
    }
    button {
        margin-left: 8px;
        border: none;
        border-radius: 5px;
        width: 60px;
    }
    .imgbutton, .image {
        margin-left: 22px;
        height: 26px;
        width: 26px;
        padding: 0;
        background-color: transparent;
    }
    .imgbutton:hover {
        opacity: 50%;
    }
    :disabled {
        opacity: 0.5;
        pointer-events: none;
    }
</style>