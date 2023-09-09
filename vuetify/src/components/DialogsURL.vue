<!-- the dialog will be rendered if the expression is true -->
<template>
    <v-card v-if="sourceTab === 'musicbrainz' && urlVisibility" variant="tonal">
        <p>Insert source link for musicbrainz.</p><br>
        <p>Visit <a href="https://musicbrainz.org">https://musicbrainz.org</a> > find artist > copy url</p><br>
        <p>Only correct links will be accepted.</p>
        <p>Example link:</p>
        <p>https://musicbrainz.org/artist/id/...</p>
    </v-card>
    <v-card v-else-if="sourceTab === 'beatport' && urlVisibility" variant="tonal">
        <p>Insert source link for beatport.</p><br>
        <p>Visit <a href="https://beatport.com">https://beatport.com</a> > find artist > copy url</p><br>
        <p>Only correct links will be accepted.</p>
        <p>Example link:</p>
        <p>https://beatport.com/artist/artistname/id/...</p>
    </v-card>
    <v-card v-else-if="sourceTab === 'junodownload' && urlVisibility" variant="tonal">
        <p>Insert source link for junodownload.</p><br>
        <p>Visit <a href="https://junodownload.com">https://junodownload.com</a> > find artist > copy url</p><br>
        <p>Only correct links will be accepted.</p>
        <p>Example link:</p>
        <p>https://junodownload.com/artists/artistname/...</p>
    </v-card>
</template>
  
<script>
import { mapState } from 'vuex';

export default {
    
    computed: {
        ...mapState([
        "tableData",
        "sourceTab",
        ]),
        urlVisibility() {
        const conditionMet = this.tableData.length === 0;
        return conditionMet;
        },
    }

};
</script>

<style scoped>
/* Component-specific styles if needed */
</style>