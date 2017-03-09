package cofh.thermaldynamics.init;

import cofh.core.init.CoreProps;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;

public class TDTextures {

	private TDTextures() {

	}

	public static void registerIcons(TextureStitchEvent.Pre event) {

		map = event.getMap();

		//@formatter:off
		SERVO_BASE_0_0 =        register(SERVO_ + "0_0");
		SERVO_BASE_0_1 =        register(SERVO_ + "0_1");
		SERVO_BASE_0_2 =        register(SERVO_ + "0_2");
		SERVO_BASE_0_3 =        register(SERVO_ + "0_3");
		SERVO_BASE_0_4 =        register(SERVO_ + "0_4");
		SERVO_BASE_1_0 =        register(SERVO_ + "1_0");
		SERVO_BASE_1_1 =        register(SERVO_ + "1_1");
		SERVO_BASE_1_2 =        register(SERVO_ + "1_2");
		SERVO_BASE_1_3 =        register(SERVO_ + "1_3");
		SERVO_BASE_1_4 =        register(SERVO_ + "1_4");

		RETRIEVER_BASE_0_0 =    register(RETRIEVER_ + "0_0");
		RETRIEVER_BASE_0_1 =    register(RETRIEVER_ + "0_1");
		RETRIEVER_BASE_0_2 =    register(RETRIEVER_ + "0_2");
		RETRIEVER_BASE_0_3 =    register(RETRIEVER_ + "0_3");
		RETRIEVER_BASE_0_4 =    register(RETRIEVER_ + "0_4");
		RETRIEVER_BASE_1_0 =    register(RETRIEVER_ + "1_0");
		RETRIEVER_BASE_1_1 =    register(RETRIEVER_ + "1_1");
		RETRIEVER_BASE_1_2 =    register(RETRIEVER_ + "1_2");
		RETRIEVER_BASE_1_3 =    register(RETRIEVER_ + "1_3");
		RETRIEVER_BASE_1_4 =    register(RETRIEVER_ + "1_4");


		FILTER_BASE_0 =         register(FILTER_ + "0");
		FILTER_BASE_1 =         register(FILTER_ + "1");
		FILTER_BASE_2 =         register(FILTER_ + "2");
		FILTER_BASE_3 =         register(FILTER_ + "3");
		FILTER_BASE_4 =         register(FILTER_ + "4");

		SIGNALLER =             register(DUCT_ATTACHMENT_ + "signallers/signaller");

		COVER_SIDE =            register(DUCT_ATTACHMENT_ + "cover/cover_side");

		SIDE_DUCTS =            register(BLOCKS_ + "duct/side_ducts");



		SERVO_BASE = new TextureAtlasSprite[][] {
				new TextureAtlasSprite[] {
						SERVO_BASE_0_0,
						SERVO_BASE_0_1,
						SERVO_BASE_0_2,
						SERVO_BASE_0_3,
						SERVO_BASE_0_4,
				},
				new TextureAtlasSprite[] {
						SERVO_BASE_1_0,
						SERVO_BASE_1_1,
						SERVO_BASE_1_2,
						SERVO_BASE_1_3,
						SERVO_BASE_1_4,
				}
		};

		RETRIEVER_BASE = new TextureAtlasSprite[][] {
				new TextureAtlasSprite[] {
						RETRIEVER_BASE_0_0,
						RETRIEVER_BASE_0_1,
						RETRIEVER_BASE_0_2,
						RETRIEVER_BASE_0_3,
						RETRIEVER_BASE_0_4,
				},
				new TextureAtlasSprite[] {
						RETRIEVER_BASE_1_0,
						RETRIEVER_BASE_1_1,
						RETRIEVER_BASE_1_2,
						RETRIEVER_BASE_1_3,
						RETRIEVER_BASE_1_4,
				}
		};

		FILTER_BASE = new TextureAtlasSprite[] {
				FILTER_BASE_0,
				FILTER_BASE_1,
				FILTER_BASE_2,
				FILTER_BASE_3,
				FILTER_BASE_4,
		};


		//@formatter:on
	}

	// Bouncer to make the class readable.
	private static TextureAtlasSprite register(String sprite) {

		return map.registerSprite(new ResourceLocation(sprite));
	}

	//Assign the TextureMap to a file to make things even more readable!.
	private static TextureMap map;

	// Bouncer for registering ColorBlind textures.
	private static TextureAtlasSprite registerCB(String sprite) {

		if (CoreProps.enableColorBlindTextures) {
			sprite += CB_POSTFIX;
		}
		return register(sprite);
	}

	private static String CB_POSTFIX = "_cb";

	private static final String BLOCKS_ = "thermaldynamics:blocks/";
	private static final String DUCT_ATTACHMENT_ = BLOCKS_ + "duct/attachment/";
	private static final String SERVO_ = DUCT_ATTACHMENT_ + "servo/servo_base_";
	private static final String RETRIEVER_ = DUCT_ATTACHMENT_ + "retriever/retriever_base_";
	private static final String FILTER_ = DUCT_ATTACHMENT_ + "filter/filter_";

	/* REFERENCES */
	public static TextureAtlasSprite[][] SERVO_BASE;
	public static TextureAtlasSprite SERVO_BASE_0_0;
	public static TextureAtlasSprite SERVO_BASE_0_1;
	public static TextureAtlasSprite SERVO_BASE_0_2;
	public static TextureAtlasSprite SERVO_BASE_0_3;
	public static TextureAtlasSprite SERVO_BASE_0_4;
	public static TextureAtlasSprite SERVO_BASE_1_0;
	public static TextureAtlasSprite SERVO_BASE_1_1;
	public static TextureAtlasSprite SERVO_BASE_1_2;
	public static TextureAtlasSprite SERVO_BASE_1_3;
	public static TextureAtlasSprite SERVO_BASE_1_4;

	public static TextureAtlasSprite[][] RETRIEVER_BASE;
	public static TextureAtlasSprite RETRIEVER_BASE_0_0;
	public static TextureAtlasSprite RETRIEVER_BASE_0_1;
	public static TextureAtlasSprite RETRIEVER_BASE_0_2;
	public static TextureAtlasSprite RETRIEVER_BASE_0_3;
	public static TextureAtlasSprite RETRIEVER_BASE_0_4;
	public static TextureAtlasSprite RETRIEVER_BASE_1_0;
	public static TextureAtlasSprite RETRIEVER_BASE_1_1;
	public static TextureAtlasSprite RETRIEVER_BASE_1_2;
	public static TextureAtlasSprite RETRIEVER_BASE_1_3;
	public static TextureAtlasSprite RETRIEVER_BASE_1_4;

	public static TextureAtlasSprite[] FILTER_BASE;
	public static TextureAtlasSprite FILTER_BASE_0;
	public static TextureAtlasSprite FILTER_BASE_1;
	public static TextureAtlasSprite FILTER_BASE_2;
	public static TextureAtlasSprite FILTER_BASE_3;
	public static TextureAtlasSprite FILTER_BASE_4;

	public static TextureAtlasSprite SIGNALLER;

	public static TextureAtlasSprite COVER_SIDE;

	public static TextureAtlasSprite SIDE_DUCTS;

}
