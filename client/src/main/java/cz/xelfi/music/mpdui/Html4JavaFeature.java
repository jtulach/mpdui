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

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

/** Helper class to help `native-image` tool from GraalVM discover all necessary
 * elements.
 */
public final class Html4JavaFeature implements Feature {
    private final Set<Type> alreadyProcessed = new HashSet<>();

    @Override
    public void duringSetup(DuringSetupAccess access) {
        access.registerObjectReplacer((obj) -> {
            if (obj instanceof Type type) {
                var str = type.toString();
                if (str.startsWith("class ")) {
                    var typeName = str.substring(6);
                    if (typeName.endsWith("$JsCallbacks$") && alreadyProcessed.add(type)) {
                        var clazz = access.findClassByName(typeName);
                        for (var m : clazz.getMethods()) {
                            if (m.getDeclaringClass() == clazz) {
                                RuntimeReflection.register(m);
                            }
                        }
                        alreadyProcessed.add(type);
                    }
                }
            }
            return obj;
        });
    }

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
    }
}
