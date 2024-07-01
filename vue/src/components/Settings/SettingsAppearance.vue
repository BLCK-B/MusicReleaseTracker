<template>
    
    <p class="title">Appearance</p>
       <div class="appearancecont">
 
         <div class="theme-buttons">
           <input type="radio" v-model="primaryColor" value="Black" @change="setSetting('theme', 'Black')" :disabled="autoTheme">
           <label :class="{ 'disabled': autoTheme }">Black</label>
           <input type="radio" v-model="primaryColor" value="Dark" @change="setSetting('theme', 'Dark')" :disabled="autoTheme">
           <label :class="{ 'disabled': autoTheme }">Dark</label>
           <input type="radio" v-model="primaryColor" value="Light" @change="setSetting('theme', 'Light')" :disabled="autoTheme">
           <label :class="{ 'disabled': autoTheme }">Light</label>
           
           <div class="colorindicator"></div>
         </div>
 
         <div class="accent-buttons">
           <input type="radio" v-model="accentColor" value="Lavender" @change="setSetting('accent', 'Lavender')">
           <label>Lavender</label>
           <input type="radio" v-model="accentColor" value="Cactus" @change="setSetting('accent', 'Cactus')">
           <label>Cactus</label>
           <input type="radio" v-model="accentColor" value="Rose" @change="setSetting('accent', 'Rose')">
           <label>Rose</label>
           <input type="radio" v-model="accentColor" value="Warm" @change="setSetting('accent', 'Warm')">
           <label>Warm</label>
           <input type="radio" v-model="accentColor" value="Cloud" @change="setSetting('accent', 'Cloud')">
           <label>Cloud</label>
           <input type="radio" v-model="accentColor" value="Ocean" @change="setSetting('accent', 'Ocean')">
           <label>Ocean</label>
         </div>
       </div>
 
       <div class="belowAppearance">
         <input type="checkbox" :checked="autoTheme" @change="setSetting('autoTheme', $event.target.checked)">
             <label>Match system theme</label>
       </div>

</template>

<script>
 import { mapState } from 'vuex';
 
 export default {
  computed: {
     ...mapState([
         'primaryColor',
         'accentColor',
     ]),
  },
  props: {
    autoTheme: Boolean,
  },
 };
 </script>



 <style scoped>
 * {
   transition: 0.1s;
 }
 .title {
   font-weight: bold;
 }
 .appearancecont {
   display:flex;
   accent-color: var(--dull-color);
 }
 .theme-buttons {
   display: grid;
   grid-template-columns: repeat(2, 1fr);
   width: 30%;
   padding-right: 20px;
   line-height: 18px;
 }
 .accent-buttons {
   display: grid;
   grid-template-columns: repeat(4, 1fr);
 }
 .belowAppearance {
   margin-left: 15px;
   margin-top: 6px;
 }
 input {
   margin-right: 5px;
 }
 .colorindicator {
   position: absolute;
   right: 0;
   top: 0;
   height: 100%;
   width: 8px;
   background-color: var(--accent-color);
   border-top-right-radius: 5px;
   border-bottom-right-radius: 5px;
 }
 .disabled {
   opacity: 0.3;
 }
 
 </style>