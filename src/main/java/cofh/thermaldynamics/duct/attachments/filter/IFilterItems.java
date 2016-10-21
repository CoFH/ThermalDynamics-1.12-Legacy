package cofh.thermaldynamics.duct.attachments.filter;

import net.minecraft.item.ItemStack;

public interface IFilterItems {

	boolean matchesFilter(ItemStack item);

	boolean shouldIncRouteItems();

	IFilterItems nullFilter = new IFilterItems() {

		@Override
		public boolean matchesFilter(ItemStack item) {

			return true;
		}

		@Override
		public boolean shouldIncRouteItems() {

			return true;
		}

		@Override
		public int getMaxStock() {

			return Integer.MAX_VALUE;
		}
	};

	int getMaxStock();

}
