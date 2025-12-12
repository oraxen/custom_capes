package dev.th0rgal.customcapes.core.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Response from the /capes endpoint.
 */
public final class CapesListResponse {

    private boolean success;
    private String backend;
    private List<CapeInfo> capes;

    public boolean isSuccess() {
        return success;
    }

    @Nullable
    public String getBackend() {
        return backend;
    }

    @NotNull
    public List<CapeInfo> getCapes() {
        return capes != null ? capes : Collections.emptyList();
    }

    /**
     * Information about an available cape.
     */
    public static final class CapeInfo {
        private String id;
        private String name;
        private boolean available;

        @NotNull
        public String getId() {
            return id != null ? id : "";
        }

        @NotNull
        public String getName() {
            return name != null ? name : "";
        }

        public boolean isAvailable() {
            return available;
        }
    }
}

