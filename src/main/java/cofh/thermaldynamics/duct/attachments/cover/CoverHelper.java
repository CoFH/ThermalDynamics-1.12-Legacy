package cofh.thermaldynamics.duct.attachments.cover;

import cofh.thermaldynamics.init.TDItems;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.StreamSupport;

public class CoverHelper {

	private static final Logger logger = LogManager.getLogger("ThermalDynamics");
	private static final boolean DEBUG_BLACKLIST = false;

	//Map for looking up the originating ItemStack for a cover.
	private static Map<String, ItemStack> lookupMap;
	private static Map<ResourceLocation, Integer> coverBlacklist = new HashMap<>();

	public static void loadCoverBlacklist(File configFolder) {
		File file = new File(configFolder, "cover_blacklist.json");
		if (!file.exists() || DEBUG_BLACKLIST) {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			try {
				file.createNewFile();
				FileUtils.copyInputStreamToFile(CoverHelper.class.getResourceAsStream("/assets/thermaldynamics/cover_blacklist_default.json"), file);
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}

		try {
			JsonParser parser = new JsonParser();
			JsonReader reader = new JsonReader(new FileReader(file));
			reader.setLenient(true);
			JsonElement element = parser.parse(reader);
			if (!element.isJsonArray()) {
				throw new JsonSyntaxException("Root element must be a JsonArray!");
			}
			JsonArray array = element.getAsJsonArray();
			for (JsonElement e : array) {
				if (!e.isJsonObject()) {
					throw new JsonSyntaxException("Expected JsonObject, was " + JsonUtils.toString(e));
				}
				JsonObject entry = e.getAsJsonObject();
				ResourceLocation loc = new ResourceLocation(JsonUtils.getString(entry, "block"));
				if (!entry.has("meta")) {
					throw new JsonSyntaxException("Missing required element 'meta'.");
				}
				if (!JsonUtils.isJsonPrimitive(entry, "meta")) {
					throw new JsonSyntaxException("Expected boolean or integer, was " + JsonUtils.toString(entry.get("meta")));
				}
				JsonPrimitive p = entry.getAsJsonPrimitive("meta");
				if (!(p.isBoolean() && !p.getAsBoolean()) && !p.isNumber()) {
					throw new JsonSyntaxException("Expected false or integer, was " + p.getAsString());
				}
				int meta;
				if (p.isBoolean()) {
					meta = -1;
				} else {
					meta = p.getAsInt();
				}
				coverBlacklist.put(loc, meta);
			}
		} catch (JsonParseException | IOException e) {
			//TODO, do we gracefully handle this?
			throw new RuntimeException("Unable to read cover blacklist json!", e);
		}
	}

	/**
	 * Gets the state to ItemStack lookup map, mainly for state -> ItemStack rendering lookup.
	 *
	 * @return The lookup map.
	 */
	public static Map<String, ItemStack> getStateLookup() {

		if (lookupMap == null) {
			lookupMap = new LinkedHashMap<>();
			NonNullList<ItemStack> stacks = NonNullList.create();
			StreamSupport.stream(ForgeRegistries.ITEMS.spliterator(), false)//
					.filter(item -> item instanceof ItemBlock)//
					.sorted(Comparator.comparingInt(Item.REGISTRY::getIDForObject))//
					.forEachOrdered(item -> item.getSubItems(CreativeTabs.SEARCH, stacks));
			for (ItemStack stack : stacks) {
				if (stack.getItem() instanceof ItemBlock) {
					ItemBlock item = (ItemBlock) stack.getItem();
					Block block = item.getBlock();
					int blockMeta = item.getMetadata(stack.getItemDamage());
					if (isValid(block, blockMeta)) {
						lookupMap.put(block.getRegistryName() + " >> " + blockMeta, stack);
					}
				}
			}
		}
		return lookupMap;
	}

	/**
	 * Attempts to lookup the ItemStack for the given BlockState.
	 *
	 * @param state The state to lookup.
	 * @return The stack, or empty if not found.
	 */
	public static ItemStack lookupItemForm(IBlockState state) {

		String lookup = state.getBlock().getRegistryName() + " >> " + state.getBlock().getMetaFromState(state);
		return getStateLookup().getOrDefault(lookup, ItemStack.EMPTY);
	}

	public static boolean isValid(ItemStack stack) {

		try {
			if (stack.getItem() instanceof ItemBlock) {
				if (isValid(((ItemBlock) stack.getItem()).getBlock(), stack.getItem().getMetadata(stack.getItemDamage()))) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isValid(Block block, int meta) {

		try {
			if (block == null) {
				return false;
			}

			if (coverBlacklist.containsKey(block.getRegistryName())) {
				int m = coverBlacklist.get(block.getRegistryName());
				if (m == -1 || m == meta) {
					return false;
				}
			}

			IBlockState state = block.getStateFromMeta(meta);
			return !(block.hasTileEntity(state) || block.hasTileEntity());
		} catch (Exception e) {
			return false;
		}
	}

	public static ItemStack getCoverStack(ItemStack stack) {

		if (stack.getItem() instanceof ItemBlock) {
			return getCoverStack(((ItemBlock) stack.getItem()).getBlock(), stack.getItem().getMetadata(stack.getItemDamage()));
		}
		return ItemStack.EMPTY;
	}

	public static ItemStack getCoverStack(IBlockState state) {

		return getCoverStack(state.getBlock(), state.getBlock().getMetaFromState(state));
	}

	public static ItemStack getCoverStack(Block block, int meta) {

		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("Block", ForgeRegistries.BLOCKS.getKey(block).toString());
		tag.setByte("Meta", ((byte) meta));

		ItemStack itemStack = new ItemStack(TDItems.itemCover);
		itemStack.setTagCompound(tag);
		return itemStack;
	}

	public static ItemStack getCoverItemStack(ItemStack stack, boolean removeInvalidData) {

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8)) {
			return ItemStack.EMPTY;
		}
		int meta = nbt.getByte("Meta");
		Block block = Block.getBlockFromName(nbt.getString("Block"));

		if (block == Blocks.AIR || meta < 0 || meta >= 16 || !isValid(block, meta)) {
			if (removeInvalidData) {
				nbt.removeTag("Meta");
				nbt.removeTag("Block");
				if (nbt.hasNoTags()) {
					stack.setTagCompound(null);
				}
			}
			return ItemStack.EMPTY;
		}
		return new ItemStack(block, 1, meta);
	}

}
