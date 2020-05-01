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

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;

final class RefreshOnBackground {
    private final Timer timer = new Timer("Background MPD UI Tasks");
    
    void schedule(Callable<Boolean> r, long period) {
        TimerTask task = new RunnableTask(r);
        timer.scheduleAtFixedRate(task, period, period);
    }
    
    private final static class RunnableTask extends TimerTask {
        private final Callable<Boolean> run;

        RunnableTask(Callable<Boolean> r) {
            this.run = r;
        }

        @Override
        public void run() {
            try {
                final Boolean ok = run.call();
                if (Boolean.FALSE.equals(ok)) {
                    cancel();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
