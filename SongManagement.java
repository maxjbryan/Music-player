// This class manages playlists and file management.
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SongManagement {

    // Store multiple playlists
    HashMap<String, ArrayList<Song>> playlists = new HashMap<>();
    HashMap<String, DefaultListModel<String>> playlistModels = new HashMap<>();

    // Currently active playlist
    String currentPlaylistName = null;
    DefaultListModel<String> currentPlaylistModel = new DefaultListModel<>();
    JList<String> displayedPlaylist = new JList<>(currentPlaylistModel);

    // Base music directory
    String musicDirectory = "/home/nyx/Documents/music/";

    /**
     * Scans the music directory for folders (playlists) and loads all .wav files
     */
    void scrapeAndADD() {
        File baseDir = new File(musicDirectory);

        if (!baseDir.exists() || !baseDir.isDirectory()) {
            System.err.println("Music directory does not exist: " + musicDirectory);
            return;
        }

        // Get all subdirectories (each represents a playlist)
        File[] playlistFolders = baseDir.listFiles(File::isDirectory);

        if (playlistFolders != null && playlistFolders.length > 0) {
            for (File playlistFolder : playlistFolders) {
                String playlistName = playlistFolder.getName();
                loadPlaylist(playlistName);
                System.out.println("Found playlist: " + playlistName);
            }

            // Set the first playlist as active by default
            if (!playlists.isEmpty()) {
                String firstPlaylist = playlists.keySet().iterator().next();
                switchToPlaylist(firstPlaylist);
            }
        } else {
            System.out.println("No playlist folders found in: " + musicDirectory);
        }
    }

    /**
     * Loads a specific playlist from a folder
     */
    void loadPlaylist(String playlistName) {
        File playlistFolder = new File(musicDirectory + playlistName);

        if (!playlistFolder.exists() || !playlistFolder.isDirectory()) {
            System.err.println("Playlist folder does not exist: " + playlistName);
            return;
        }

        // Get all .wav files in the playlist folder
        File[] wavFiles = playlistFolder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".wav"));

        ArrayList<Song> songs = new ArrayList<>();
        DefaultListModel<String> model = new DefaultListModel<>();

        if (wavFiles != null) {
            for (File file : wavFiles) {
                Song song = new Song();
                song.FilePath = file.getAbsolutePath();
                song.name = file.getName();
                song.playlistName = playlistName;

                songs.add(song);
                model.addElement(file.getName());

                System.out.println("  Found file: " + file.getName());
            }
        }

        playlists.put(playlistName, songs);
        playlistModels.put(playlistName, model);
    }

    /**
     * Switches the displayed playlist to a different one
     */
    void switchToPlaylist(String playlistName) {
        if (!playlists.containsKey(playlistName)) {
            System.err.println("Playlist does not exist: " + playlistName);
            return;
        }

        currentPlaylistName = playlistName;
        currentPlaylistModel = playlistModels.get(playlistName);
        displayedPlaylist.setModel(currentPlaylistModel);

        System.out.println("Switched to playlist: " + playlistName);
    }

    /**
     * Gets all available playlist names
     */
    ArrayList<String> getPlaylistNames() {
        return new ArrayList<>(playlists.keySet());
    }

    /**
     * Gets the currently active playlist name
     */
    String getCurrentPlaylistName() {
        return currentPlaylistName;
    }

    /**
     * Gets the full file path for a song in the current playlist
     */
    String getSongPath(String songName) {
        if (currentPlaylistName == null) {
            return null;
        }

        ArrayList<Song> songs = playlists.get(currentPlaylistName);
        if (songs != null) {
            for (Song song : songs) {
                if (song.name.equals(songName)) {
                    return song.FilePath;
                }
            }
        }

        return null;
    }

    /**
     * Reloads all playlists from disk
     */
    void refreshPlaylists() {
        playlists.clear();
        playlistModels.clear();
        currentPlaylistModel.clear();
        currentPlaylistName = null;
        scrapeAndADD();
    }
}