// Manages UI elements.

import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;

class PlayerUI {

    //class objs
    AudioController ac = new AudioController();  // audio controller
    SongManagement sm = new SongManagement();  // playlist and song manager

    JFrame mainFrame = new JFrame("Max's Music Player");

    // Custom JDesktopPane with background image
    JDesktopPane desktop = new JDesktopPane() {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (BackgroundPanel != null) {
                g.drawImage(BackgroundPanel, 0, 0, getWidth(), getHeight(), this);
            }
        }
    };

    Image BackgroundPanel;

    //menu bar and items
    JMenuBar topMenuBar = new JMenuBar();
    JMenu playlists = new JMenu("Playlists");
    JMenu help = new JMenu("Help");

    JButton exit = new JButton("X");

    //Internal frames
    JInternalFrame playlistFrame = new JInternalFrame("Playlist", true, false, true, true);

    // Bottom control panel (fixed, not internal frame)
    JPanel controlPanel = new JPanel();

    //volume
    JSlider volumeBar = new JSlider(0,100);
    JLabel volumeText = new JLabel("50%");

    //start/stop
    JButton startAndStop = new JButton("▶");

    // Previous and Next buttons
    JButton previousButton = new JButton("⏮");
    JButton nextButton = new JButton("⏭");

    //now playing label
    JLabel nowPlayingLabel = new JLabel("No song playing");

    // Progress bar for scrubbing
    JSlider progressBar = new JSlider(0, 100);
    JLabel currentTimeLabel = new JLabel("0:00");
    JLabel totalTimeLabel = new JLabel("0:00");

    // Timer for updating progress bar
    Timer progressTimer;

    //playlists
    JScrollPane playlistDisplayScrollPane = new JScrollPane(sm.displayedPlaylist);

    void main(String[] args) {
        setUi();
        sm.scrapeAndADD();

        // Populate playlist menu with discovered playlists
        populatePlaylistMenu();

        try {
            BackgroundPanel = ImageIO.read(new File("/home/nyx/Documents/music/MusicPlayerBackground.png"));
            desktop.repaint(); // Trigger repaint after loading image
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    void setUi(){
        frameSetup();
        configItems();
        addActionListeners();
        additems();
    }

    void frameSetup() {
        // Dark mode UI Manager settings for internal frames
        UIManager.put("InternalFrame.activeTitleBackground", new Color(45, 45, 48));
        UIManager.put("InternalFrame.activeTitleForeground", new Color(220, 220, 220));
        UIManager.put("InternalFrame.inactiveTitleBackground", new Color(35, 35, 38));
        UIManager.put("InternalFrame.inactiveTitleForeground", new Color(150, 150, 150));
        UIManager.put("InternalFrame.borderColor", new Color(60, 60, 63));

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setSize(screenSize.width-300, screenSize.height-100);

        // Create main container with BorderLayout
        JPanel mainContainer = new JPanel(new BorderLayout());
        mainContainer.add(desktop, BorderLayout.CENTER);
        mainContainer.add(controlPanel, BorderLayout.SOUTH);

        mainFrame.setContentPane(mainContainer);
        mainFrame.setJMenuBar(topMenuBar);

        // Dark mode menu bar
        topMenuBar.setBackground(new Color(30, 30, 30));
        topMenuBar.setForeground(new Color(220, 220, 220));
        topMenuBar.setBorderPainted(false);

        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        desktop.setBackground(new Color(25, 25, 28));
    }

    void configItems() {
        // Style exit button for dark mode
        exit.setFont(new Font("Default", Font.PLAIN, 14));
        exit.setBackground(new Color(45, 45, 48));
        exit.setForeground(new Color(220, 220, 220));
        exit.setFocusPainted(false);
        exit.setBorderPainted(false);

        volumeBar.setValue(50);
        volumeText.setFont(new Font("Default", Font.PLAIN, 11));

        // Configure the JList with dark mode
        sm.displayedPlaylist.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sm.displayedPlaylist.setVisibleRowCount(15);
        sm.displayedPlaylist.setFont(new Font("Default", Font.PLAIN, 14));
        sm.displayedPlaylist.setBackground(new Color(30, 30, 33));
        sm.displayedPlaylist.setForeground(new Color(220, 220, 220));
        sm.displayedPlaylist.setSelectionBackground(new Color(70, 130, 180));
        sm.displayedPlaylist.setSelectionForeground(Color.WHITE);

        // Style the scroll pane
        playlistDisplayScrollPane.getViewport().setBackground(new Color(30, 30, 33));
        playlistDisplayScrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Configure playlist internal frame with dark mode
        playlistFrame.setSize(400, 500);
        playlistFrame.setLocation(50, 50);
        playlistFrame.setLayout(new BorderLayout());
        playlistFrame.add(playlistDisplayScrollPane, BorderLayout.CENTER);

        // Dark mode colors for playlist frame
        playlistFrame.getContentPane().setBackground(new Color(30, 30, 33));
        playlistFrame.setFrameIcon(null);

        playlistFrame.setVisible(true);

        // Configure bottom control panel (Spotify-style)
        controlPanel.setLayout(new BorderLayout());
        controlPanel.setBackground(new Color(24, 24, 27));
        controlPanel.setPreferredSize(new Dimension(0, 100));
        controlPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 63)));

        // Left section - Now Playing
        JPanel leftSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftSection.setBackground(new Color(24, 24, 27));
        nowPlayingLabel.setFont(new Font("Default", Font.BOLD, 13));
        nowPlayingLabel.setForeground(new Color(220, 220, 220));
        leftSection.add(nowPlayingLabel);

        // Center section - Playback controls
        JPanel centerSection = new JPanel();
        centerSection.setLayout(new BoxLayout(centerSection, BoxLayout.Y_AXIS));
        centerSection.setBackground(new Color(24, 24, 27));

        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonsPanel.setBackground(new Color(24, 24, 27));

        // Style buttons
        styleControlButton(previousButton);
        styleControlButton(startAndStop);
        styleControlButton(nextButton);

        startAndStop.setPreferredSize(new Dimension(50, 45));
        startAndStop.setFont(new Font("Default", Font.PLAIN, 14));

        buttonsPanel.add(previousButton);
        buttonsPanel.add(startAndStop);
        buttonsPanel.add(nextButton);

        // Progress section
        JPanel progressPanel = new JPanel(new BorderLayout(5, 0));
        progressPanel.setBackground(new Color(24, 24, 27));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 5, 20));

        currentTimeLabel.setForeground(new Color(180, 180, 180));
        currentTimeLabel.setFont(new Font("Default", Font.PLAIN, 11));
        totalTimeLabel.setForeground(new Color(180, 180, 180));
        totalTimeLabel.setFont(new Font("Default", Font.PLAIN, 11));

        progressBar.setValue(0);
        progressBar.setBackground(new Color(24, 24, 27));
        progressBar.setForeground(new Color(70, 130, 180));
        progressBar.setPaintTicks(false);
        progressBar.setPaintLabels(false);

        progressPanel.add(currentTimeLabel, BorderLayout.WEST);
        progressPanel.add(progressBar, BorderLayout.CENTER);
        progressPanel.add(totalTimeLabel, BorderLayout.EAST);

        centerSection.add(buttonsPanel);
        centerSection.add(progressPanel);

        // Right section - Volume control
        JPanel rightSection = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 35));
        rightSection.setBackground(new Color(24, 24, 27));

        JLabel volumeIcon = new JLabel("🔊");
        volumeIcon.setFont(new Font("Default", Font.PLAIN, 16));

        volumeBar.setPreferredSize(new Dimension(120, 25));
        volumeBar.setBackground(new Color(24, 24, 27));
        volumeBar.setForeground(new Color(70, 130, 180));

        volumeText.setForeground(new Color(180, 180, 180));

        rightSection.add(volumeIcon);
        rightSection.add(volumeBar);
        rightSection.add(volumeText);

        controlPanel.add(leftSection, BorderLayout.WEST);
        controlPanel.add(centerSection, BorderLayout.CENTER);
        controlPanel.add(rightSection, BorderLayout.EAST);

        // Initialize progress timer
        progressTimer = new Timer(100, e -> updateProgressBar());
    }

    void styleControlButton(JButton button) {
        button.setBackground(new Color(24, 24, 27));
        button.setForeground(new Color(220, 220, 220));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(50, 40));
        button.setFont(new Font("Default", Font.PLAIN, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    void updateProgressBar() {
        if (ac.getcurrentClip() != null && ac.getcurrentClip().isRunning()) {
            long currentPos = ac.getcurrentClip().getMicrosecondPosition();
            long totalLength = ac.getcurrentClip().getMicrosecondLength();

            if (totalLength > 0) {
                int progress = (int) ((currentPos * 100) / totalLength);
                progressBar.setValue(progress);

                currentTimeLabel.setText(formatTime(currentPos / 1000000));
                totalTimeLabel.setText(formatTime(totalLength / 1000000));
            }
        }
    }

    String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    void addActionListeners() {
        exit.addActionListener(e -> {
            if (ac.getcurrentClip() != null) {
                ac.getcurrentClip().close();
            }
            if (progressTimer != null) {
                progressTimer.stop();
            }
            mainFrame.dispose();
        });

        volumeBar.addChangeListener(e -> {
            volumeText.setText(volumeBar.getValue() + "%");
            float volumeChange = (((float) volumeBar.getValue()*2.0f)/100f);
            ac.setVolume(volumeChange);
        });

        startAndStop.addActionListener(e -> {
            if (ac.clipPaused == true) {
                ac.resume();
                System.out.println("RESUMING");
                startAndStop.setText("⏸");
                progressTimer.start();
            }
            else if (ac.clipPaused == false) {
                ac.pause();
                System.out.println("PAUSING");
                startAndStop.setText("▶");
                progressTimer.stop();
            }
        });

        previousButton.addActionListener(e -> {
            playPreviousSong();
        });

        nextButton.addActionListener(e -> {
            playNextSong();
        });

        // Progress bar scrubbing
        progressBar.addChangeListener(e -> {
            if (progressBar.getValueIsAdjusting() && ac.getcurrentClip() != null) {
                long totalLength = ac.getcurrentClip().getMicrosecondLength();
                long newPosition = (totalLength * progressBar.getValue()) / 100;
                ac.setPosition(newPosition);
            }
        });

        // Add listener for playlist selection
        sm.displayedPlaylist.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = sm.displayedPlaylist.getSelectedValue();
                if (selected != null) {
                    System.out.println("Selected: " + selected);

                    // Update now playing label
                    nowPlayingLabel.setText("♪ " + selected.replace(".wav", ""));

                    // Get the full path from SongManagement
                    String fullPath = sm.getSongPath(selected);

                    if (fullPath != null) {
                        // Stop previous track
                        if (ac.getcurrentClip() != null) {
                            ac.getcurrentClip().close();
                        }

                        // Load and play the selected track on a separate thread
                        new Thread(() -> {
                            ac.playSound(fullPath);
                            startAndStop.setText("⏸");
                            progressTimer.start();
                        }).start();
                    } else {
                        System.err.println("Could not find path for: " + selected);
                    }
                }
            }
        });
    }

    void playPreviousSong() {
        int currentIndex = sm.displayedPlaylist.getSelectedIndex();
        if (currentIndex > 0) {
            sm.displayedPlaylist.setSelectedIndex(currentIndex - 1);
        }
    }

    void playNextSong() {
        int currentIndex = sm.displayedPlaylist.getSelectedIndex();
        if (currentIndex < sm.displayedPlaylist.getModel().getSize() - 1) {
            sm.displayedPlaylist.setSelectedIndex(currentIndex + 1);
        }
    }

    /**
     * Populates the Playlists menu with items for each discovered playlist folder
     */
    void populatePlaylistMenu() {
        playlists.removeAll(); // Clear existing items

        ArrayList<String> playlistNames = sm.getPlaylistNames();

        if (playlistNames.isEmpty()) {
            JMenuItem noPlaylists = new JMenuItem("No playlists found");
            noPlaylists.setEnabled(false);
            noPlaylists.setBackground(new Color(40, 40, 43));
            noPlaylists.setForeground(new Color(150, 150, 150));
            playlists.add(noPlaylists);
        } else {
            for (String playlistName : playlistNames) {
                JMenuItem playlistItem = new JMenuItem(playlistName);

                // Style the menu item
                playlistItem.setBackground(new Color(40, 40, 43));
                playlistItem.setForeground(new Color(220, 220, 220));

                // Add action listener to switch playlists
                playlistItem.addActionListener(e -> {
                    sm.switchToPlaylist(playlistName);
                    playlistFrame.setTitle("Playlist - " + playlistName);
                    nowPlayingLabel.setText("No song playing");
                });

                playlists.add(playlistItem);
            }
        }

        // Add separator and refresh option
        playlists.addSeparator();
        JMenuItem refreshItem = new JMenuItem("Refresh Playlists");
        refreshItem.setBackground(new Color(40, 40, 43));
        refreshItem.setForeground(new Color(220, 220, 220));
        refreshItem.addActionListener(e -> {
            sm.refreshPlaylists();
            populatePlaylistMenu();
        });
        playlists.add(refreshItem);
    }

    void additems() {
        // Style menu items for dark mode
        playlists.setForeground(new Color(220, 220, 220));
        help.setForeground(new Color(220, 220, 220));

        topMenuBar.add(playlists);
        topMenuBar.add(help);
        topMenuBar.add(Box.createHorizontalGlue());
        topMenuBar.add(exit);

        // Add internal frames to desktop
        desktop.add(playlistFrame);

        // Bring frames to front
        try {
            playlistFrame.setSelected(true);
        } catch (java.beans.PropertyVetoException ex) {
            ex.printStackTrace();
        }
    }
}