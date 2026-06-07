package com.bytedance.myapplication;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerPool {

    private static final String TAG = "PlayerPool";
    private static final int MAX_POOL_SIZE = 3;
    private static PlayerPool instance;

    private List<MediaPlayer> idlePlayers;
    private ConcurrentHashMap<String, MediaPlayer> activePlayers;
    private ConcurrentHashMap<MediaPlayer, Float> playerVolumes;
    private Context context;

    private PlayerPool(Context context) {
        this.context = context.getApplicationContext();
        this.idlePlayers = new ArrayList<>();
        this.activePlayers = new ConcurrentHashMap<>();
        this.playerVolumes = new ConcurrentHashMap<>();
    }

    public static synchronized PlayerPool getInstance(Context context) {
        if (instance == null) {
            instance = new PlayerPool(context);
        }
        return instance;
    }

    public MediaPlayer acquirePlayer(String playerId) {
        MediaPlayer player = null;

        synchronized (idlePlayers) {
            if (!idlePlayers.isEmpty()) {
                player = idlePlayers.remove(idlePlayers.size() - 1);
            }
        }

        if (player == null) {
            player = createNewPlayer();
        }

        activePlayers.put(playerId, player);
        return player;
    }

    public void releasePlayer(String playerId) {
        MediaPlayer player = activePlayers.remove(playerId);
        if (player != null) {
            resetPlayer(player);

            synchronized (idlePlayers) {
                if (idlePlayers.size() < MAX_POOL_SIZE) {
                    idlePlayers.add(player);
                } else {
                    player.release();
                }
            }
        }
    }

    public void releaseAllPlayers() {
        synchronized (idlePlayers) {
            for (MediaPlayer player : idlePlayers) {
                try {
                    player.release();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            idlePlayers.clear();
        }

        for (MediaPlayer player : activePlayers.values()) {
            try {
                player.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        activePlayers.clear();
    }

    private MediaPlayer createNewPlayer() {
        MediaPlayer player = new MediaPlayer();
        player.setLooping(true);
        player.setVolume(0f, 0f); // 默认静音
        player.setOnErrorListener((mp, what, extra) -> {
            String key = findKeyByPlayer(mp);
            if (key != null) {
                releasePlayer(key);
            }
            return true;
        });
        return player;
    }

    public void setMute(MediaPlayer player, boolean mute) {
        if (player != null) {
            float volume = mute ? 0f : 1f;
            player.setVolume(volume, volume);
            playerVolumes.put(player, volume);
        }
    }

    public void setVolume(MediaPlayer player, float volume) {
        if (player != null && volume >= 0f && volume <= 1f) {
            player.setVolume(volume, volume);
            playerVolumes.put(player, volume);
        }
    }

    public boolean isMuted(MediaPlayer player) {
        return player != null && playerVolumes.getOrDefault(player, 0f) == 0f;
    }

    public float getVolume(MediaPlayer player) {
        return player != null ? playerVolumes.getOrDefault(player, 1f) : 0f;
    }

    private void resetPlayer(MediaPlayer player) {
        try {
            player.stop();
            player.reset();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void preparePlayer(MediaPlayer player, String videoUrl, SurfaceHolder holder) throws IOException {
        player.setDataSource(context, Uri.parse(videoUrl));
        player.setDisplay(holder);
        player.prepareAsync();
    }

    private String findKeyByPlayer(MediaPlayer player) {
        for (ConcurrentHashMap.Entry<String, MediaPlayer> entry : activePlayers.entrySet()) {
            if (entry.getValue() == player) {
                return entry.getKey();
            }
        }
        return null;
    }

    public int getIdlePlayerCount() {
        synchronized (idlePlayers) {
            return idlePlayers.size();
        }
    }

    public int getActivePlayerCount() {
        return activePlayers.size();
    }
}