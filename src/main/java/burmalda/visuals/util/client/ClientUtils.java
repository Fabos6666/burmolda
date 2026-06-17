package burmalda.visuals.util.client;

import net.minecraft.client.network.PlayerListEntry;
import burmalda.visuals.util.minecraft.IMinecraft;

import javax.swing.*;

public final class ClientUtils implements IMinecraft {

    public static String getUsername() {
        String username = System.getProperty("user.name");
        if (username == null || username.isEmpty()) {
            username = System.getenv("USERNAME");
            if (username == null || username.isEmpty()) {
                username = System.getenv("USER");
            }
        }
        return username;
    }

    public static String getServerName() {
        if (mc.isInSingleplayer()) {
            return "singleplayer";
        }

        if (mc.getCurrentServerEntry() != null && mc.getCurrentServerEntry().address != null) {
            return mc.getCurrentServerEntry().address;
        }

        return "unknown";
    }

    public static float getTps() {
        if (mc.world != null) {
            return mc.world.getTickManager().getTickRate();
        }

        return 20.0f;
    }

    public static int getPing() {
        if (mc.player == null || mc.getNetworkHandler() == null) {
            return 0;
        }

        PlayerListEntry playerListEntry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
        return playerListEntry != null ? Math.max(playerListEntry.getLatency(), 0) : 0;
    }

    public static void crash() {
        System.exit(228);
    }

    public static void push(String title, String text) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(
                        null,
                        text,
                        title,
                        JOptionPane.INFORMATION_MESSAGE
                )
        );
    }
}
