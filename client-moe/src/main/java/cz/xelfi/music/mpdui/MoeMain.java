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

import net.java.html.boot.BrowserBuilder;
import cz.xelfi.music.mpdui.js.PlatformServices;
import apple.foundation.NSUserDefaults;

public final class MoeMain {
    public static void main(String... args) throws Exception {
        BrowserBuilder.newBrowser().
            loadPage("pages/index.html").
            loadFinished(MoeMain::onPageLoad).
            showAndWait();
        System.exit(0);
    }

    public static void onPageLoad() {
        DataModel.onPageLoad(new MoeServices());
    }

    private static final class MoeServices extends PlatformServices {
        @Override
        public String getPreferences(String key) {
            return NSUserDefaults.standardUserDefaults().stringForKey(key);
        }

        @Override
        public void setPreferences(String key, String value) {
            NSUserDefaults.standardUserDefaults().setValueForKey(key, value);
        }
    }
}


