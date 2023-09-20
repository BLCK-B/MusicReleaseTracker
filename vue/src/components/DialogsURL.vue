<!-- the dialog will be rendered if the expression is true -->
<template>
    <div v-if="sourceTab === 'musicbrainz' && urlVisibility && allowButtons && artist.length !== 0" class="dialog">
      <h1>MusicBrainz source</h1>
      <p class="artist">{{ artist }}</p><br>
      <p><a href="https://musicbrainz.org">https://musicbrainz.org</a> > find artist > copy url</p>
      <p>Only correct links will be accepted.</p><br>
      <p>Example link:</p>
      <p>https://musicbrainz.org/artist/<span class="variabletext">id/...</span></p>
        <input v-model="input" :class="{ 'invalid': !isValid }"/>
        <button @click="clickURL" :disabled="!isValid">insert</button>
    </div>
    <div v-else-if="sourceTab === 'beatport' && urlVisibility && allowButtons && artist.length !== 0" class="dialog">
        <h1>Beatport source</h1>
        <p class="artist">{{ artist }}</p><br>
      <p><a href="https://beatport.com">https://beatport.com</a> > find artist > copy url</p>
      <p>Only correct links will be accepted.</p><br>
      <p>Example link:</p>
      <p>https://beatport.com/artist/<span class="variabletext">artistname/id/...</span></p>
        <input v-model="input" :class="{ 'invalid': !isValid }"/>
        <button @click="clickURL" :disabled="!isValid">insert</button>
    </div>
    <div v-else-if="sourceTab === 'junodownload' && urlVisibility && allowButtons && artist.length !== 0" class="dialog">
        <h1>Junodownload source</h1>
        <p class="artist">{{ artist }}</p><br>
      <p><a href="https://junodownload.com">https://junodownload.com</a> > find artist > copy url</p>
      <p>Only correct links will be accepted.</p><br>
      <p>Example link:</p>
      <p>https://junodownload.com/artists/<span class="variabletext">artistname/...</span></p>
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
        "allowButtons",
        "artist",
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
        width: 375px;
        height: 280px;
        background-color: var(--primary-color);
        border: 1px solid var(--contrast-color);
        color: var(--contrast-color);
        padding: 8px;
    }
    input {
        background-color: var(--duller-color);
        color: var(--contrast-color);
        border: none;
        width: 328px;
        position: absolute;
        bottom: 3px;
        left: 5px;
    }
    input:focus {
        outline: none;
    }
    button {
        color: black;
        border: none;
        position: absolute;
        right: 5px;
        bottom: 3px;
    }
    button:hover {
        opacity: 70%;
    }
    a {
        color: var(--accent-color);
        font-weight: bold;
        text-decoration: none;
    }
    a:hover {
        text-decoration: underline;
    }
    h1 {
        font-size: 17px;
        font-weight: normal;
        
    }
    .variabletext {
        color: var(--accent-color);
    }
    .artist {
        user-select: text;
    }
    
</style>