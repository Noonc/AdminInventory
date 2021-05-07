package io.github.noonc.admininventory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class InventorySerialization {
	public static String encoded;
	public static String iEncoded;
    public static String toBase64(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(inventory.getSize());

            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            dataOutput.close();
            encoded = Base64Coder.encodeLines(outputStream.toByteArray());
            return encoded;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }        
    }
    public static String getEncode()
    {
    	return encoded;
    }
    public static String getIEncode()
    {
    	return iEncoded;
    }
    public static String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
    	try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            

            dataOutput.writeInt(items.length);

            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }

            dataOutput.close();
            iEncoded = Base64Coder.encodeLines(outputStream.toByteArray());
            return iEncoded;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }
    public static Inventory fromBase64(String data) throws IOException {
        try {
        	int leng = data.length();
        	String lengS = String.valueOf(leng);
        	Bukkit.getLogger().info(lengS);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            //int roundup = (dataInput.readInt() + (9-1)) / 9 * 9;
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());
    
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }
            dataInput.close();
            return inventory;
        } catch (Exception e) {
        	e.printStackTrace();
            throw new IOException("Unable to decode class type.", e);
        }
    }
	public static ItemStack[] newItems;
    public static ItemStack[] getItems()
    {    	
    	return newItems;
    }
    public static ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
    	try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];
    
            for (int i = 0; i < items.length; i++) {
            	items[i] = (ItemStack) dataInput.readObject();
            }
            
            dataInput.close();
            newItems = items;
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.", e);
        }
    }
}