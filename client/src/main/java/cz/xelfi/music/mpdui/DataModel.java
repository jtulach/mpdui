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

import net.java.html.json.ComputedProperty;
import net.java.html.json.Function;
import net.java.html.json.Model;
import net.java.html.json.Property;
import net.java.html.json.ModelOperation;
import net.java.html.json.OnPropertyChange;
import cz.xelfi.music.mpdui.js.PlatformServices;
import org.bff.javampd.player.PlayerChangeEvent;
import org.bff.javampd.player.PlayerChangeListener;
import org.bff.javampd.server.MPD;
import org.bff.javampd.song.MPDSong;
/** Model annotation generates class Data with
 * one message property, boolean property and read only words property
 */
@Model(className = "Data", targetId = "", instance = true, builder = "put", properties = {
    @Property(name = "message", type = String.class),
    @Property(name = "host", type = String.class),
    @Property(name = "currentSong", type = Song.class),
    @Property(name = "rotating", type = boolean.class)
})
final class DataModel {
    private Listener listener;
    private PlatformServices services;
    private MPD mpd;

    @ModelOperation
    void updateStatus(Data model) {
        MPDSong s = mpd(model).getPlayer().getCurrentSong();
        model.getCurrentSong().read(s);
    }

    @ComputedProperty static java.util.List<String> words(String message) {
        String[] arr = new String[6];
        String[] words = message == null ? new String[0] : message.split(" ", 6);
        for (int i = 0; i < 6; i++) {
            arr[i] = words.length > i ? words[i] : "!";
        }
        return java.util.Arrays.asList(arr);
    }

    @Function void turnAnimationOn(Data model) {
        mpd(model).getPlayer().play();
    }

    private MPD mpd(Data model) {
        if (mpd == null) {
            mpd = new MPD.Builder()
                .server(model.getHost())
                .build();
            mpd.getPlayer().addPlayerChangeListener(listener);
        }
        return mpd;
    }

    @Function
    void turnAnimationOff(final Data model) {
        mpd(model).getPlayer().pause();
    }

    @Function static void rotate5s(final Data model) {
        model.setRotating(true);
        java.util.Timer timer = new java.util.Timer("Rotates a while");
        timer.schedule(new java.util.TimerTask() {
            @Override
            public void run() {
                model.setRotating(false);
            }
        }, 5000);
    }

    @Function
    void showScreenSize(Data model) {
        int[] widthHeight = services.getScreenSize();
        model.setMessage("Screen size is " + widthHeight[0] + " x " + widthHeight[1]);
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

        ui.setMessage("Hello World from HTML and Java!");
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

    @Model(builder = "put", className = "Song", properties = {
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
        @ModelOperation
        static void read(Song model, MPDSong s) {
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
    }
}