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
import org.bff.javampd.server.MPD;
/** Model annotation generates class Data with
 * one message property, boolean property and read only words property
 */
@Model(className = "Data", targetId = "", instance = true, properties = {
    @Property(name = "message", type = String.class),
    @Property(name = "rotating", type = boolean.class)
})
final class DataModel {
    private PlatformServices services;

    @ComputedProperty static java.util.List<String> words(String message) {
        String[] arr = new String[6];
        String[] words = message == null ? new String[0] : message.split(" ", 6);
        for (int i = 0; i < 6; i++) {
            arr[i] = words.length > i ? words[i] : "!";
        }
        return java.util.Arrays.asList(arr);
    }

    @Function static void turnAnimationOn(Data model) {
        MPD mpd = new MPD.Builder()
        .server("bigmac")
        .build();
        
        mpd.getPlayer().play();
    }

    @Function
    void turnAnimationOff(final Data model) {
        services.confirmByUser("Really turn off?", new Runnable() {
            @Override
            public void run() {
                model.setRotating(false);
            }
        });
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
        Data ui = new Data();
        ui.setMessage("Hello World from HTML and Java!");
        ui.initServices(services);
        ui.applyBindings();
    }
}
