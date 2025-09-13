import type {SongType} from "@/types/SongType.ts";

export type MediaItemType = {
    songs: SongType[];
    album?: string;
};