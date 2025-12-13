import globals from "globals";
import pluginJs from "@eslint/js";
import pluginVue from "eslint-plugin-vue";
import vueParser from "vue-eslint-parser";
import tsParser from "@typescript-eslint/parser";

export default [
    {files: ["**/*.{js,mjs,cjs,vue}"]},
    {languageOptions: { globals: globals.browser }},
    pluginJs.configs.recommended,
    ...pluginVue.configs["flat/essential"],
    {
        files: ["**/*.ts", "**/*.vue"],
        languageOptions: {
            parser: vueParser,
            parserOptions: {
                parser: tsParser,
                ecmaVersion: 2023,
                sourceType: "module",
                extraFileExtensions: [".vue"]
            }
        },
        rules: {
        }
    }
];