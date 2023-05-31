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
package cz.xelfi.music.mpdui.js;

import net.java.html.js.JavaScriptBody;

/** Use {@link JavaScriptBody} annotation on methods to
 * directly interact with JavaScript. See
 * http://bits.netbeans.org/html+java/1.3/net/java/html/js/package-summary.html
 * to understand how.
 */
public class PlatformServices {
    /**
     * Reads a value from a persistent storage.
     * @param key the identification for the value
     * @return the value or <code>null</code> if not found
     */
    public String getPreferences(String key) {
        return getPreferencesImpl(key);
    }

    /**
     * Puts a value into the persitent storage.
     * @param key the identification for the value
     * @param value the value to store
     */
    public void setPreferences(String key, String value) {
        setPreferencesImpl(key, value);
    }

    /**
     * Obtains size of the screen.
     * @return array with two numbers: width and height
     */
    public int[] getScreenSize() {
        Object[] size = screenSizeImpl();
        return new int[] {
            ((Number)size[0]).intValue(),
            ((Number)size[1]).intValue(),
        };
    }

    public String getLocation(String attr) {
        return getLocationImpl(attr);
    }

    public void setLocation(String attr, String value) {
        setLocationImpl(attr, value);
    }

    @JavaScriptBody(args = { "key" }, body = """
        if (!window.localStorage) return null;
        return window.localStorage.getItem(key);
        """
    )
    private static native String getPreferencesImpl(String key);

    @JavaScriptBody(args = { "key", "value" }, body = """
        if (!window.localStorage) return;
        window.localStorage.setItem(key, value);
        """
    )
    private static native void setPreferencesImpl(String key, String value);

    /**
     * Shows confirmation dialog to the user.
     *
     * @param msg the message
     * @param callback called back when the use accepts (can be null)
     */
    @JavaScriptBody(
            args = {"msg", "callback"},
            javacall = true,
            body = """
            if (confirm(msg)) {
                callback.@java.lang.Runnable::run()();
            }
            """
    )
    static native void confirmImpl(String msg, Runnable callback);

    @JavaScriptBody(args = {}, body = """
        var w = window,
            d = document,
            e = d.documentElement,
            g = d.getElementsByTagName('body')[0],
            x = w.innerWidth || e.clientWidth || g.clientWidth,
            y = w.innerHeight|| e.clientHeight|| g.clientHeight;

        return [x, y];
        """
    )
    static native Object[] screenSizeImpl();

    @JavaScriptBody(args = { "attr" }, body = """
        var w = window || document;
        return w.location[attr];
        """
    )
    static native String getLocationImpl(String attr);

    @JavaScriptBody(args = { "attr", "value" }, body = """
        var w = window || document;
        w.location[attr] = value;
        """
    )
    static native void setLocationImpl(String attr, String value);


}
