package thermaldynamics.render;

import cofh.repack.codechicken.lib.render.TextureDataHolder;
import cofh.repack.codechicken.lib.render.TextureSpecial;
import cofh.repack.codechicken.lib.render.TextureUtils;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.ResourceLocation;

public class TextureOverlay {
    private final static String folder = "thermaldynamics:textures/blocks/duct/";
    private final static Tex textures[][] = {
            {Tex.newTexture("item", "DuctBase")},
            {null, Tex.newTexture("item", "DuctUncover", 1F), Tex.newTexture("item", "DuctCover")},
            {null, Tex.newTexture("item", "DuctHighlight", 0.5F, 0.4F, 1, 0.4F), Tex.newTexture("item", "DuctHighlight", 0.75F, 0.2F, 0.2F, 0.2F), Tex.newTexture("item", "DuctHighlight", 0.5F, 1, 0.4F, 0.4F)}
    };

    private static class Tex {
        float a;
        float r, g, b;
        ResourceLocation location;

        public static Tex newTexture(String type, String name) {
            return new Tex(new ResourceLocation(folder + type + "/" + name + ".png"));
        }

        private static Tex newTexture(String type, String name, float r, float g, float b) {
            return new Tex(new ResourceLocation(folder + type + "/" + name + ".png"), 1, r, g, b);
        }

        private static Tex newTexture(String type, String name, float transparency) {
            return new Tex(new ResourceLocation(folder + type + "/" + name + ".png"), transparency);
        }

        private static Tex newTexture(String type, String name, float transparency, float r, float g, float b) {
            return new Tex(new ResourceLocation(folder + type + "/" + name + ".png"), transparency, r, g, b);
        }

        private static Tex newTexture(String name, float transparency, float r, float g, float b) {
            return new Tex(new ResourceLocation(name), transparency, r, g, b);
        }


        private Tex(ResourceLocation location) {
            this(location, 1, 1, 1, 1);
        }

        private Tex(ResourceLocation location, float a) {
            this(location, a, 1, 1, 1);
        }

        private Tex(ResourceLocation location, float a, float r, float g, float b) {
            this.a = a;
            this.r = r;
            this.g = g;
            this.b = b;
            this.location = location;
        }

        public TextureDataHolder loadTexture() {
            TextureDataHolder image = TextureUtils.loadTexture(location);
            if (a != 1 || r != 1 || g != 1 || b != 1)
                for (int i = 0; i < image.data.length; i++) {
                    int colour = image.data[i];
                    int na = (colour >> 24) & 0xFF;
                    int nr = (colour >> 16) & 0xFF;
                    int ng = (colour >> 8) & 0xFF;
                    int nb = colour & 0xFF;


                    if (na != 0) {

                        nr = (int) (nr * r);
                        ng = (int) (ng * g);
                        nb = (int) (nb * b);
                        na = (int) (na * a);

                        image.data[i] = (na & 0xFF) << 24 | (nr & 0xFF) << 16 | (ng & 0xFF) << 8 | (nb & 0xFF);
                    }
                }
            return image;
        }
    }


    public static TextureSpecial generateTexture(IIconRegister register, boolean removeTransparency, int... ints) {
        TextureDataHolder image = null, newimage;
        //image = textures[0][ints[0]].loadTexture();
        for (int i = 0; i < ints.length; i++) {
            if (textures[i][ints[i]] != null) {
                newimage = textures[i][ints[i]].loadTexture();
                if (image == null)
                    image = newimage;
                else {
                    if (newimage.width != image.width || newimage.height != image.height)
                        throw new RuntimeException("Textures " + textures[0][ints[0]] + " and " + textures[i][ints[i]].toString() + " must be the same size");

                    for (int j = 0; j < newimage.data.length; j++) {
                        final int colour = newimage.data[j];
                        final int alpha = (colour >> 24) & 0xFF;
                        if (alpha != 0) {
                            image.data[j] = colour;

                            if (alpha < 255 && removeTransparency) {
                                image.data[j] = 0xFF000000 | (colour & 0x00FFFFFF);
                            }
                        }
                    }
                }
            }
        }

        StringBuilder builder = new StringBuilder("thermaldynamics:Duct");
        for (int i : ints) builder.append(i);
        TextureSpecial texture = TextureUtils.getTextureSpecial(register, builder.toString());
        texture.addTexture(image);
        return texture;
    }

}
