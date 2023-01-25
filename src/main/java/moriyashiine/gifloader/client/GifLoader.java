package moriyashiine.gifloader.client;

import at.dhyan.open_imaging.GifDecoder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class GifLoader implements ClientModInitializer {
	private static final Object2ObjectMap<Identifier, GifData> CACHED_GIFS = new Object2ObjectOpenHashMap<>();

	private static final Object2ObjectMap<Identifier, Integer> FRAMES = new Object2ObjectOpenHashMap<>(), DELAYS = new Object2ObjectOpenHashMap<>();
	private static final List<Pair<Identifier, Integer>> UNLOAD_TIMERS = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!client.isPaused()) {
				for (int i = UNLOAD_TIMERS.size() - 1; i >= 0; i--) {
					int timer = UNLOAD_TIMERS.get(i).getRight();
					if (timer > 1) {
						UNLOAD_TIMERS.get(i).setRight(timer - 1);
					} else {
						Identifier identifier = UNLOAD_TIMERS.get(i).getLeft();
						FRAMES.remove(identifier);
						DELAYS.remove(identifier);
						UNLOAD_TIMERS.remove(i);
					}
				}
			}
		});
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			private static final Identifier ID = new Identifier("gif_loader", "gif_loader");

			@Override
			public Identifier getFabricId() {
				return ID;
			}

			@Override
			public void reload(ResourceManager manager) {
				CACHED_GIFS.clear();
				FRAMES.clear();
				DELAYS.clear();
				UNLOAD_TIMERS.clear();
			}
		});
	}

	public static Identifier getFrame(Identifier identifier) {
		if (identifier.getPath().endsWith(".gif")) {
			boolean paused = MinecraftClient.getInstance().isPaused();
			GifData gifData = loadGif(identifier);
			int frame = FRAMES.getOrDefault(identifier, 0);
			int delay = DELAYS.getOrDefault(identifier, -1);
			if (!paused) {
				if (delay == -1) {
					delay = (int) (gifData.getDelay(frame) / 1.5F);
				}
				if (delay > 0) {
					delay--;
				} else {
					frame = (frame + 1) % gifData.getFrames();
					delay = -1;
				}
				FRAMES.put(identifier, frame);
				DELAYS.put(identifier, delay);
				UNLOAD_TIMERS.stream().filter(pair -> pair.getLeft().equals(identifier)).findFirst().ifPresentOrElse(pair -> pair.setRight(2), () -> UNLOAD_TIMERS.add(new Pair<>(identifier, 2)));
			}
			return gifData.getImage(frame);
		}
		return identifier;
	}

	@SuppressWarnings("unchecked")
	public static GifData loadGif(Identifier identifier) {
		return CACHED_GIFS.computeIfAbsent(identifier, name -> {
			try {
				GifDecoder.GifImage gif = GifDecoder.read(MinecraftClient.getInstance().getResourceManager().getResourceOrThrow(identifier).getInputStream());
				Pair<Identifier, Integer>[] frames = new Pair[gif.getFrameCount()];
				for (int i = 0; i < frames.length; i++) {
					Identifier frameId = new Identifier(identifier + "_frame" + i);
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					ImageIO.write(gif.getFrame(i), "png", outputStream);
					MinecraftClient.getInstance().getTextureManager().registerTexture(frameId, new NativeImageBackedTexture(NativeImage.read(new ByteArrayInputStream(outputStream.toByteArray()))));
					frames[i] = new Pair<>(frameId, gif.getDelay(i));
				}
				return new GifData(frames);
			} catch (IOException e) {
				throw new RuntimeException("Unable to find gif at " + identifier);
			}
		});
	}

	public record GifData(Pair<Identifier, Integer>[] frames) {
		public Identifier getImage(int frame) {
			return frames[frame].getLeft();
		}

		public int getDelay(int frame) {
			return frames[frame].getRight();
		}

		public int getFrames() {
			return frames.length;
		}
	}
}
