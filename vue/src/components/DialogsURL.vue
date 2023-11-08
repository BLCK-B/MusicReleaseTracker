<template>
    <div v-if="showUrlDiag && allowButtons && artist.length !== 0 && !addDialogVis">

        <div v-if="sourceTab === 'musicbrainz'" class="dialog">
            <h1>MusicBrainz source</h1>
            <p class="artist">{{ artist }}</p><br>
            <p><a href="https://musicbrainz.org" target="_blank">https://musicbrainz.org</a> > find artist > copy URL</p><br>
            <p>Example link:</p>
            <p>https://musicbrainz.org/artist/<span class="variabletext">id/...</span></p>
            <input v-model="input"/>
            <button @click="clickURL" class="imgbutton">
                <img v-if="primaryColor !== 'Light'" class="image" src="./icons/confirmdark.png" alt="OK"/>
                <img v-if="primaryColor === 'Light'" class="image" src="./icons/confirmlight.png" alt="OK"/>
            </button>
        </div>

        <div v-else-if="sourceTab === 'beatport'" class="dialog">
            <h1>Beatport source</h1>
            <p class="artist">{{ artist }}</p><br>
            <p><a href="https://beatport.com" target="_blank">https://beatport.com</a> > find artist > copy URL</p><br>
            <p>Example link:</p>
            <p>https://beatport.com/artist/<span class="variabletext">artistname/id/...</span></p>
            <input v-model="input"/>
            <button @click="clickURL" class="imgbutton">
                <img v-if="primaryColor !== 'Light'" class="image" src="./icons/confirmdark.png" alt="OK"/>
                <img v-if="primaryColor === 'Light'" class="image" src="./icons/confirmlight.png" alt="OK"/>
            </button>
        </div>

        <div v-else-if="sourceTab === 'junodownload'" class="dialog">
            <h1>Junodownload source</h1>
            <p class="artist">{{ artist }}</p><br>
            <p><a href="https://junodownload.com" target="_blank">https://junodownload.com</a> > find artist > copy URL</p><br>
            <p>Example link:</p>
            <p>https://junodownload.com/artists/<span class="variabletext">artistname/...</span></p>
            <input v-model="input"/>
            <button @click="clickURL" class="imgbutton">
                <img v-if="primaryColor !== 'Light'" class="image" src="./icons/confirmdark.png" alt="OK"/>
                <img v-if="primaryColor === 'Light'" class="image" src="./icons/confirmlight.png" alt="OK"/>
            </button>
        </div>

        <div v-else-if="sourceTab === 'youtube'" class="dialog">
            <h1>Youtube source</h1>
            <p class="artist">{{ artist }}</p><br>
            <p><a href="https://youtube.com" target="_blank">https://youtube.com</a></p>
            <p>
                Find an auto-generated channel with "Topic" in its name.
                <br>ID can be obtained in various ways.
                <br>For example: About tab > share > Copy channel ID
            </p>
            
            <p>Example ID of "Koven - Topic":</p>
            <p><span class="variabletext">UCWaKvFOf-a7vENyuEsZkNqg</span></p>
            <input v-model="input"/>
            <button @click="clickURL" class="imgbutton">
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
        showUrlDiag: false,
    }),
    computed: {
        ...mapState([
        "tableData",
        "sourceTab",
        "allowButtons",
        "artist",
        "addDialogVis",
        'primaryColor',
        ]),
    },
    methods: {
        //send input to be processed, displays preview dialog with scraped source table
        clickURL() {
            const url = encodeURIComponent(this.input);
            this.input = "";
            axios.post('http://localhost:8080/api/clickAddURL', url)
            .then(() => {
                const artist = this.artist;
                axios.post('http://localhost:8080/api/artistListClick', { artist })
                    .then(response => {
                        this.$store.commit('SET_TABLE_CONTENT', response.data);
                        this.$store.commit('SET_PREVIEW_VIS', true);
                    })
            })
            .catch(error => {
                console.error(error);
            });
        },
        //only show dialog when table is null, and if URL does not exist
        determineDiagShow() {
            if (this.tableData.length === 0) {
                axios.get("http://localhost:8080/api/checkExistURL")
                .then(response => {
                    if (response.data == true)
                        this.showUrlDiag = false;
                    else
                        this.showUrlDiag = true;
                })
                .catch(error => {
                    console.error(error);
                });
            }
            else
                this.showUrlDiag = false;
        },
    },
    watch: {
        //trigger url check on every tableData change
        tableData() {
            this.determineDiagShow();
        },
    },
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
        width: 345px;
        position: absolute;
        bottom: 5px;
        left: 5px;
        font-size: 13px;
        height: 18px;
    }
    input:focus {
        outline: none;
    }
    button {
        color: black;
        border: none;
        position: absolute;
        right: 5px;
        bottom: 5px;
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
    .imgbutton, .image {
        height: 23px;
        width: 23px;
        padding: 0;
        float: right;
        margin-right: 2px;
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