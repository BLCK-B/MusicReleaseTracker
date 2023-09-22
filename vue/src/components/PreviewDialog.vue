<template>
    <div class="preview">
            <p>PREVIEW TEXT</p>
            <button>YES</button>
            <button @click="clickCancel">NO</button>
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
    
    },
    methods: {
        clickCancel() {
            this.$store.commit('SET_PREVIEW_VIS', false);
            //clear contents of table
        }
    }

};
</script>

<style scoped>
    .preview {
        width: 100%;
        height: 100%;
        background-color: red;
    }
</style>