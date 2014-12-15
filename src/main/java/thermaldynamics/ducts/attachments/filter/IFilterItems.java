package thermaldynamics.ducts.attachments.filter;

import net.minecraft.item.ItemStack;

public interface IFilterItems {
    public boolean matchesFilter(ItemStack item);

    final static IFilterItems nullFilter = new IFilterItems() {
        @Override
        public boolean matchesFilter(ItemStack item) {
            return true;
        }
    };


}
