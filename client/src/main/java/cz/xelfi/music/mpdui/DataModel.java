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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import net.java.html.json.ComputedProperty;
import net.java.html.json.Models;
import org.bff.javampd.player.PlayerChangeEvent;
import org.bff.javampd.player.PlayerChangeListener;
import org.bff.javampd.playlist.PlaylistDatabase;
import org.bff.javampd.server.MPD;
import org.bff.javampd.song.MPDSong;
import org.bff.javampd.song.SongDatabase;

@Model(className = "Data", targetId = "", instance = true, builder = "put", properties = {
    @Property(name = "message", type = String.class),
    @Property(name = "host", type = String.class),
    @Property(name = "currentSong", type = Song.class),
    @Property(name = "foundSongs", type = Song.class, array = true),
    @Property(name = "playlist", type = Song.class, array = true),
})
final class DataModel {
    private Listener listener;
    private PlatformServices services;
    private MPD mpd;
    private Executor exec;

    @ModelOperation
    void updateStatus(Data model) {
        final MPD server = mpd(model);
        {
            MPDSong s = server.getPlayer().getCurrentSong();
            model.getCurrentSong().read(s);
        }
        {
            List<Song> playing = convertSongs(server.getPlaylist().getSongList());
            model.getPlaylist().clear();
            model.getPlaylist().addAll(playing);
        }
    }

    @Function
    void addSong(Data model, Song data) {
        mpd(model).getPlaylist().addSong(data.getFile());
        model.updateStatus();
    }

    @Function
    void removeSong(Data model, Song data) {
        data.withSong(s -> {
            mpd(model).getPlaylist().removeSong(s);
        });
        model.updateStatus();
    }

    @ComputedProperty
    static boolean searching(String message) {
        return message != null && message.length() >= 3;
    }

    @ComputedProperty
    static boolean playing(String message) {
        return !searching(message);
    }

    @OnPropertyChange("message")
    void search(Data model) {
        final String msg = model.getMessage();
        if (!searching(msg)) {
            return;
        }
        final MPD d = mpd(model);
        exec.execute(() -> {
            final SongDatabase db = d.getMusicDatabase().getSongDatabase();
            final Collection<MPDSong> result = db.searchAny(msg);
            PlaylistDatabase pdb = d.getMusicDatabase().getPlaylistDatabase();
            for (String list : pdb.listPlaylists()) {
                if (list.contains(msg)) {
                    Collection<MPDSong> playList = pdb.listPlaylistSongs(list);
                    result.addAll(playList);
                }
            }
            model.applySongs(result);
        });
    }

    @ModelOperation
    void applySongs(Data model, Collection<MPDSong> songs) {
        List<Song> arr = convertSongs(songs);
        model.getFoundSongs().clear();
        model.getFoundSongs().addAll(arr);
    }

    private static List<Song> convertSongs(final Collection<MPDSong> result) {
        List<Song> arr = Models.asList();
        for (MPDSong s : result) {
            Song n = new Song();
            n.read(s);
            arr.add(n);
        }
        return arr;
    }

    private MPD mpd(Data model) {
        if (mpd == null) {
            mpd = new MPD.Builder()
                .server(model.getHost())
                .build();
            mpd.getPlayer().addPlayerChangeListener(listener);
            exec = Executors.newSingleThreadExecutor();
        }
        return mpd;
    }


    @Function void play(Data model) {
        mpd(model).getPlayer().play();
    }

    @Function
    void pause(final Data model) {
        mpd(model).getPlayer().pause();
    }

    @Function
    void playNext(final Data model) {
        mpd(model).getPlayer().playNext();
    }

    @Function
    void refresh(Data model) {
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
        this.listener = new Listener(model);
        this.services = services;
        String previousMessage = services.getPreferences("message");
        if (previousMessage != null) {
            model.setMessage(previousMessage);
        }
    }
    /**
     * Called when the page is ready.
     */
    static void onPageLoad(PlatformServices services) {
        Data ui = new Data()
            .putHost("bigmac");

        ui.initServices(services);
        ui.applyBindings();
        ui.updateStatus();
    }

    @Model(builder = "put", className = "PlayerStatus", properties = {
        @Property(name = "currentSong", type = Song.class)
    })
    static final class Listener implements PlayerChangeListener {
        final Data model;

        Listener(Data model) {
            this.model = model;
        }

        @Override
        public void playerChanged(PlayerChangeEvent event) {
            PlayerChangeEvent.Event ev = event.getEvent();
            System.err.println("ev: " + ev);
            model.updateStatus();
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
}
