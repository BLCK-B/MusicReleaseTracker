<!-- the dialog will be rendered if the expression is true -->
<template>
    <div v-if="sourceTab === 'musicbrainz' && urlVisibility && allowButtons" class="dialog">
      <p>Insert sourcelink for musicbrainz.</p><br>
      <p>Visit <a href="https://musicbrainz.org">https://musicbrainz.org</a> > find artist > copy url</p><br>
      <p>Only correct links will be accepted.</p>
      <p>Example link:</p>
      <p>https://musicbrainz.org/artist/id/...</p>
        <input v-model="input" :class="{ 'invalid': !isValid }"/>
        <button @click="clickURL" :disabled="!isValid">insert</button>
    </div>
    <div v-else-if="sourceTab === 'beatport' && urlVisibility && allowButtons" class="dialog">
      <p>Insert source link for beatport.</p><br>
      <p>Visit <a href="https://beatport.com">https://beatport.com</a> > find artist > copy url</p><br>
      <p>Only correct links will be accepted.</p>
      <p>Example link:</p>
      <p>https://beatport.com/artist/artistname/id/...</p>
        <input v-model="input" :class="{ 'invalid': !isValid }"/>
        <button @click="clickURL" :disabled="!isValid">insert</button>
    </div>
    <div v-else-if="sourceTab === 'junodownload' && urlVisibility && allowButtons" class="dialog">
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
        'allowButtons',
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
        background-color: var(--primary-color);
        border: 1px solid var(--contrast-color);
        color: var(--contrast-color);
    }
    input {
        background-color: var(--duller-color);
        color: var(--contrast-color);
        border: none;
        width: 85%;
    }
    button {
        color: black;
        border: none;
        float: right;
    }
    button:hover {
        opacity: 70%;
    }
    a {
        color: var(--accent-color);
    }
</style>