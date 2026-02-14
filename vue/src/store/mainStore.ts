import {defineStore} from "pinia";
import type {MediaItemType} from "@/types/MediaItemType.ts";
import type {AccentColors, PrimaryThemes} from "@/types/Theming.ts";
import type {SongType} from "@/types/SongType.ts";
import type {WebSource} from "@/types/Sources.ts";

export const useMainStore = defineStore("main", {
    state: () => ({
        tableData: [] as MediaItemType[],
        selectedSongDetails: null as SongType | null,
        sourceTab: "combview" as WebSource,
        primaryColor: "" as PrimaryThemes,
        accentColor: "" as AccentColors,
        selectedArtist: "" as string,
        progress: 0 as number,
        loadListRequest: false as boolean,
        allowButtons: true as boolean,
        previewVis: false as boolean,
        isoDates: false as boolean,
        urlExists: false as boolean,
        sourcesWithUrls: [] as WebSource[],
    }),
    actions: {
        setSourceTab(sourceTab: WebSource) {
            this.sourceTab = sourceTab;
        },
        setSelectedArtist(selectedArtist: string) {
            this.selectedArtist = selectedArtist;
        },
        setTableContent(tableData: any[]) {
            this.tableData = tableData;
        },
        setLoadRequest(loadListRequest: boolean) {
            this.loadListRequest = loadListRequest;
        },
        setAllowButtons(allowButtons: boolean) {
            this.allowButtons = allowButtons;
        },
        setProgress(progress: number) {
            this.progress = progress * 100;
        },
        setPrimaryColor(color: PrimaryThemes) {
            this.primaryColor = color;
        },
        setAccentColor(color: AccentColors) {
            this.accentColor = color;
        },
        setPreviewVis(previewVis: boolean) {
            this.previewVis = previewVis;
        },
        setIsoDates(isoDates: boolean) {
            this.isoDates = isoDates;
        },
        setUrlExists(urlExists: boolean) {
            this.urlExists = urlExists;
        },
        setSelectedSongDetails(selected: any) {
            this.selectedSongDetails = selected;
        },
        setSourcesWithUrls(sourcesWithUrls: WebSource[]) {
            this.sourcesWithUrls = sourcesWithUrls;
        },
    },
});