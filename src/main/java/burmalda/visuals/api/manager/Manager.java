package burmalda.visuals.api.manager;

import burmalda.visuals.api.manager.impl.ModuleManager;
import burmalda.visuals.api.manager.impl.ProfileManager;
import burmalda.visuals.util.client.ClientUtils;

public final class Manager {
    private final ProfileManager profileManager;
    private final ModuleManager moduleManager;

    public Manager() {
        this.profileManager = new ProfileManager();
        initRole();
        this.moduleManager = new ModuleManager();
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public ProfileManager getProfileManager() {
        return profileManager;
    }

    private void initRole() {
        if (ClientUtils.getUsername().contains("qcold")) {
            ProfileManager.setUsername("cool");
            ProfileManager.setUid(1);
            ProfileManager.setGroup(ProfileManager.Group.PREMIUM);
            ProfileManager.setRole(ProfileManager.Role.ADMIN);
            ProfileManager.setExpire(java.time.Instant.parse("2026-12-31T00:00:00Z"));
            return;
        }

        ProfileManager.setUsername("nocool");
        ProfileManager.setUid(java.util.concurrent.ThreadLocalRandom.current().nextInt(1, 100));
        ProfileManager.setGroup(ProfileManager.Group.DEFAULT);
        ProfileManager.setRole(ProfileManager.Role.USER);
        ProfileManager.setExpire(java.time.Instant.parse("2022-02-24T00:00:00Z"));
    }
}
