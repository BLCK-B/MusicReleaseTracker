<template>
    <div class="preview">
        <h1>preview</h1>
        <p>Confirm only if the table has complete, recent information.</p>
        <button @click="clickConfirm" :disabled="hideTable">confirm</button>
        <button @click="clickCancel">cancel</button>
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
        value => value.includes("musicbrainz.org/artist/") ||
        value.includes("beatport.com/artist/") ||
        value.includes("junodownload.com/artists/"),
        ],
    }),
    computed: {
        ...mapState([
        'tableData'
        ]),
        hideTable() {
            return this.tableData.length == 0;
        },
    },
    methods: {
        //close dialog, delete scraped preview from db
        clickCancel() {
            axios.request('http://localhost:8080/api/cleanArtistSource')
            .catch(error => {
                console.error(error);
            });
            this.$store.commit('SET_PREVIEW_VIS', false);
        },
        //close dialog, save url used for preview
        clickConfirm() {
            axios.request('http://localhost:8080/api/saveUrl')
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
        padding: 8px;
        width: 100%;
        height: 100%;
        background-color: var(--subtle-color);
        font-size: 15px;
    }
    h1 {
        font-size: 18px;
        color: var(--accent-color);
    }
    button {
        margin-left: 8px;
        border: none;
        border-radius: 5px;
        width: 60px;
    }
</style>