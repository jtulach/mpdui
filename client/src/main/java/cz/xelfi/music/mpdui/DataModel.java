/**
 * MPD UI - UI for Music Protocol Daemon
 * Copyright © jaroslav.tulach@apidesign.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.xelfi.music.mpdui;

import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;
import net.java.html.json.ModelOperation;
import net.java.html.json.OnPropertyChange;
import cz.xelfi.music.mpdui.js.PlatformServices;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Models;
import org.bff.javampd.MPDException;
import org.bff.javampd.player.Player;
import org.bff.javampd.player.PlayerChangeEvent;
import org.bff.javampd.player.PlayerChangeListener;
import org.bff.javampd.player.VolumeChangeEvent;
import org.bff.javampd.player.VolumeChangeListener;
import org.bff.javampd.playlist.PlaylistDatabase;
import org.bff.javampd.server.MPD;
import org.bff.javampd.server.MPDConnectionException;
import org.bff.javampd.song.MPDSong;
import org.bff.javampd.song.SongDatabase;

@Model(className = "Data", targetId = "", instance = true, builder = "put", properties = {
    @Property(name = "message", type = String.class),
    @Property(name = "messageSelected", type = boolean.class),
    @Property(name = "connectionError", type = String.class),
    @Property(name = "tab", type = DataModel.Tab.class),
    @Property(name = "volume", type = int.class),
    @Property(name = "elapsed", type = int.class),
    @Property(name = "total", type = int.class),
    @Property(name = "playing", type = boolean.class),
    @Property(name = "host", type = String.class),
    @Property(name = "port", type = int.class),
    @Property(name = "currentSong", type = Song.class),
    @Property(name = "foundSongs", type = Song.class, array = true),
    @Property(name = "playlist", type = Song.class, array = true),
})
final class DataModel {
    enum Tab {
        MAIN,
        SEARCHLINE,
        SETTINGS,
        PLAYLIST,
    }

    private ConnectedToMpd connection;
    private PlatformServices services;
    private final Executor exec = Executors.newSingleThreadExecutor();
    private final RefreshOnBackground updates = new RefreshOnBackground();

    @ModelOperation
    void updateStatus(Data model) {
        withMpd(model, (server) -> {
            final Player player = server.getPlayer();
            MPDSong s = player.getCurrentSong();
            model.setVolume(player.getVolume());
            model.setElapsed((int) player.getElapsedTime());
            model.setTotal((int) player.getTotalTime());
            model.setPlaying(player.getStatus() == Player.Status.STATUS_PLAYING);
            model.getCurrentSong().read(s);
            List<Song> playing = convertSongs(server.getPlaylist().getSongList(), model.getCurrentSong(), 100);
            model.getPlaylist().clear();
            model.getPlaylist().addAll(playing);
        });
    }

    @ModelOperation
    void updateUI(Data model) {
        if (model.isPlaying()) {
            int e = model.getElapsed();
            if (e < model.getTotal()) {
                model.setElapsed(e + 1);
            } else {
                model.updateStatus();
            }
        }
    }

    @Function
    void addSong(Data model, Song data) {
        withMpd(model, (server) -> {
            model.getFoundSongs().remove(data);
            server.getPlaylist().addSong(data.getFile());
            model.updateStatus();
        });
    }

    @Function
    void removeSong(Data model, Song data) {
        withMpd(model, (server) -> {
            data.withSong(s -> {
                server.getPlaylist().removeSong(s);
            });
            model.updateStatus();
        });
    }

    @ComputedProperty
    static boolean searching(Tab tab) {
        return tab == Tab.SEARCHLINE;
    }

    @ComputedProperty
    static boolean listing(Tab tab) {
        return tab == Tab.PLAYLIST;
    }

    @ComputedProperty
    static boolean searchline(Tab tab) {
        return tab == Tab.SEARCHLINE;
    }

    @ComputedProperty
    static boolean controlling(Tab tab) {
        return tab == null || tab == Tab.MAIN;
    }

    @ComputedProperty
    static boolean settingup(Tab tab) {
        return tab == Tab.SETTINGS;
    }

    @ComputedProperty
    static String elapsedMinSec(int elapsed) {
        int min = elapsed / 60;
        int sec = elapsed % 60;
        String secStr = "00" + sec;
        secStr = secStr.substring(secStr.length() - 2);
        return min + ":" + secStr;
    }

    @Function
    static void doSettings(Data model) {
        model.setTab(Tab.SETTINGS);
    }

    @Function
    static void doSearch(Data model) {
        if (model.getTab() == Tab.SEARCHLINE) {
            model.setMessage("");
        }
        model.setTab(Tab.SEARCHLINE);
        model.setMessageSelected(true);
        model.updateStatus();
    }

    @Function
    static void doMain(Data model) {
        model.setTab(Tab.MAIN);
    }

    @Function
    static void doList(Data model) {
        model.setTab(Tab.PLAYLIST);
    }

    @Function
    void volumeUp(Data model) {
        volumeChange(model, Math.min(100, model.getVolume() + 10));
    }

    @Function
    void volumeDown(Data model) {
        volumeChange(model, Math.max(0, model.getVolume() - 10));
    }

    @OnPropertyChange("tab")
    void tabChange(Data model) {
        if (services != null) {
            services.setLocation("hash", "#" + model.getTab().toString());
        }
    }

    @OnPropertyChange("volume")
    void volumeChange(Data model) {
        volumeChange(model, model.getVolume());
    }

    private void volumeChange(Data model, int value) {
        withMpd(model, (server) -> {
            final Player player = server.getPlayer();
            if (player.getVolume() == value) {
                return;
            }
            final VolumeChangeListener onVolumeChange = new VolumeChangeListener() {
                @Override
                public void volumeChanged(VolumeChangeEvent event) {
                    player.removeVolumeChangedListener(this);
                    model.setVolume(event.getVolume());
                }
            };
            player.addVolumeChangeListener(onVolumeChange);
            player.setVolume(value);
        });
    }

    @OnPropertyChange("message")
    void search(Data model) {
        final String msg = model.getMessage();
        withMpd(model, (d) -> {
            Runnable r = () -> {
                final SongDatabase db = d.getMusicDatabase().getSongDatabase();
                final List<MPDSong> result = new ArrayList<>();
                result.addAll(db.searchAny(msg));
                PlaylistDatabase pdb = d.getMusicDatabase().getPlaylistDatabase();
                for (String list : pdb.listPlaylists()) {
                    if (list.contains(msg) || list.toLowerCase().contains(msg.toLowerCase())) {
                        try {
                            Collection<MPDSong> playList = pdb.listPlaylistSongs(list);
                            result.addAll(playList);
                        } catch (MPDException ex) {
                            // ignore
                        }
                    }
                }
                Collections.shuffle(result);
                model.applySongs(result);
            };
            exec.execute(r);
        });
    }

    @ModelOperation
    void applySongs(Data model, Collection<MPDSong> songs) {
        List<Song> arr = convertSongs(songs, model.getCurrentSong(), 100);
        model.getFoundSongs().clear();
        model.getFoundSongs().addAll(arr);
    }

    private static List<Song> convertSongs(final Collection<MPDSong> result, Song current, int maxItems) {
        List<Song> arr = Models.asList();
        for (MPDSong s : result) {
            Song n = new Song();
            n.read(s);
            if (n.equals(current)) {
                n.setImportant(true);
            }
            arr.add(n);
            if (arr.size() >= maxItems) {
                break;
            }
        }
        return arr;
    }

    @OnPropertyChange({ "host", "port" })
    void resetMpd(Data model) {
        ConnectedToMpd l = this.connection;
        if (l != null) {
            l.close();
        }
        this.connection = null;
        try {
            mpd(model);
            if (services != null) {
                services.setPreferences("host", model.getHost());
                services.setPreferences("port", "" + model.getPort());
            }
            model.updateStatus();
            model.setTab(Tab.MAIN);
        } catch (MPDConnectionException ex) {
            // OK, go on
        }
    }

    private void withMpd(Data model, With<MPD> operation) {
        try {
            operation.with(mpd(model));
        } catch (MPDConnectionException ex) {
            model.setConnectionError(ex.getLocalizedMessage());
        }
    }

    private MPD mpd(Data model) {
        if (connection == null) {
            MPD tmp;
            ConnectedToMpd tmpListener;
            try {
                final String host = model.getHost();
                final int port = model.getPort();
                if (host == null || host.isEmpty()) {
                    throw new MPDConnectionException("Specify host name or IP");
                }
                if (port < 0) {
                    throw new MPDConnectionException("Port must be a number, usually 6600");
                }
                tmp = new MPD.Builder()
                    .server(host)
                    .port(port)
                    .build();
                tmpListener = new ConnectedToMpd(tmp, model);
                tmp.getPlayer().addPlayerChangeListener(tmpListener);
            } catch (MPDConnectionException ex) {
                model.setConnectionError(ex.getMessage());
                model.setTab(Tab.SETTINGS);
                throw ex;
            }

            model.setConnectionError("OK");
            connection = tmpListener;

            updates.schedule(tmpListener, 1000);
        }
        return connection.mpd;
    }


    @Function void play(Data model) {
        withMpd(model, (server) -> {
            server.getPlayer().play();
        });
    }

    @Function
    void pause(final Data model) {
        withMpd(model, (server) -> {
            server.getPlayer().pause();
        });
    }

    @Function
    void playNext(final Data model) {
        withMpd(model, (server) -> {
            server.getPlayer().playNext();
        });
    }

    @Function
    void doRefresh(Data model) {
        model.setTab(Tab.MAIN);
        model.updateStatus();
    }

    @OnPropertyChange("message")
    void storeMessage(Data model) {
        final String msg = model.getMessage();
        if (services != null && !msg.contains("Screen size")) {
            services.setPreferences("message", msg);
        }
    }

    @ModelOperation
    void initServices(Data model, PlatformServices services) {
        this.services = services;
        String previousMessage = services.getPreferences("message");
        if (previousMessage != null) {
            model.setMessage(previousMessage);
        }
        String previousHost = services.getPreferences("host");
        if (previousHost != null) {
            model.setHost(previousHost);
        }
        String previousPort = services.getPreferences("port");
        if (previousPort != null) {
            try {
                int port = Integer.parseInt(previousPort);
                model.setPort(port);
            } catch (NumberFormatException ex) {
                // OK
            }
        }
    }
    /**
     * Called when the page is ready.
     */
    static void onPageLoad(PlatformServices services) {
        Data ui = new Data()
            .putPort(6600)
            .putHost("localhost");

        ui.initServices(services);

        String hash = services.getLocation("hash");
        if (hash.startsWith("#")) {
            hash = hash.substring(1);
            for (Tab t : Tab.values()) {
                if (hash.equals(t.toString())) {
                    ui.setTab(t);
                    break;
                }
            }
        }

        ui.applyBindings();
        ui.updateStatus();
    }

    @Model(builder = "put", className = "PlayerStatus", properties = {
        @Property(name = "currentSong", type = Song.class)
    })
    static final class ConnectedToMpd implements PlayerChangeListener, Callable<Boolean> {
        final Data model;
        final MPD mpd;

        ConnectedToMpd(MPD mpd, Data model) {
            this.model = model;
            this.mpd = mpd;
        }

        @Override
        public void playerChanged(PlayerChangeEvent event) {
            PlayerChangeEvent.Event ev = event.getEvent();
            if (mpd.isConnected()) {
                model.updateStatus();
            }
        }

        @Override
        public Boolean call() {
            if (mpd.isConnected()) {
                model.updateUI();
                return true;
            } else {
                return false;
            }
        }

        void close() {
            if (mpd.isConnected()) {
                mpd.close();
            }
        }
    }

    @Model(builder = "put", instance = true, className = "Song", properties = {
        @Property(name = "name", type = String.class),
        @Property(name = "title", type = String.class),
        @Property(name = "genre", type = String.class),
        @Property(name = "comment", type = String.class),
        @Property(name = "file", type = String.class),
        @Property(name = "year", type = String.class),
        @Property(name = "discNumber", type = String.class),
        @Property(name = "artistName", type = String.class),
        @Property(name = "albumName", type = String.class),
        @Property(name = "length", type = int.class),
        @Property(name = "track", type = int.class),
        @Property(name = "id", type = int.class),
        @Property(name = "position", type = int.class),
        @Property(name = "important", type = boolean.class),
    })
    static final class SongCntrl {
        private MPDSong song;

        @ComputedProperty
        static String fullName(String artistName, String name, String title, String file) {
            if (name != null && !name.isEmpty()) {
                if (artistName != null && !artistName.isEmpty()) {
                    return artistName + ":" + name;
                }
                return name;
            }
            if (title != null && !title.isEmpty()) {
                if (artistName != null && !artistName.isEmpty()) {
                    return artistName + ":" + title;
                }
                return title;
            }
            return file;
        }

        @ModelOperation
        void read(Song model, MPDSong s) {
            this.song = s;
            if (s == null) {
                return;
            }
            model
                .putName(s.getName())
                .putTitle(s.getTitle())
                .putGenre(s.getGenre())
                .putComment(s.getComment())
                .putFile(s.getFile())
                .putYear(s.getYear())
                .putDiscNumber(s.getDiscNumber())
                .putAlbumName(s.getAlbumName())
                .putArtistName(s.getArtistName())
                .putLength(s.getLength())
                .putTrack(s.getTrack())
                .putId(s.getId())
                .putPosition(s.getPosition());
        }

        @ModelOperation
        void withSong(Song model, WithMPDSong with) {
            if (song != null) {
                with.accept(song);
            }
        }
    }

    interface WithMPDSong {
        void accept(MPDSong s);
    }

    @FunctionalInterface
    private static interface With<M> {
        void with(M mpd);
    }
}
