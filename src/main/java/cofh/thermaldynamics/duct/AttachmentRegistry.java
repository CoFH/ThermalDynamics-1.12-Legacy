package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.filter.FilterFluid;
import cofh.thermaldynamics.duct.attachments.filter.FilterItem;
import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.duct.attachments.retriever.RetrieverFluid;
import cofh.thermaldynamics.duct.attachments.retriever.RetrieverItem;
import cofh.thermaldynamics.duct.attachments.servo.ServoFluid;
import cofh.thermaldynamics.duct.attachments.servo.ServoItem;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class AttachmentRegistry {

	private final static Map<ResourceLocation, BiFunction<TileGrid, Byte, Attachment>> REGISTRY = new HashMap<>();

	public final static ResourceLocation FACADE = new ResourceLocation(ThermalDynamics.MOD_ID, "facade");
	public final static ResourceLocation SERVO_FLUID = new ResourceLocation(ThermalDynamics.MOD_ID, "servo_fluid");
	public final static ResourceLocation SERVO_ITEM = new ResourceLocation(ThermalDynamics.MOD_ID, "servo_item");
	public final static ResourceLocation FILTER_FLUID = new ResourceLocation(ThermalDynamics.MOD_ID, "filter_fluid");
	public final static ResourceLocation FILTER_ITEM = new ResourceLocation(ThermalDynamics.MOD_ID, "filter_item");
	public final static ResourceLocation RETRIEVER_FLUID = new ResourceLocation(ThermalDynamics.MOD_ID, "retriever_fluid");
	public final static ResourceLocation RETRIEVER_ITEM = new ResourceLocation(ThermalDynamics.MOD_ID, "retriever_item");
	public final static ResourceLocation RELAY = new ResourceLocation(ThermalDynamics.MOD_ID, "relay");

	static {

		registerAttachment(FACADE, Cover::new);
		registerAttachment(SERVO_FLUID, ServoFluid::new);
		registerAttachment(SERVO_ITEM, ServoItem::new);
		registerAttachment(FILTER_FLUID, FilterFluid::new);
		registerAttachment(FILTER_ITEM, FilterItem::new);
		registerAttachment(RETRIEVER_FLUID, RetrieverFluid::new);
		registerAttachment(RETRIEVER_ITEM, RetrieverItem::new);
		registerAttachment(RELAY, Relay::new);
	}

	public static Attachment createAttachment(TileGrid tile, byte side, ResourceLocation id) {

		if (REGISTRY.containsKey(id))
			return REGISTRY.get(id).apply(tile, side);
		throw new RuntimeException("Illegal Attachment ID");
	}

	public static boolean registerAttachment(ResourceLocation id, BiFunction<TileGrid, Byte, Attachment> function) {

		if (!REGISTRY.containsKey(id)) {
			REGISTRY.put(id, function);
			return true;
		}
		return false;
	}

}
