package dev.th0rgal.customcapes.bukkit;

import dev.th0rgal.customcapes.bukkit.commands.CapeCommand;
import dev.th0rgal.customcapes.core.api.CapesApiClient;
import dev.th0rgal.customcapes.core.api.CapesListResponse;
import dev.th0rgal.customcapes.core.config.Config;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Main entry point for the Custom Capes Bukkit/Paper plugin.
 */
public final class CustomCapesPlugin extends JavaPlugin {

    private static CustomCapesPlugin instance;

    private Config config;
    private CapesApiClient apiClient;
    private BukkitAudiences audiences;
    private SkinApplierBukkit skinApplier;
    
    /** Cached list of available capes from the API */
    private volatile List<CapesListResponse.CapeInfo> availableCapes;
    /** Set of available cape IDs for quick lookup */
    private final Set<String> availableCapeIds = ConcurrentHashMap.newKeySet();
    /** The backend type being used by the API */
    private volatile String apiBackend = "unknown";

    @Override
    public void onEnable() {
        instance = this;

        // Load configuration
        config = Config.load(getDataFolder());

        // Initialize API client
        apiClient = new CapesApiClient(config.getApiUrl(), config.getTimeoutSeconds());

        // Initialize Adventure audiences for messaging
        audiences = BukkitAudiences.create(this);

        // Initialize skin applier
        skinApplier = new SkinApplierBukkit(this);

        // Register commands
        CapeCommand capeCommand = new CapeCommand(this);
        getCommand("cape").setExecutor(capeCommand);
        getCommand("cape").setTabCompleter(capeCommand);

        // Initialize bStats metrics
        new Metrics(this, 23456); // Replace with actual bStats plugin ID

        getLogger().info("Custom Capes enabled! API: " + config.getApiUrl());

        // Fetch available capes from API in background
        refreshAvailableCapes();
    }

    /**
     * Refresh the list of available capes from the API.
     */
    public void refreshAvailableCapes() {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                CapesListResponse response = apiClient.getAvailableCapes();
                availableCapes = response.getCapes();
                apiBackend = response.getBackend() != null ? response.getBackend() : "unknown";
                
                // Update available IDs set
                availableCapeIds.clear();
                for (CapesListResponse.CapeInfo cape : availableCapes) {
                    if (cape.isAvailable()) {
                        availableCapeIds.add(cape.getId().toLowerCase());
                    }
                }
                
                long availableCount = availableCapes.stream().filter(CapesListResponse.CapeInfo::isAvailable).count();
                getLogger().info("Fetched " + availableCount + "/" + availableCapes.size() + 
                    " available capes from API (backend: " + apiBackend + ")");
            } catch (CapesApiClient.CapesApiException e) {
                getLogger().warning("Failed to fetch available capes: " + e.getMessage());
            }
        });
    }

    @Override
    public void onDisable() {
        if (audiences != null) {
            audiences.close();
        }
        instance = null;
        getLogger().info("Custom Capes disabled.");
    }

    /**
     * Reload the plugin configuration.
     */
    public void reload() {
        config = Config.load(getDataFolder());
        apiClient = new CapesApiClient(config.getApiUrl(), config.getTimeoutSeconds());
        refreshAvailableCapes();
        getLogger().info("Configuration reloaded.");
    }

    @NotNull
    public static CustomCapesPlugin get() {
        return instance;
    }

    @NotNull
    public Config getPluginConfig() {
        return config;
    }

    @NotNull
    public CapesApiClient getApiClient() {
        return apiClient;
    }

    @NotNull
    public BukkitAudiences getAudiences() {
        return audiences;
    }

    @NotNull
    public SkinApplierBukkit getSkinApplier() {
        return skinApplier;
    }

    /**
     * Get the cached list of available capes from the API.
     * May be null if not yet fetched.
     */
    @Nullable
    public List<CapesListResponse.CapeInfo> getAvailableCapes() {
        return availableCapes;
    }

    /**
     * Check if a cape type ID is available.
     */
    public boolean isCapeAvailable(@NotNull String capeId) {
        return availableCapeIds.contains(capeId.toLowerCase());
    }

    /**
     * Get the API backend type ("mineskin" or "internal").
     */
    @NotNull
    public String getApiBackend() {
        return apiBackend;
    }
}

