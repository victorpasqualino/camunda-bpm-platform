/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.application.impl;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationScriptEngineResolver {

  protected Map<String, ScriptEngine> cachedEngines = new HashMap<String, ScriptEngine>();

  protected ScriptEngineManager scriptEngineManager;

  public ProcessApplicationScriptEngineResolver(ClassLoader classLoader) {
    scriptEngineManager = new ScriptEngineManager(classLoader);
  }

  public ScriptEngine getScriptEngine(String name, boolean cache) {
    ScriptEngine scriptEngine = null;

    if (cache) {
      scriptEngine = getCachedScriptEngine(name);

    }
    else {
      scriptEngine = scriptEngineManager.getEngineByName(name);

    }

    return scriptEngine;
  }

  protected ScriptEngine getCachedScriptEngine(String name) {
    ScriptEngine scriptEngine = cachedEngines.get(name);

    if(scriptEngine == null) {
      scriptEngine = scriptEngineManager.getEngineByName(name);

      if(scriptEngine != null) {
        if(isCachable(scriptEngine)) {
          cachedEngines.put(name, scriptEngine);
        }
      }
    }

    return scriptEngine;
  }

  protected boolean isCachable(ScriptEngine scriptEngine) {
    // Check if script-engine supports multithreading. If true it can be cached.
    Object threadingParameter = scriptEngine.getFactory().getParameter("THREADING");
    return threadingParameter != null;
  }

}
