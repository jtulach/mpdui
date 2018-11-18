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
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;
import org.junit.Test;

/** Tests for behavior of your application in isolation. Verify
 * behavior of your MVVC code in a unit test.
 */
@RunWith(BrowserRunner.class)
public class DataModelTest {
    @Test public void testUIModelWithoutUI() {
        Data model = new Data();
        model.setMessage("Hello World!");

        java.util.List<String> arr = model.getWords();
        assertEquals("Six words always", arr.size(), 6);
        assertEquals("Hello is the first word", "Hello", arr.get(0));
        assertEquals("World is the second word", "World!", arr.get(1));
    }
}
