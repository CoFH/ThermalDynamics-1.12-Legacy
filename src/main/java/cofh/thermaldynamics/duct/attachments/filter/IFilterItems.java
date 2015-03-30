package cofh.thermaldynamics.duct.attachments.filter;

import net.minecraft.item.ItemStack;

public interface IFilterItems {

	public boolean matchesFilter(ItemStack item);

	public boolean shouldIncRouteItems();

	final static IFilterItems nullFilter = new IFilterItems() {

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

	public int getMaxStock();
}
