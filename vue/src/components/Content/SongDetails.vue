<template>
  <Card :cardSize="'m'">
    <p>scrape link: {{ scrapeLink }}</p>
  </Card>
</template>

<script setup>
const scrapeLink = "url";

import Card from "../Util/Card.vue";
</script>
