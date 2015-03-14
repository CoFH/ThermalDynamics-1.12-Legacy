package cofh.thermaldynamics.block;

import cofh.thermaldynamics.duct.attachments.facades.Cover;
import cofh.thermaldynamics.duct.attachments.filter.FilterFluid;
import cofh.thermaldynamics.duct.attachments.filter.FilterItem;
import cofh.thermaldynamics.duct.attachments.retriever.RetrieverFluid;
import cofh.thermaldynamics.duct.attachments.retriever.RetrieverItem;
import cofh.thermaldynamics.duct.attachments.servo.ServoFluid;
import cofh.thermaldynamics.duct.attachments.servo.ServoItem;

public class AttachmentRegistry {

	public final static byte FACADE = 0;
	public final static byte SERVO_FLUID = 1;
	public final static byte SERVO_ITEM = 2;
	public final static byte FILTER_FLUID = 3;
	public final static byte FILTER_ITEM = 4;
    public final static byte RETRIEVER_FLUID = 5;
    public final static byte RETRIEVER_ITEM = 6;

	public static Attachment createAttachment(TileTDBase tile, byte side, int id) {

		if (id == FACADE) {
			return new Cover(tile, side);
		} else if (id == SERVO_FLUID) {
			return new ServoFluid(tile, side);
		} else if (id == SERVO_ITEM) {
			return new ServoItem(tile, side);
		} else if (id == FILTER_FLUID) {
			return new FilterFluid(tile, side);
		} else if (id == FILTER_ITEM) {
            return new FilterItem(tile, side);
        }else if (id == RETRIEVER_FLUID){
            return new RetrieverFluid(tile,side);
		} else if (id == RETRIEVER_ITEM) {
            return new RetrieverItem(tile, side);
        }
		throw new RuntimeException("Illegal Attachment ID");
	}

}
