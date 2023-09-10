<!-- the dialog will be rendered if the expression is true -->
<template>
    <div v-if="sourceTab === 'musicbrainz' && urlVisibility" class="dialog">
      <p>Insert source link for musicbrainz.</p><br>
      <p>Visit <a href="https://musicbrainz.org">https://musicbrainz.org</a> > find artist > copy url</p><br>
      <p>Only correct links will be accepted.</p>
      <p>Example link:</p>
      <p>https://musicbrainz.org/artist/id/...</p>
        <input v-model="input" :class="{ 'invalid': !isValid }"/>
        <button @click="clickURL" :disabled="!isValid">insert</button>
    </div>
    <div v-else-if="sourceTab === 'beatport' && urlVisibility" class="dialog">
      <p>Insert source link for beatport.</p><br>
      <p>Visit <a href="https://beatport.com">https://beatport.com</a> > find artist > copy url</p><br>
      <p>Only correct links will be accepted.</p>
      <p>Example link:</p>
      <p>https://beatport.com/artist/artistname/id/...</p>
        <input v-model="input" :class="{ 'invalid': !isValid }"/>
        <button @click="clickURL" :disabled="!isValid">insert</button>
    </div>
    <div v-else-if="sourceTab === 'junodownload' && urlVisibility" class="dialog">
      <p>Insert source link for junodownload.</p><br>
      <p>Visit <a href="https://junodownload.com">https://junodownload.com</a> > find artist > copy url</p><br>
      <p>Only correct links will be accepted.</p>
      <p>Example link:</p>
      <p>https://junodownload.com/artists/artistname/...</p>
        <input v-model="input" :class="{ 'invalid': !isValid }"/>
        <button @click="clickURL" :disabled="!isValid">insert</button>
    </div>
    
  </template>
  
<script>
import axios from 'axios';
import { mapState } from 'vuex';

export default {
    data: () => ({
        input: '',
        rules: [
        value => !!value.trim(),
        value => value.includes("musicbrainz.org/artist/") ||
        value.includes("beatport.com/artist/") ||
        value.includes("junodownload.com/artists/"),
        ],
    }),
    computed: {
        ...mapState([
        "tableData",
        "sourceTab",
        ]),
        urlVisibility() {
        const conditionMet = this.tableData.length === 0;
        return conditionMet;
        },
        isValid() {
            return this.rules.every(rule => rule(this.input) === true);
        },
    },
    methods: {
        clickURL() {
            const url = encodeURIComponent(this.input);
            axios.post('http://localhost:8080/api/clickAddURL', url)
            .then(() => {
                console.log("success");
            })
            .catch(error => {
                console.error(error);
            });
        },
    }

};
</script>

<style scoped>
    .dialog {
        width: 360px;
        background-color: #CCCCFF;
    }
    input {
        width: 82%;
    }
</style>