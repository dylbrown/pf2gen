package model.player;

import javafx.collections.ObservableMap;
import model.data_managers.sources.SourcesLoader;
import model.enums.Slot;
import model.equipment.BaseItem;
import model.equipment.Item;
import model.equipment.ItemCount;
import model.equipment.armor.Armor;
import model.equipment.weapons.Weapon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InventoryManagerTest {

    private InventoryManager inventory;

    private List<Item> sampleItems;
    private PC pc;


    @BeforeEach
    void setUp() {
        pc = new PC(SourcesLoader.ALL_SOURCES);
        inventory = pc.inventory();
        inventory.setMoney(30000000);
        sampleItems = new ArrayList<>();
        int i=0;
        for (Item item : SourcesLoader.ALL_SOURCES.equipment()
                .getAll().values()) {
            sampleItems.add(item);
            if(i>=20) break;
            i++;
        }
    }

    @Test
    void buyValid() {
        int currentAmount = 0;
        Item item = sampleItems.get(0);
        for(int i=1; i<6; i++) {
            assertTrue(inventory.buy(item, i));
            currentAmount += i;
            checkMatches(inventory.getItems(), new ItemCount(item, currentAmount));
            checkMatches(inventory.getUnequipped(), new ItemCount(item, currentAmount));
        }
    }

    @Test
    void buyZero() {
        Item item = sampleItems.get(0);
        assertFalse(inventory.buy(item, 0));
        assertEmpty();
    }

    @Test
    void buyTooMany() {
        inventory.setMoney(10);
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.setName("test");
        builder.setValue(5);
        builder.getExtension(Weapon.Builder.class);
        Item item = builder.build();
        assertFalse(inventory.buy(item, 11));
        assertEmpty();
    }
    @Test
    void buyExact() {
        inventory.setMoney(10);
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.setName("test");
        builder.setValue(1);
        builder.getExtension(Weapon.Builder.class);
        Item item = builder.build();
        assertTrue(inventory.buy(item, 10));
        checkMatches(inventory.getItems(), new ItemCount(item, 10));
        checkMatches(inventory.getUnequipped(), new ItemCount(item, 10));
    }

    @Test
    void sellValidRemaining() {
        int currentAmount = 15;
        Item item = sampleItems.get(0);
        assertTrue(inventory.buy(item, 15));
        for(int i=5; i>1; i--) {
            assertTrue(inventory.sell(item, i));
            currentAmount -= i;
            checkMatches(inventory.getItems(), new ItemCount(item, currentAmount));
            checkMatches(inventory.getUnequipped(), new ItemCount(item, currentAmount));
        }
    }

    @Test
    void sellLast(){
        Item item = sampleItems.get(0);
        assertTrue(inventory.buy(item, 15));
        assertTrue(inventory.sell(item, 15));
        assertEmpty();
    }

    @Test
    void sellOver(){
        Item item = sampleItems.get(0);
        assertTrue(inventory.buy(item, 14));
        assertFalse(inventory.sell(item, 15));

        checkMatches(inventory.getItems(), new ItemCount(item, 14));
        checkMatches(inventory.getUnequipped(),new ItemCount(item, 14));
    }

    @Test
    void getTotalValue() {
        double totalValue = 0;
        int i=1;
        for (Item sampleItem : sampleItems) {
            assertTrue(inventory.buy(sampleItem, i));
            totalValue += sampleItem.getValue() * i;
            i++;
            assertEquals(totalValue, inventory.getTotalValue());
        }
        i = 1;
        for (Item sampleItem : sampleItems) {
            assertTrue(inventory.sell(sampleItem, i));
            totalValue -= sampleItem.getValue() * i;
            i++;
            assertEquals(totalValue, inventory.getTotalValue());
        }
    }

    @Test
    void equipSome() {
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.setName("test");
        builder.setValue(0);
        builder.getExtension(Armor.Builder.class);
        Item item1 = builder.build();
        Item item2 = sampleItems.get(1);
        assertTrue(inventory.buy(item1, 3));
        assertTrue(inventory.buy(item2, 5));
        assertTrue(inventory.equip(item1, item1.getSlot(), 2));
        assertEquals(1, inventory.getEquipped().size());

        checkMatches(inventory.getUnequipped(),
                new ItemCount(item1, 1),
                new ItemCount(item2, 5));
        assertEquals(1, inventory.getEquipped().size());
        ItemCount actual = inventory.getEquipped().get(item1.getSlot());
        assertEquals(item1, actual.stats());
        assertEquals(2, actual.getCount());
    }

    @Test
    void equipAll() {
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.setName("test");
        builder.setValue(5);
        builder.getExtension(Armor.Builder.class);
        Item item1 = builder.build();
        Item item2 = sampleItems.get(1);
        assertTrue(inventory.buy(item1, 3));
        assertTrue(inventory.buy(item2, 5));
        assertTrue(inventory.equip(item1, item1.getSlot(), 3));
        assertEquals(1, inventory.getEquipped().size());

        checkMatches(inventory.getUnequipped(),
                new ItemCount(item2, 5));
        assertEquals(1, inventory.getEquipped().size());
        ItemCount actual = inventory.getEquipped().get(item1.getSlot());
        assertEquals(item1, actual.stats());
        assertEquals(3, actual.getCount());
    }

    @Test
    void unequipSome() {
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.setName("test");
        builder.setValue(5);
        builder.getExtension(Armor.Builder.class);
        Item item1 = builder.build();
        Item item2 = sampleItems.get(1);
        assertTrue(inventory.buy(item1, 3));
        assertTrue(inventory.buy(item2, 5));
        assertTrue(inventory.equip(item1, item1.getSlot(), 3));
        assertTrue(inventory.unequip(item1, item1.getSlot(), 1));
        assertEquals(1, inventory.getEquipped().size());

        checkMatches(inventory.getUnequipped(),
                new ItemCount(item1, 1),
                new ItemCount(item2, 5));
        assertEquals(1, inventory.getEquipped().size());
        ItemCount actual = inventory.getEquipped().get(item1.getSlot());
        assertEquals(item1, actual.stats());
        assertEquals(2, actual.getCount());
    }

    @Test
    void unequipAll() {
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.setName("test");
        builder.setValue(5);
        builder.getExtension(Armor.Builder.class);
        Item item1 = builder.build();
        Item item2 = sampleItems.get(1);
        assertTrue(inventory.buy(item1, 3));
        assertTrue(inventory.buy(item2, 5));
        assertTrue(inventory.equip(item1, item1.getSlot(), 3));
        assertTrue(inventory.unequip(item1, item1.getSlot(), 3));
        assertEquals(0, inventory.getEquipped().size());

        checkMatches(inventory.getUnequipped(),
                new ItemCount(item2, 5),
                new ItemCount(item1, 3));
    }


    @Test
    void getEquipped() {
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.setName("test");
        builder.setValue(5);
        builder.getExtension(Armor.Builder.class);
        Item item1 = builder.build();
        inventory.buy(item1, 3);
        inventory.equip(item1, item1.getSlot(), 2);
        assertEquals(1, inventory.getUnequipped().size());
        assertEquals(1, inventory.getEquipped().size());

        checkMatches(inventory.getUnequipped(),
                new ItemCount(item1, 1));
        ItemCount actual = inventory.getEquipped(item1.getSlot());
        assertEquals(item1, actual.stats());
        assertEquals(2, actual.getCount());

        for (Slot value : Slot.values()) {
            if(value != item1.getSlot()) {
                assertNull(inventory.getEquipped(value));
            }
        }
    }

    @Test
    void equipOneHand() {
        BaseItem.Builder builder = new BaseItem.Builder();
        builder.setName("test");
        builder.setValue(5);
        builder.getExtension(Armor.Builder.class);
        Item item1 = builder.build();
        inventory.buy(item1, 3);
        inventory.equip(item1, item1.getSlot(), 2);
        assertEquals(1, inventory.getUnequipped().size());
        assertEquals(1, inventory.getEquipped().size());

        checkMatches(inventory.getUnequipped(),
                new ItemCount(item1, 1));
        ItemCount actual = inventory.getEquipped(Slot.PrimaryHand);
        assertEquals(item1, actual.stats());
        assertEquals(2, actual.getCount());

        for (Slot value : Slot.values()) {
            if(value != Slot.PrimaryHand) {
                assertNull(inventory.getEquipped(value));
            }
        }
    }

    @Test
    void reset() {
        int i=1;
        for (Item sampleItem : sampleItems) {
            assertTrue(inventory.buy(sampleItem, i));
            i++;
        }
        pc.reset();
        assertEquals(InventoryManager.INITIAL_AMOUNT, inventory.getMoney());
        assertEmpty();
    }

    @Test
    void getTotalWeight() {
        double totalWeight = 0;
        int i=1;
        for (Item sampleItem : sampleItems) {
            assertTrue(inventory.buy(sampleItem, i));
            totalWeight += sampleItem.getWeight() * i;
            i++;
            assertEquals(totalWeight, inventory.getTotalWeight());
        }
    }

    private void assertEmpty(){
        assertEquals(0, inventory.getItems().size());
        assertEquals(0, inventory.getEquipped().size());
        assertEquals(0, inventory.getUnequipped().size());
        assertEquals(0, inventory.getTotalValue());
        assertEquals(0, inventory.getTotalWeight());
    }


    private void checkMatches(ObservableMap<Item, ItemCount> actualList, ItemCount... expected) {
        assertEquals(expected.length, actualList.size());
        for (ItemCount itemCount : expected) {
            ItemCount actual = actualList.get(itemCount.stats());
            assertNotNull(actual);
            assertEquals(itemCount.getCount(), actual.getCount());
            assertEquals(itemCount.stats(), actual.stats());
        }
    }
}