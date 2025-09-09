export type mediaItemType = {
    // always
    date: string;
    artists: string[];
    // single
    name: string;
    // album
    album?: string;
    songs: string[];
    // custom
    thumbnailUrl?: string;
};