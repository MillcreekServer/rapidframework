package io.github.wysohn.rapidframework3.testmodules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import io.github.wysohn.rapidframework3.interfaces.plugin.IGlobalPluginManager;

public class MockGlobalPluginManager extends AbstractModule {
    @Provides
    IGlobalPluginManager getGlobalPluginManager() {
        return new IGlobalPluginManager() {
            @Override
            public boolean isEnabled(String pluginName) {
                return true;
            }
        };
    }
}
