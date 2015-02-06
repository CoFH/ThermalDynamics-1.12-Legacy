package cofh.thermaldynamics.ducts.attachments.filter;

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
	};

}
