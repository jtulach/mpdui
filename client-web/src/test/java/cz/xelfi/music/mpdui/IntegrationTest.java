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

import net.java.html.junit.BrowserRunner;
import net.java.html.junit.HTMLContent;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for behavior of your application in real systems. The {@link BrowserRunner}
 * selects all possible presenters from your <code>pom.xml</code> and
 * runs the tests inside of them. By default there are two:
 * <ul>
 *   <li>JavaFX WebView presenter - verifies behavior in HotSpot JVM</li>
 *   <li>Bck2Brwsr presenter - runs the test in a pluginless browser</li>
 * </ul>
 *
 * See your <code>pom.xml</code> dependency section for details.
 */
@RunWith(BrowserRunner.class)
@HTMLContent(
    "<h3>Test in JavaFX WebView and pluginless Browser</h3>\n" +
    "<span data-bind='text: message'></span>\n" +
    "<ul data-bind='foreach: words'>\n" +
    "  <li>\n" +
    "    <span data-bind='text: $data'></span>\n" +
    "  </li>\n" +
    "</ul>\n" +
    "\n"
)
public class IntegrationTest {
    @Test public void testUIModelUI() {
        Data model = new Data();
        model.applyBindings();
        model.setMessage("Hello World by JavaFX WebView");

        java.util.List<String> arr = model.getWords();
        assertEquals("Six words always", arr.size(), 6);
        assertEquals("Hello is the first word", "Hello", arr.get(0));
        assertEquals("World is the second word", "World", arr.get(1));
        assertEquals("JavaFX", arr.get(3));

        model.setMessage("Hello World by Bck2Brwsr Virtual Machine");

        arr = model.getWords();
        assertEquals("Six words always", arr.size(), 6);
        assertEquals("Hello is the first word", "Hello", arr.get(0));
        assertEquals("World is the second word", "World", arr.get(1));
        assertEquals("Bck2Brwsr", arr.get(3));
    }
}
