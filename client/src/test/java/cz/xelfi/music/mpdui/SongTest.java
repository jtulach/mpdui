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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class SongTest {
    @Test
    public void computeFromFileName() {
        Song s = new Song().putFile("New/TatkoviHity/Disco/BETTY MIRANDA-Dance.mp3");
        assertEquals("Dance", s.getFullTitle());
        assertEquals("BETTY MIRANDA", s.getFullArtist());
        assertEquals(s.getFile(), s.getFullAlbum());
    }
}