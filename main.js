import * as DS from "./dataStore.js";
import { createApp } from "https://unpkg.com/vue@3/dist/vue.esm-browser.js";
import App from "./app.js";

let units = [];

async function init() {
  const res = await fetch("./units.json");
  units = await res.json();

  // global machen, damit App zugreifen kann
  window.units = units;

  DS.loadData();
  createApp(App).mount("#app");
}

init();

// Chrome und Firefox unterstützen "Assert" nicht, deswegen eine neue function