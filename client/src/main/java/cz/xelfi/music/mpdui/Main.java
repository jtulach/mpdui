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

import cz.xelfi.music.mpdui.js.PlatformServices;
import java.util.prefs.Preferences;
import net.java.html.boot.BrowserBuilder;

public final class Main {
    private Main() {
    }

    public static void main(String... args) throws Exception {
        System.setProperty("com.dukescript.presenters.browserPort", "6680");
        System.setProperty("com.dukescript.presenters.browser", "NONE");
        BrowserBuilder.newBrowser().
            loadPage("pages/index.html").
            loadClass(Main.class).
            invoke("onPageLoad", args).
            showAndWait();
        System.in.read();
        System.exit(0);
    }

    /**
     * Called when the page is ready.
     */
    public static void onPageLoad(PlatformServices services) throws Exception {
        DataModel.onPageLoad(services);
    }

    public static void onPageLoad() throws Exception {
        // don't put "common" initialization stuff here, other platforms (iOS, Android, Bck2Brwsr) may not call this method. They rather call DataModel.onPageLoad
        DataModel.onPageLoad(new DesktopServices());
    }

    private static final class DesktopServices extends PlatformServices {
        @Override
        public String getPreferences(String key) {
            return Preferences.userNodeForPackage(Main.class).get(key, null);
        }

        @Override
        public void setPreferences(String key, String value) {
            Preferences.userNodeForPackage(Main.class).put(key, value);
        }
    }
}
