<template>
    
    <p class="title">Appearance</p>
       <div class="appearancecont">
 
         <div class="theme-buttons">
           <input type="radio" :checked="primaryColor === 'Black'" @change="$emit('set-setting', 'theme', 'Black')" :disabled="autoTheme">
           <label :class="{ 'disabled': autoTheme }">Black</label>
           <input type="radio" :checked="primaryColor === 'Dark'" @change="$emit('set-setting', 'theme', 'Dark')" :disabled="autoTheme">
           <label :class="{ 'disabled': autoTheme }">Dark</label>
           <input type="radio" :checked="primaryColor === 'Light'" @change="$emit('set-setting', 'theme', 'Light')" :disabled="autoTheme">
           <label :class="{ 'disabled': autoTheme }">Light</label>
           
           <div class="colorindicator"></div>

         </div>
 
         <div class="accent-buttons">
            <input type="radio" :checked="accentColor === 'Cactus'" @change="$emit('set-setting', 'accent', 'Cactus')">
            <label>Cactus</label>
            <input type="radio" :checked="accentColor === 'Cloud'" @change="$emit('set-setting', 'accent', 'Cloud')">
            <label>Cloud</label>
            <input type="radio" :checked="accentColor === 'Lavender'" @change="$emit('set-setting', 'accent', 'Lavender')">
            <label>Lavender</label>
            <input type="radio" :checked="accentColor === 'Ocean'" @change="$emit('set-setting', 'accent', 'Ocean')">
            <label>Ocean</label>
            <input type="radio" :checked="accentColor === 'Rose'" @change="$emit('set-setting', 'accent', 'Rose')">
            <label>Rose</label>
            <input type="radio" :checked="accentColor === 'Warm'" @change="$emit('set-setting', 'accent', 'Warm')">
            <label>Warm</label>
         </div>
       </div>
 
       <div class="belowAppearance">
         <input type="checkbox" :checked="autoTheme" @change="$emit('set-setting', 'autoTheme', $event.target.checked)">
             <label>Match system theme</label>
       </div>

</template>

<script>
 export default {
  emits: ['set-setting'],
  props: {
    primaryColor: String,
    accentColor: String,
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
   margin-right: 10px;
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