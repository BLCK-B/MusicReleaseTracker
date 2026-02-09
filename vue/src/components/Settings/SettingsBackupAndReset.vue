<template>
  <p class="title">Application data</p>
  <div class="horizontal-divs">
    <div class="vertical-buttons">
      <button @click="exportDB">
        Export DB
      </button>
      <button @click="isDragAndDropField = !isDragAndDropField">
        Import DB
      </button>
    </div>
    <div class="vertical-buttons" v-if="!isDragAndDropField">
      <button @click="resetSettings" @mouseleave="resetProtection" class="danger" testid="reset-settings-btn">
        {{ settingsProtection ? "Reset settings" : "Confirm" }}
      </button>
      <button @click="resetDB" @mouseleave="resetProtection" class="danger" testid="reset-db-btn">
        {{ dbProtection ? "Reset database" : "Confirm" }}
      </button>
    </div>
    <div class="dnd-box" v-else @dragover.prevent @drop.prevent="importDB">
      <span>{{ dndText }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import {ref} from "vue";
import axios from "axios";

const settingsProtection = ref(true);
const dbProtection = ref(true);
const isDragAndDropField = ref(false);
const dndText = ref("Drop file here");

const resetSettings = () => {
  if (settingsProtection.value === true) {
    settingsProtection.value = false;
  } else {
    axios.post("/api/resetSettings").catch((error) => {
      console.error(error);
    });
  }
};

const resetDB = () => {
  if (dbProtection.value === true) {
    dbProtection.value = false;
  } else {
    axios.post("/api/resetDB").catch((error) => {
      console.error(error);
    });
  }
};

const resetProtection = () => {
  settingsProtection.value = true;
  dbProtection.value = true;
};

const exportDB = async () => {
  const response = await axios.get("/api/getDBfile", {
    responseType: "blob"
  });
  const blob = new Blob([response.data], {
    type: "application/octet-stream",
  });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement("a");
  a.href = url;
  a.download = "musicdata.db";
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
};

const importDB = async (event: DragEvent) => {
  const files = event.dataTransfer?.files;
  if (!files || files.length === 0) return;

  const file = files[0];
  if (!file?.name.endsWith(".db")) {
    alert("Not a valid file");
    return;
  }

  try {
    const formData = new FormData();
    formData.append("file", file);
    await axios.post("/api/uploadDBfile", formData, {
      headers: {"Content-Type": "multipart/form-data"},
    });

    dndText.value = "✓";
    setTimeout(() => {
      dndText.value = "Drop file here";
      isDragAndDropField.value = false;
    }, 800);
  } catch (err) {
    alert("Failed to import file: " + err);
  }
};
</script>

<style scoped>
.title {
  font-weight: bold;
}

.horizontal-divs {
  display: flex;
  justify-content: center;
  gap: 2.4rem;
}

.vertical-buttons {
  display: flex;
  flex-direction: column;
  justify-content: space-evenly;
  gap: 0.6rem;
}

button {
  border-radius: 5px;
  background-color: var(--primary-color);
  border: 2px solid var(--primary-color);
  color: var(--contrast-color);
  padding: 4px;
  width: 120px;
  cursor: pointer;
}

.danger {
  background-color: transparent;
  border: 2px solid red;
}

button:hover {
  background-color: var(--subtle-color);
  border: 2px solid var(--subtle-color);
}

.danger:hover {
  background-color: red;
  border: 2px solid red;
}

.dnd-box {
  width: 116px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed var(--contrast-color);
  border-radius: 8px;
}

.dnd-box:hover {
  background-color: var(--subtle-color);
}
</style>
